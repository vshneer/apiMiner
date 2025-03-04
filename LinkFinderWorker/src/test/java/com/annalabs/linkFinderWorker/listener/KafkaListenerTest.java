package com.annalabs.linkFinderWorker.listener;

import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.entity.ScopeEntity;
import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.linkFinderWorker.worker.LinkFinderWorker;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"project"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class KafkaListenerTest {
    public static final String TEST_IN_SCOPE = "fnx.co.il";
    public static final String TEST_SUBDOMAIN = TEST_IN_SCOPE;
    public static final String DUMMY_PROJECT_ID = "dummy-project-id";

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        mongoDBContainer.start();
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KafkaMessageListener listener;
    @Autowired
    private LinkFinderWorker worker;
    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    @Value("${kafka.topics.project}")
    private String projectTopic;
    @Value("${kafka.topics.subdomain}")
    private String subdomainTopic;
    @Value("${kafka.topics.js}")
    private String jsTopic;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }


    @Test
    public void projectTopicListenerWorks() {
        ProjectEntity projectEntity = imitateWorkOfProjectCreatorService();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // ✅ Verify that worker.work() was called with smth
            verify(worker, times(1)).processMessage(projectEntity.getId(), TEST_IN_SCOPE);
        });
    }

    @Test
    public void subdomainTopicListenerWorks() throws InterruptedException {
        imitateWorkOfService(subdomainTopic);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // ✅ Verify that worker.work() was called with smth
            verify(worker, times(1)).processMessage(DUMMY_PROJECT_ID, TEST_SUBDOMAIN);
        });
    }

    @Test
    public void jsTopicListenerWorks() throws InterruptedException {
        imitateWorkOfService(jsTopic);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // ✅ Verify that worker.work() was called with smth
            verify(worker, times(1)).processMessage(DUMMY_PROJECT_ID, TEST_SUBDOMAIN);
        });
    }

    private ProjectEntity imitateWorkOfProjectCreatorService() {
        ProjectEntity projectEntity = populateMongoWithNewProject();
        sendNewProjectEventToKafka();
        return projectEntity;
    }

    private void imitateWorkOfService(String topic) throws InterruptedException {
        KafkaMessage testMessage = new KafkaMessage(DUMMY_PROJECT_ID, TEST_SUBDOMAIN, "");
        Thread.sleep(5000); // Wait for Kafka to fully initialize
        kafkaTemplate.send(topic, testMessage);
    }

    private void sendNewProjectEventToKafka() {
        List<ProjectEntity> projects = mongoTemplate.findAll(ProjectEntity.class);
        String projectId = projects.getFirst().getId();
        KafkaMessage testMessage = new KafkaMessage(projectId, "", "");
        kafkaTemplate.send(projectTopic, testMessage);
    }

    public ProjectEntity populateMongoWithNewProject() {
        // Create a Project document
        ProjectEntity project = new ProjectEntity("test-title", new ScopeEntity(List.of(), List.of(TEST_IN_SCOPE)));
        // Save it in MongoDB
        mongoTemplate.save(project);
        return project;
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public LinkFinderWorker mockWorker() {
            return mock(LinkFinderWorker.class);
        }
    }
}
