package com.annalabs.getJsWorker.processor;

import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.getJsWorker.worker.JsWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.annalabs.common.constant.Collection.projectCollection;

@Component
public class ProjectMessageProcessor {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private JsWorker jsWorker;
    public void process(KafkaMessage message) {
        System.out.println("📌 Processing project message: " + message);
        ProjectEntity project = mongoTemplate.findOne(new Query(new Criteria("_id").is(message.getProjectId())), ProjectEntity.class, projectCollection);
        Optional.ofNullable(project).orElseThrow(() -> new RuntimeException("Project not found"));
        project.getScope().getInScope().parallelStream().forEach(domain -> jsWorker.processMessage(message.getProjectId(),domain));
    }
}
