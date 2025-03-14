package com.annalabs.getJsWorker.listener;

import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.getJsWorker.processor.ProjectMessageProcessor;
import com.annalabs.getJsWorker.processor.SubdomainMessageProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class KafkaMessageListener {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Thread pool for async processing
    @Autowired
    private ProjectMessageProcessor projectMessageProcessor;
    @Autowired
    private SubdomainMessageProcessor subdomainMessageProcessor;
    @Value("${kafka.topics.project}")
    private String projectTopic;
    @Value("${kafka.topics.subdomain}")
    private String subdomainTopic;

    @KafkaListener(topics = {"${kafka.topics.project}", "${kafka.topics.subdomain}"}, groupId = "${kafka.groups.getjs}")
    public void listen(ConsumerRecord<String, KafkaMessage> record) {
        String topic = record.topic();
        KafkaMessage message = record.value();
        executorService.submit(() -> {
            if (topic.equals(projectTopic)) {
                projectMessageProcessor.process(message);
            } else if (topic.equals(subdomainTopic)) {
                subdomainMessageProcessor.process(message);
            } else {
                System.err.println("⚠️ Received message from unknown topic: " + topic);
            }
        });
    }


}
