package com.annalabs.certificateTransparencyWorker.worker;

import com.annalabs.certificateTransparencyWorker.client.CrtShClient;
import com.annalabs.common.kafka.KafkaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CertificateTransparencyLogWorker {


    @Autowired
    CrtShClient crtShClient;
    @Autowired
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    @Value("${kafka.topics.subdomain}")
    private String topic;

    public void work(String projectId, String domain) throws IOException {
        crtShClient.getSubdomains(domain)
                .forEach(subdomain ->
                        kafkaTemplate.send(
                                topic,
                                new KafkaMessage(
                                        projectId,
                                        subdomain,
                                        "CertificateTransparencyLogWorker")));

    }
}
