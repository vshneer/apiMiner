package com.annalabs.common.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class KafkaMessage implements Serializable {
    private String projectId;
    private String content;
    private String source;

    @JsonCreator
    public KafkaMessage(
            @JsonProperty("projectId") String projectId,
            @JsonProperty("content") String content,
            @JsonProperty("source") String source) {
        this.projectId = projectId;
        this.content = content;
        this.source = source;
    }

}
