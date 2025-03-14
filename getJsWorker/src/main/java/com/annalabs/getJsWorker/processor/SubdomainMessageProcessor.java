package com.annalabs.getJsWorker.processor;

import com.annalabs.common.kafka.KafkaMessage;
import com.annalabs.getJsWorker.worker.JsWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubdomainMessageProcessor {
    @Autowired
    private JsWorker jsWorker;
    public void process(KafkaMessage message) {
        System.out.println("📌 Processing subdomain message: " + message);
        jsWorker.processMessage(message.getProjectId(), message.getContent());
    }
}
