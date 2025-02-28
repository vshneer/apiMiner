package com.annalabs.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@Getter
@Setter
public class ScopeEntity {
    List<String> outScope;
    List<String> inScope;
}
