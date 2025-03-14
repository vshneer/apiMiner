package com.annalabs.enumerationRequestPublisher.response;

import com.annalabs.common.entity.AssetEntity;

import java.util.List;

public class AssetResponse {

    List<AssetEntity> asset;

    public AssetResponse(List<AssetEntity> asset) {
        this.asset = asset;
    }
}
