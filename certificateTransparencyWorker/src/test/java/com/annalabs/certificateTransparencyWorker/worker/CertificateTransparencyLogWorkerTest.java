package com.annalabs.certificateTransparencyWorker.worker;

import com.annalabs.certificateTransparencyWorker.client.CrtShClient;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CertificateTransparencyLogWorkerTest {

    public static final String TEST_D = "fnx.co.il";

    @Mock // ✅ This ensures only the mock is used
    private CrtShClient client;

    @InjectMocks
    private CertificateTransparencyLogWorker certificateTransparencyLogWorker;

    @Test
    void workerCallsCertClient() throws IOException {
        certificateTransparencyLogWorker.work("", TEST_D);
        verify(client, times(1)).getSubdomains(TEST_D);
    }

}