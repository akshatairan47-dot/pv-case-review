package com.pvreview.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldRecord {

    private String value;
    private Double confidence;
    private String source;
    private MergeStatus status;
    private String previousValue;
}
