package com.annalabs.enumerationRequestPublisher.controller;

import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.enumerationRequestPublisher.entity.ProjectEntity;
import com.annalabs.enumerationRequestPublisher.entity.ScopeEntity;
import com.annalabs.enumerationRequestPublisher.request.PostProjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectControllerTest {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        kafkaContainer.start();
        mongoDBContainer.start();
    }

    private final CountDownLatch latch = new CountDownLatch(1);
    @Autowired
    ProjectController controller;

    private KafkaMessage receivedMessage;
    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void testLaunch() throws InterruptedException {
        // Send request to the controller
        controller.createProject(new PostProjectRequest(new ScopeEntity(List.of(), List.of()), "test"));
        // Wait for Kafka message
        boolean messageConsumed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageConsumed, "Message was not consumed in time");

        // Verify MongoDB document exists
        List<ProjectEntity> projects = mongoTemplate.findAll(ProjectEntity.class);
        assertEquals(1, projects.size(), "Expected one document in MongoDB");

        // Get the ID of the stored document
        String storedDocumentId = projects.getFirst().getId();
        assertNotNull(storedDocumentId, "Stored document ID should not be null");

        // Verify Kafka message contains the correct document ID
        assertEquals(storedDocumentId, receivedMessage.getProjectId(), "Kafka message should contain the project ID");
    }

    @KafkaListener(topics = {"${kafka.topics.project}"}, groupId = "${kafka.groups.getjs}")
    public void listen(KafkaMessage message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}