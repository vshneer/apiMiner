package com.annalabs.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.annalabs.common.constant.Collection.projectCollection;

@Document(collection = projectCollection)
@Getter
@Setter
public class ProjectEntity {
    @Id
    private String id;
    private String title;
    private ScopeEntity scope;

    public ProjectEntity(String title, ScopeEntity scope) {
        this.title = title;
        this.scope = scope;
    }
}
