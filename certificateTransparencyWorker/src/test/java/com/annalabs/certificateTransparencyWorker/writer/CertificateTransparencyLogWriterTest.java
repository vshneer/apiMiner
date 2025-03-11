package com.annalabs.certificateTransparencyWorker.writer;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.common.kafka.KafkaMessage;
import org.bson.Document;
import org.junit.Assert;
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
public class CertificateTransparencyLogWriterTest {
    public static final String TEST_D = "fnx.co.il";
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        kafkaContainer.start();
        mongoDBContainer.start();
    }

    private final CountDownLatch latch = new CountDownLatch(1);
    @Autowired
    CertificateTransparencyLogWriter writer;
    private KafkaMessage receivedMessage;
    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void writerWritesToKafka() throws InterruptedException {
                /*
            If worker gets domain
            It produces a message to Kafka
            With subdomains
         */
        Thread.sleep(2000); // Wait for Kafka to fully initialize
        writer.persist("", TEST_D);
        // Wait for the message to be consumed
        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);
        // Verify the received message
        assertTrue(messageConsumed, "Message was not consumed in time");

    }

    @Test
    void writerWritesToMongo(){
        writer.persist("", TEST_D);
        assertFalse(mongoTemplate.findAll(AssetEntity.class, "Asset").isEmpty());
    }


    @KafkaListener(topics = {"${kafka.topics.subdomain}"}, groupId = "${kafka.groups.certsh}")
    public void listen(KafkaMessage message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}
