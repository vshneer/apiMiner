package com.annalabs.getJsWorker.listener;

import com.annalabs.common.entity.ProjectEntity;
import com.annalabs.common.entity.ScopeEntity;
import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.getJsWorker.processor.ProjectMessageProcessor;
import com.annalabs.getJsWorker.processor.SubdomainMessageProcessor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"project", "subdomain"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class KafkaMessageListenerTest {

    public static final String TEST_IN_SCOPE = "fnx.co.il";
    public static final String TEST_SUBDOMAIN = TEST_IN_SCOPE;
    public static final String DUMMY_PROJECT_ID = "dummy-project-id";
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        mongoDBContainer.start();
    }

    KafkaMessage testMessage = new KafkaMessage(DUMMY_PROJECT_ID, TEST_SUBDOMAIN, "");
    @Captor
    private ArgumentCaptor<KafkaMessage> messageCaptor;
    @Autowired
    private KafkaMessageListener listener;
    @Autowired
    private ProjectMessageProcessor projectMessageProcessor;
    @Autowired
    private SubdomainMessageProcessor subdomainMessageProcessor;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    @Value("${kafka.topics.project}")
    private String projectTopic;
    @Value("${kafka.topics.subdomain}")
    private String subdomainTopic;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    public void populateMongoWithNewProject() {
        // Create a Project document
        ProjectEntity project = new ProjectEntity("test-title", new ScopeEntity(List.of(), List.of(TEST_IN_SCOPE)));
        // Save it in MongoDB
        mongoTemplate.save(project);
    }

    @BeforeEach
    public void checkMocks() {
        System.out.println("🔍 Processor is: " + projectMessageProcessor);
        System.out.println("🔍 Processor is a mock? " + (Mockito.mockingDetails(projectMessageProcessor).isMock()));
    }

    @Test
    public void projectTopicListenerCallsProcessor() throws IOException {
        /*
            If Listener gets kafka message from project topic
            It calls worker with correct parameters
         */

        // when message is consumed
        imitateWorkOfProjectCreatorService();
        // then worker starts
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // ✅ Verify that worker.work() was called with smth
            verify(projectMessageProcessor, times(1)).process(any(KafkaMessage.class));
        });
    }

    @Test
    public void subdomainTopicListenerCallsProcessor() throws IOException, InterruptedException {
        /*
            If Listener gets kafka message from subdomain topic
            It calls worker with correct parameters
         */

        // when message is consumed
        imitateWorkOfSubdomainEnumService();
        // then worker starts
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // ✅ Verify that worker.work() was called with smth
            verify(subdomainMessageProcessor, times(1)).process(any(KafkaMessage.class));
        });
    }

    private void imitateWorkOfSubdomainEnumService() throws InterruptedException {
        Thread.sleep(5000); // Wait for Kafka to fully initialize
        kafkaTemplate.send(subdomainTopic, testMessage);
    }

    private void imitateWorkOfProjectCreatorService() {
        populateMongoWithNewProject();
        sendNewProjectEventToKafka();
    }

    private void sendNewProjectEventToKafka() {
        List<ProjectEntity> projects = mongoTemplate.findAll(ProjectEntity.class);
        String projectId = projects.getFirst().getId();
        KafkaMessage testMessage = new KafkaMessage(projectId, "", "");
        kafkaTemplate.send(projectTopic, testMessage);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public ProjectMessageProcessor mockProjectProcessor() {
            return mock(ProjectMessageProcessor.class);
        }

        @Bean
        @Primary
        public SubdomainMessageProcessor mockSubdomainProcessor() {
            return mock(SubdomainMessageProcessor.class);
        }
    }

}
