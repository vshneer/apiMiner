package com.annalabs.linkFinderWorker.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


@Component
public class LinkFinderWorker {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.api}")
    private String topic;

    public void processMessage(String message) {
        try {
            // Build the command safely
            List<String> command = Arrays.asList("GoLinkFinder", "-d", message);
            System.out.println("Executing command: " + String.join(" ", command));
            // Execute the process with proper stream handling
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            // Create an ExecutorService with a timeout
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<List<String>> outputFuture = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    List<String> result = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        result.add(line);
                    }
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Future<String> errorFuture = executor.submit(() -> {
                StringBuilder error = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                        System.err.println(line);
                    }
                }
                return error.toString();
            });

            try {
                // Wait for process completion with a timeout
                boolean completed = process.waitFor(5, TimeUnit.MINUTES);
                if (!completed) {
                    process.destroyForcibly();
                    throw new TimeoutException("Process timed out after 5 minutes");
                }

                // Get the output and error streams with timeout
                String error = errorFuture.get(1, TimeUnit.MINUTES);


                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    System.err.println("Process exited with non-zero code: " + exitCode);
                    System.err.println("Error output: " + error);
                    throw new RuntimeException();
                }

                System.out.println("Process completed successfully.");
                outputFuture.get().forEach(result -> kafkaTemplate.send(topic, result));


            } catch (TimeoutException e) {
                System.err.println("Timeout while waiting for process: " + e.getMessage());
                process.destroyForcibly();
            } finally {
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate in time");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Error executing command: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
