package com.annalabs.enumerationRequestPublisher.controller;

import com.annalabs.enumerationRequestPublisher.service.AssetService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AssetControllerTest {
    @Mock
    AssetService service;

    @InjectMocks
    AssetController controller;

    @Test
    void getAssetControllerCallsService() {
        controller.getAsset();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(service, times(1)).getAsset());
    }
}