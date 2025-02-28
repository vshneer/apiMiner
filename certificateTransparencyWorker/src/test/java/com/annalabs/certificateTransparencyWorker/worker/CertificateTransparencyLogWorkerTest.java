package com.annalabs.certificateTransparencyWorker.worker;

import com.annalabs.common.kafka.KafkaMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CertificateTransparencyLogWorkerTest {

    public static final String TEST_D = "fnx.co.il";

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    static {
        kafkaContainer.start();
    }

    private final CountDownLatch latch = new CountDownLatch(1);
    @Autowired
    CertificateTransparencyLogWorker certificateTransparencyLogWorker;
    private KafkaMessage receivedMessage;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    void workerPublishesToKafka() throws InterruptedException, IOException {
        /*
            If worker gets domain
            It produces a message to Kafka
            With subdomains
         */
        certificateTransparencyLogWorker.work("", TEST_D);
        // Wait for the message to be consumed
        boolean messageConsumed = latch.await(2, TimeUnit.SECONDS);
        // Verify the received message
        assertTrue(messageConsumed, "Message was not consumed in time");
    }

    @KafkaListener(topics = {"${kafka.topics.subdomain}"}, groupId = "${kafka.groups.certsh}")
    public void listen(KafkaMessage message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}