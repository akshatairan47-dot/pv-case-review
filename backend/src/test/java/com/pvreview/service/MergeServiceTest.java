package com.pvreview.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pvreview.dto.CasePayload;
import com.pvreview.dto.RawField;
import com.pvreview.model.Case;
import com.pvreview.model.FieldRecord;
import com.pvreview.model.MergeStatus;

class MergeServiceTest {

    private final MergeService mergeService = new MergeService();

    private Case baseline;

    @BeforeEach
    void setUp() {
        Map<String, FieldRecord> patient = new LinkedHashMap<>();
        patient.put("initials", field("M.K.", 0.98, "p.2 §1"));
        patient.put("age", field("62", 0.9, "p.2 §1"));
        patient.put("sex", field("Male", 0.99, "p.2 §1"));
        patient.put("weight_kg", field("78", 0.85, "p.3 §2"));

        Map<String, FieldRecord> adverseEvent = new LinkedHashMap<>();
        adverseEvent.put("event_term", field("Myalgia", 0.94, "p.4 §1"));
        adverseEvent.put("onset_date", field("2026-03-12", 0.72, "p.4 §2"));
        adverseEvent.put("outcome", field("Recovered", 0.81, "p.5 §1"));
        adverseEvent.put("seriousness", field("Non-serious", 0.79, "p.5 §1"));

        Map<String, FieldRecord> suspectDrug = new LinkedHashMap<>();
        suspectDrug.put("drug_name", field("Atorvastatin", 0.97, "p.3 §3"));

        Map<String, Map<String, FieldRecord>> sections = new LinkedHashMap<>();
        sections.put("patient", patient);
        sections.put("adverse_event", adverseEvent);
        sections.put("suspect_drug", suspectDrug);

        baseline = Case.builder()
                .caseId("PV-2026-0451")
                .version(1)
                .caseClassification("non-significant")
                .extractedAt("2026-01-10T10:00:00Z")
                .sourceDocument("initial_report_PV-2026-0451.pdf")
                .sections(sections)
                .missingFields(List.of())
                .build();
    }

    private FieldRecord field(String value, double confidence, String source) {
        return FieldRecord.builder().value(value).confidence(confidence).source(source).build();
    }

    private RawField raw(String value, double confidence, String source) {
        return RawField.builder().value(value).confidence(confidence).source(source).build();
    }

    @Test
    void sameValueIsMarkedUnchanged() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("sex", raw("Male", 0.99, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        FieldRecord sex = merged.getSections().get("patient").get("sex");
        assertThat(sex.getStatus()).isEqualTo(MergeStatus.UNCHANGED);
        assertThat(sex.getValue()).isEqualTo("Male");
        assertThat(sex.getPreviousValue()).isNull();
    }

    @Test
    void differingValueIsOverriddenAndKeepsPreviousValue() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        FieldRecord age = merged.getSections().get("patient").get("age");
        assertThat(age.getStatus()).isEqualTo(MergeStatus.OVERRIDDEN);
        assertThat(age.getValue()).isEqualTo("63");
        assertThat(age.getPreviousValue()).isEqualTo("62");
    }

    @Test
    void fieldAbsentFromFollowUpIsRetainedFromStoredCase() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        FieldRecord weight = merged.getSections().get("patient").get("weight_kg");
        assertThat(weight.getStatus()).isEqualTo(MergeStatus.RETAINED);
        assertThat(weight.getValue()).isEqualTo("78");
        assertThat(weight.getPreviousValue()).isNull();
    }

    @Test
    void newFieldInExistingSectionIsMarkedNew() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("adverse_event", Map.of("severity_grade", raw("Grade 2", 0.83, "p.4 §3"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        FieldRecord severity = merged.getSections().get("adverse_event").get("severity_grade");
        assertThat(severity.getStatus()).isEqualTo(MergeStatus.NEW);
        assertThat(severity.getValue()).isEqualTo("Grade 2");
        assertThat(severity.getPreviousValue()).isNull();

        FieldRecord eventTerm = merged.getSections().get("adverse_event").get("event_term");
        assertThat(eventTerm.getStatus()).isEqualTo(MergeStatus.RETAINED);
    }

    @Test
    void entirelyNewSectionIsMarkedNew() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("lab_results", Map.of("ck_level", raw("450 U/L", 0.89, "p.6 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        FieldRecord ckLevel = merged.getSections().get("lab_results").get("ck_level");
        assertThat(ckLevel.getStatus()).isEqualTo(MergeStatus.NEW);
        assertThat(ckLevel.getValue()).isEqualTo("450 U/L");
    }

    @Test
    void sectionOmittedFromFollowUpIsRetainedFieldByField() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        Map<String, FieldRecord> suspectDrug = merged.getSections().get("suspect_drug");
        assertThat(suspectDrug.get("drug_name").getStatus()).isEqualTo(MergeStatus.RETAINED);
        assertThat(suspectDrug.get("drug_name").getValue()).isEqualTo("Atorvastatin");
    }

    @Test
    void versionIncrementsWhenNotExplicitlyProvided() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getVersion()).isEqualTo(2);
    }

    @Test
    void explicitVersionInPayloadOverridesAutoIncrement() {
        CasePayload payload = CasePayload.builder()
                .version(5)
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getVersion()).isEqualTo(5);
    }

    @Test
    void caseClassificationFallsBackToStoredValueWhenOmitted() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getCaseClassification()).isEqualTo("non-significant");
    }

    @Test
    void caseClassificationIsOverriddenWhenProvided() {
        CasePayload payload = CasePayload.builder()
                .caseClassification("significant")
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getCaseClassification()).isEqualTo("significant");
    }

    @Test
    void missingFieldsFromPayloadArePassedThrough() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .missingFields(List.of("patient.weight_kg"))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getMissingFields()).containsExactly("patient.weight_kg");
    }

    @Test
    void missingFieldsDefaultToEmptyWhenOmitted() {
        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age", raw("63", 0.9, "p.2 §1"))))
                .build();

        Case merged = mergeService.merge(baseline, payload);

        assertThat(merged.getMissingFields()).isEmpty();
    }
}
