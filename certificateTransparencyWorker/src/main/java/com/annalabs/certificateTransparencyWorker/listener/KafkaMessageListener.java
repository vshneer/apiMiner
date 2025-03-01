package com.annalabs.certificateTransparencyWorker.listener;

import com.annalabs.certificateTransparencyWorker.worker.CertificateTransparencyLogWorker;
import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.annalabs.common.constant.Collection.projectCollection;

@Component
public class KafkaMessageListener {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CertificateTransparencyLogWorker worker;

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);

    @KafkaListener(topics = {"${kafka.topics.project}"}, groupId = "${kafka.groups.certsh}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(KafkaMessage message) {
        logger.info("Received Kafka message: {}", message);
        // TODO semantic improvement - extract it to processor class
        ProjectEntity project = mongoTemplate.findOne(new Query(new Criteria("_id").is(message.getProjectId())), ProjectEntity.class, projectCollection);
        Optional.ofNullable(project).orElseThrow(() -> new RuntimeException("Project not found"));
        project
                .getScope()
                .getInScope()
                .stream().peek(inScopeDomain -> { /* TODO extract subdomain list from regex */})
                .forEach(domain -> {
                    try {
                        logger.info("Calling worker.work({}, {})", message.getProjectId(), domain);
                        worker.work(message.getProjectId(), domain);
                    } catch (IOException e) {
                        logger.error("Error processing message", e);
                    }
                });
    }
}
