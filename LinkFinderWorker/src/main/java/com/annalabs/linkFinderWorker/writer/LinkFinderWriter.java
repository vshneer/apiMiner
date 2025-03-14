package com.annalabs.linkFinderWorker.writer;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LinkFinderWriter {

    @Value("${kafka.topics.api}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void persist(String projectId, String link){
        AssetEntity asset = new AssetEntity(projectId, "LinkFinderWorker", new Document("link", link));
        mongoTemplate.insert(asset);
        kafkaTemplate.send(topic, new KafkaMessage(projectId, link, "LinkFinderWorker"));
    }
}
