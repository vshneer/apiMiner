package com.annalabs.certificateTransparencyWorker.writer;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CertificateTransparencyLogWriter {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Value("${kafka.topics.subdomain}")
    private String topic;

    public void persist(String projectId, String subdomain) {
        AssetEntity asset = new AssetEntity(projectId, "CertificateTransparencyLogWorker", new Document("subdomain", subdomain));
        mongoTemplate.insert(asset);
        kafkaTemplate.send(topic, new KafkaMessage(projectId, subdomain, "CertificateTransparencyLogWorker"));
    }
}
