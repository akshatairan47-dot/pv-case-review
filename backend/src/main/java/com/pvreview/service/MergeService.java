package com.pvreview.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.pvreview.dto.CasePayload;
import com.pvreview.dto.RawField;
import com.pvreview.model.Case;
import com.pvreview.model.FieldRecord;
import com.pvreview.model.MergeStatus;

@Service
public class MergeService {

    public Case merge(Case current, CasePayload payload) {
        Objects.requireNonNull(payload, "payload must not be null");

        String caseId = current != null ? current.getCaseId() : payload.getCaseId();
        int version = resolveVersion(current, payload);
        String caseClassification = coalesce(payload.getCaseClassification(),
                current != null ? current.getCaseClassification() : null);
        String extractedAt = coalesce(payload.getExtractedAt(),
                current != null ? current.getExtractedAt() : null);
        String sourceDocument = coalesce(payload.getSourceDocument(),
                current != null ? current.getSourceDocument() : null);

        Map<String, Map<String, FieldRecord>> mergedSections = mergeSections(current, payload);
        List<String> missingFields = payload.getMissingFields() != null
                ? payload.getMissingFields()
                : Collections.emptyList();

        return Case.builder()
                .caseId(caseId)
                .version(version)
                .caseClassification(caseClassification)
                .extractedAt(extractedAt)
                .sourceDocument(sourceDocument)
                .sections(mergedSections)
                .missingFields(missingFields)
                .build();
    }

    private int resolveVersion(Case current, CasePayload payload) {
        if (payload.getVersion() != null) {
            return payload.getVersion();
        }
        return current != null ? current.getVersion() + 1 : 1;
    }

    private String coalesce(String preferred, String fallback) {
        return preferred != null ? preferred : fallback;
    }

    private Map<String, Map<String, FieldRecord>> mergeSections(Case current, CasePayload payload) {
        Map<String, Map<String, FieldRecord>> currentSections = current != null && current.getSections() != null
                ? current.getSections()
                : Collections.emptyMap();
        Map<String, Map<String, RawField>> payloadSections = payload.getSections() != null
                ? payload.getSections()
                : Collections.emptyMap();

        Map<String, Map<String, FieldRecord>> merged = new LinkedHashMap<>();
        for (String sectionKey : orderedKeys(payloadSections.keySet(), currentSections.keySet())) {
            Map<String, FieldRecord> currentFields = currentSections.getOrDefault(sectionKey, Collections.emptyMap());
            Map<String, RawField> payloadFields = payloadSections.getOrDefault(sectionKey, Collections.emptyMap());
            merged.put(sectionKey, mergeFields(currentFields, payloadFields));
        }
        return merged;
    }

    private Map<String, FieldRecord> mergeFields(Map<String, FieldRecord> currentFields,
            Map<String, RawField> payloadFields) {
        Map<String, FieldRecord> merged = new LinkedHashMap<>();
        for (String fieldKey : orderedKeys(payloadFields.keySet(), currentFields.keySet())) {
            RawField newField = payloadFields.get(fieldKey);
            FieldRecord oldField = currentFields.get(fieldKey);
            if (newField == null && oldField == null) {
                continue;
            }
            merged.put(fieldKey, mergeField(oldField, newField));
        }
        return merged;
    }

    private FieldRecord mergeField(FieldRecord oldField, RawField newField) {
        if (newField != null && oldField != null) {
            boolean sameValue = Objects.equals(newField.getValue(), oldField.getValue());
            FieldRecord.FieldRecordBuilder builder = FieldRecord.builder()
                    .value(newField.getValue())
                    .confidence(newField.getConfidence())
                    .source(newField.getSource());
            return sameValue
                    ? builder.status(MergeStatus.UNCHANGED).build()
                    : builder.status(MergeStatus.OVERRIDDEN).previousValue(oldField.getValue()).build();
        }

        if (newField != null) {
            return FieldRecord.builder()
                    .value(newField.getValue())
                    .confidence(newField.getConfidence())
                    .source(newField.getSource())
                    .status(MergeStatus.NEW)
                    .build();
        }

        return FieldRecord.builder()
                .value(oldField.getValue())
                .confidence(oldField.getConfidence())
                .source(oldField.getSource())
                .status(MergeStatus.RETAINED)
                .build();
    }

    private List<String> orderedKeys(Set<String> primary, Set<String> secondary) {
        LinkedHashSet<String> keys = new LinkedHashSet<>(primary);
        keys.addAll(secondary);
        return new ArrayList<>(keys);
    }
}
