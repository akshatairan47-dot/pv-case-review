package com.pvreview.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pvreview.dto.CasePayload;
import com.pvreview.dto.RawField;
import com.pvreview.exception.CaseNotFoundException;
import com.pvreview.model.Case;
import com.pvreview.model.FieldRecord;
import com.pvreview.repository.CaseStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseStore caseStore;
    private final MergeService mergeService;

    public Case getCase(String caseId) {
        return caseStore.find(caseId)
                .orElseThrow(() -> new CaseNotFoundException("No case found with id " + caseId));
    }

    public Case applyFollowUp(String caseId, CasePayload payload) {
        Case current = caseStore.find(caseId)
                .orElseThrow(() -> new CaseNotFoundException("No case found with id " + caseId));
        Case merged = mergeService.merge(current, payload);
        return caseStore.save(merged);
    }

    public Case bootstrap(CasePayload payload) {
        return caseStore.save(toBaselineCase(payload));
    }

    private Case toBaselineCase(CasePayload payload) {
        Map<String, Map<String, FieldRecord>> sections = new LinkedHashMap<>();
        if (payload.getSections() != null) {
            for (Map.Entry<String, Map<String, RawField>> sectionEntry : payload.getSections().entrySet()) {
                Map<String, FieldRecord> fields = new LinkedHashMap<>();
                for (Map.Entry<String, RawField> fieldEntry : sectionEntry.getValue().entrySet()) {
                    RawField raw = fieldEntry.getValue();
                    fields.put(fieldEntry.getKey(), FieldRecord.builder()
                            .value(raw.getValue())
                            .confidence(raw.getConfidence())
                            .source(raw.getSource())
                            .build());
                }
                sections.put(sectionEntry.getKey(), fields);
            }
        }

        return Case.builder()
                .caseId(payload.getCaseId())
                .version(payload.getVersion() != null ? payload.getVersion() : 1)
                .caseClassification(payload.getCaseClassification())
                .extractedAt(payload.getExtractedAt())
                .sourceDocument(payload.getSourceDocument())
                .sections(sections)
                .missingFields(payload.getMissingFields() != null ? payload.getMissingFields() : List.of())
                .build();
    }
}
