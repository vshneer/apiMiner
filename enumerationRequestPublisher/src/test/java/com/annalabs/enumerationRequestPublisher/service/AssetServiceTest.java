package com.annalabs.enumerationRequestPublisher.service;

import com.annalabs.enumerationRequestPublisher.repository.AssetRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AssetServiceTest {

    @Mock
    AssetRepository repository;

    @InjectMocks
    AssetService service;

    @Test
    void getAssetServiceCallsRepository() {
        service.getAsset();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(repository, times(1)).getAsset());
    }

}