package com.annalabs.enumerationRequestPublisher.repository;

import com.annalabs.common.entity.AssetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public List<AssetEntity> getAsset() {
        return mongoTemplate.findAll(AssetEntity.class);
    }
}

