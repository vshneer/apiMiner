package com.annalabs.enumerationRequestPublisher.controller;

import com.annalabs.enumerationRequestPublisher.response.AssetResponse;
import com.annalabs.enumerationRequestPublisher.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import com.annalabs.common.entity.AssetEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssetController {

    @Autowired
    AssetService assetService;

    @GetMapping("/asset")
    public AssetResponse getAsset(){
        List<AssetEntity> asset = assetService.getAsset();
        return new AssetResponse(asset);
    }
}
