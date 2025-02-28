package com.annalabs.common.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class KafkaMessage {
    private String projectId;
    private String content;
    private String source;
}
