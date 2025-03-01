package com.annalabs.getJsWorker.listener;

import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.getJsWorker.worker.JsWorker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.annalabs.common.constant.Collection.projectCollection;

@Component
public class KafkaMessageListener {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Thread pool for async processing
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private JsWorker jsWorker;
    @Value("${kafka.topics.project}")
    private String projectTopic;
    @Value("${kafka.topics.subdomain}")
    private String subdomainTopic;

    @KafkaListener(topics = {"${kafka.topics.project}", "${kafka.topics.subdomain}"}, groupId = "${kafka.groups.getjs}")
    public void listen(ConsumerRecord<String, KafkaMessage> record) {
        String topic = record.topic();  // ✅ Get the topic name
        KafkaMessage message = record.value();  // ✅ Extract message payload

        // ✅ Switch on the topic name
        executorService.submit(() -> {
            if (topic.equals(projectTopic)) {
                handleProjectMessage(message);
            } else if (topic.equals(subdomainTopic)) {
                handleSubdomainMessage(message);
            } else {
                System.err.println("⚠️ Received message from unknown topic: " + topic);
            }
        });
    }

    private void handleProjectMessage(KafkaMessage message) {
        System.out.println("📌 Processing project message: " + message);
        ProjectEntity project = mongoTemplate.findOne(new Query(new Criteria("_id").is(message.getProjectId())), ProjectEntity.class, projectCollection);
        Optional.ofNullable(project).orElseThrow(() -> new RuntimeException("Project not found"));
        project.getScope().getInScope().parallelStream().forEach(domain -> jsWorker.processMessage(message.getProjectId(),domain));
    }

    private void handleSubdomainMessage(KafkaMessage message) {
        System.out.println("📌 Processing subdomain message: " + message);
        jsWorker.processMessage(message.getProjectId(), message.getContent());
    }
}
