package com.annalabs.enumerationRequestPublisher.controller;


import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.enumerationRequestPublisher.request.PostProjectRequest;
import com.annalabs.enumerationRequestPublisher.response.PostProjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProjectController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${kafka.topics.project}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @PostMapping("/project")
    public PostProjectResponse createProject(@RequestBody PostProjectRequest postProjectRequest) {
        // TODO make MONGO + KAFKA transactions atomic
        try {
            // Create a Project document
            ProjectEntity project = new ProjectEntity(postProjectRequest.getTitle(), postProjectRequest.getScope());

            // Save it in MongoDB
            ProjectEntity savedProject = mongoTemplate.save(project);

            // Send the new document's ID to Kafka
            kafkaTemplate.send(topic, new KafkaMessage(savedProject.getId(),"", "createProject"));

            return new PostProjectResponse(savedProject.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return new PostProjectResponse(null);
        }
    }

}
