package com.pvreview.dto;

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
public class CasePayload {

    private String caseId;
    private Integer version;
    private String caseClassification;
    private String extractedAt;
    private String sourceDocument;
    private Map<String, Map<String, RawField>> sections;
    private List<String> missingFields;
}
