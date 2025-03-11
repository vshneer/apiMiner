package com.annalabs.common.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.annalabs.common.constant.Collection.assetCollection;

@Document(collection = assetCollection)
@Getter
@Setter
@NoArgsConstructor
public class AssetEntity {
    @Id
    private String id;
    private String projectId;
    private String source;
    private Date timestamp;
    private org.bson.Document content;

    public AssetEntity(String projectId, String source, org.bson.Document content) {
        this.projectId = projectId;
        this.source = source;
        this.content = content;
        this.timestamp = new Date();
    }

    public AssetEntity(String projectId, String source, org.bson.Document content, Date timestamp) {
        this.projectId = projectId;
        this.content = content;
        this.source = source;
        this.timestamp = timestamp;
    }

}
