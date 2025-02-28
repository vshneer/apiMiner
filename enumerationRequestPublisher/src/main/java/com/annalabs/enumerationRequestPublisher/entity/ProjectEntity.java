package com.annalabs.enumerationRequestPublisher.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Project")
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
