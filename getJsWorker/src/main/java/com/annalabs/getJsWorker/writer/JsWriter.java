package com.annalabs.getJsWorker.writer;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JsWriter {

    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${kafka.topics.js}")
    String topic;

    public void persist(String projectId, String js) {
        AssetEntity asset = new AssetEntity(projectId, "JsWorker", new Document("js", js));
        mongoTemplate.insert(asset);
        kafkaTemplate.send(topic, new KafkaMessage(projectId, js, "JsWorker"));
    }
}
