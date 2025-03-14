package com.annalabs.getJsWorker.worker;

import com.annalabs.getJsWorker.writer.JsWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class JsWorker {
    @Autowired
    JsWriter jsWriter;

    public void processMessage(String projectId, String message) {
        String fileNameInput = message + "_in.txt";
        String fileNameOutput = message + "_out.txt";
        File tempDir = new File(System.getProperty("java.io.tmpdir")); // System temp directory
        File pathInputFile = new File(tempDir, fileNameInput); // Combine tempDir and fileName
        File pathOutputFile = new File(tempDir, fileNameOutput); // Combine tempDir and fileName
        if (!tempDir.exists()) {
            System.err.println("Temp directory does not exist: " + tempDir);
            return;
        }
        try {
            // Write message to file
            // Write the message to the input file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathInputFile))) {
                writer.write("https://" + message);
                writer.newLine(); // Ensure a newline for proper parsing by the tool
            } catch (IOException e) {
                System.err.println("Error writing to input file: " + e.getMessage());
                return;
            }
            String command = "jsfinder -l " + pathInputFile.getAbsolutePath() + " -o " + pathOutputFile.getAbsolutePath();
            // Execute the CLI command
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true); // Redirect stderr to stdout
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Process exited with code: " + exitCode);
                return;
            }
            // Read the output line by line
            try (BufferedReader reader = new BufferedReader(new FileReader(pathOutputFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsWriter.persist(projectId, line);
                }
            } catch (IOException e) {
                System.err.println("Error reading from output file: " + e.getMessage());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
}
