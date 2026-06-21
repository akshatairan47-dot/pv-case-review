package com.pvreview.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {

    private String caseId;
    private int version;
    private String caseClassification;
    private String extractedAt;
    private String sourceDocument;
    private Map<String, Map<String, FieldRecord>> sections;
    private List<String> missingFields;
}
