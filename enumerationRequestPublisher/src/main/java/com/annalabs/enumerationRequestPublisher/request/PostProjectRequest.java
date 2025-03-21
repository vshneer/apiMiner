package com.annalabs.enumerationRequestPublisher.request;

import com.annalabs.common.entity.ScopeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class PostProjectRequest {
    private ScopeEntity scope;
    private String title;
}