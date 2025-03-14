package com.annalabs.enumerationRequestPublisher.repository;

import com.annalabs.common.entity.AssetEntity;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class AssetRepositoryTest {

    @Autowired
    AssetRepository repository;

    @Autowired
    MongoTemplate mongoTemplate;

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void prepopulateMongoWithAsset(){
        mongoTemplate.save(new AssetEntity("testProject", "testSource", new Document("test", "testContent")));
    }

    @AfterEach
    void cleanUpMongo(){
        mongoTemplate.dropCollection(AssetEntity.class);
    }

    @Test
    void getAssetReturnsData(){
        assertFalse(repository.getAsset().isEmpty());
    }
}