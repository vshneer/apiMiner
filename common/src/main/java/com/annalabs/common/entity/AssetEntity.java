package com.annalabs.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.annalabs.common.constant.Collection.assetCollection;

@Document(collection = assetCollection)
@Getter
@Setter
public class AssetEntity {
    @Id
    private String id;
    private String projectId;
    private Date timestamp;
    private org.bson.Document content;

    public AssetEntity(String projectId, org.bson.Document content) {
        this.projectId = projectId;
        this.content = content;
        this.timestamp = new Date();
    }

    public AssetEntity(String projectId, org.bson.Document content, Date timestamp) {
        this.projectId = projectId;
        this.content = content;
        this.timestamp = timestamp;
    }

}
