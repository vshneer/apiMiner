package com.annalabs.certificateTransparencyWorker.listener;

import com.annalabs.certificateTransparencyWorker.worker.CertificateTransparencyLogWorker;
import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.entity.ScopeEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"project"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class KafkaMessageListenerTest {

    public static final String TEST_IN_SCOPE = "fnx.co.il";
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        mongoDBContainer.start();
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    @Value("${kafka.topics.project}")
    private String topic;

    @Mock
    CertificateTransparencyLogWorker worker;

    @InjectMocks
    KafkaMessageListener listener;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    public void populateMongoWithNewProject() {
        // Create a Project document
        ProjectEntity project = new ProjectEntity("test-title", new ScopeEntity(List.of(TEST_IN_SCOPE), List.of()));
        // Save it in MongoDB
        mongoTemplate.save(project);
    }

    @Test
    public void listen() throws IOException {
        /*
            If Listener gets kafka message
            It calls worker with smth
            TODO fix the test
         */
        imitateWorkOfProjectCreatorService();
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // ✅ Verify that worker.work() was called with smth
                    verify(worker, times(1)).work(anyString(), anyString());
                });
    }

    private void imitateWorkOfProjectCreatorService() {
        populateMongoWithNewProject();
        sendNewProjectEventToKafka();
    }

    private void sendNewProjectEventToKafka() {
        List<ProjectEntity> projects = mongoTemplate.findAll(ProjectEntity.class);
        String projectId = projects.getFirst().getId();
        KafkaMessage testMessage = new KafkaMessage(projectId, "", "");
        kafkaTemplate.send(topic, testMessage);
    }
}