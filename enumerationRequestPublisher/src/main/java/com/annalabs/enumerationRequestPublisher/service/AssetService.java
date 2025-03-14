package com.annalabs.enumerationRequestPublisher.service;

import com.annalabs.common.entity.AssetEntity;
import com.annalabs.enumerationRequestPublisher.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    @Autowired
    AssetRepository assetRepository;

    public List<AssetEntity> getAsset() {
        return assetRepository.getAsset();
    }
}
