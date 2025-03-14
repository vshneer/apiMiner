package com.annalabs.certificateTransparencyWorker.worker;

import com.annalabs.certificateTransparencyWorker.client.CrtShClient;
import com.annalabs.certificateTransparencyWorker.writer.CertificateTransparencyLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CertificateTransparencyLogWorker {

    @Autowired
    CrtShClient crtShClient;
    @Autowired
    CertificateTransparencyLogWriter writer;

    public void work(String projectId, String domain) throws IOException {
        crtShClient.getSubdomains(domain).forEach(subdomain -> {
            writer.persist(projectId, subdomain);
        });
    }


}
