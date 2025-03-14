package com.annalabs.getJsWorker.writer;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.common.kafka.KafkaMessage;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JsWriterTest {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        kafkaContainer.start();
        mongoDBContainer.start();
    }
    private final CountDownLatch latch = new CountDownLatch(1);

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    @Autowired
    JsWriter jsWriter;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void jsWriterWritesToKafka() throws InterruptedException {
        Thread.sleep(2000); // Wait for Kafka to fully initialize
        jsWriter.persist("", "test.js");
        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);
        // Verify the received message
        assertTrue(messageConsumed, "Message was not consumed in time");
    }

    @Test
    void jsWriterWritesToMongo(){
        jsWriter.persist("", "test.js");
        assertFalse(mongoTemplate.findAll(AssetEntity.class, "Asset").isEmpty());
    }

    @KafkaListener(topics = {"${kafka.topics.js}"}, groupId = "${kafka.groups.getjs}")
    public void listen(KafkaMessage message) {
        latch.countDown();
    }
}
