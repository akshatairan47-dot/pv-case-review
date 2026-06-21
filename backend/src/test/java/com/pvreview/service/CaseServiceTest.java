package com.pvreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pvreview.dto.CasePayload;
import com.pvreview.dto.RawField;
import com.pvreview.exception.CaseNotFoundException;
import com.pvreview.model.Case;
import com.pvreview.repository.CaseStore;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseStore caseStore;

    private final MergeService mergeService = new MergeService();

    private CaseService caseService() {
        return new CaseService(caseStore, mergeService);
    }

    @Test
    void getCaseReturnsStoredCase() {
        CaseService caseService = caseService();
        Case stored = Case.builder().caseId("PV-2026-0451").version(1).build();
        when(caseStore.find("PV-2026-0451")).thenReturn(Optional.of(stored));

        Case result = caseService.getCase("PV-2026-0451");

        assertThat(result).isSameAs(stored);
    }

    @Test
    void getCaseThrowsWhenNotFound() {
        CaseService caseService = caseService();
        when(caseStore.find("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseService.getCase("UNKNOWN"))
                .isInstanceOf(CaseNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void applyFollowUpThrowsWhenCaseNotFound() {
        CaseService caseService = caseService();
        when(caseStore.find("UNKNOWN")).thenReturn(Optional.empty());
        CasePayload payload = CasePayload.builder().build();

        assertThatThrownBy(() -> caseService.applyFollowUp("UNKNOWN", payload))
                .isInstanceOf(CaseNotFoundException.class);

        verify(caseStore, never()).save(any());
    }

    @Test
    void applyFollowUpMergesAndSavesResult() {
        CaseService caseService = caseService();
        Case stored = Case.builder()
                .caseId("PV-2026-0451")
                .version(1)
                .sections(Map.of())
                .build();
        when(caseStore.find("PV-2026-0451")).thenReturn(Optional.of(stored));
        when(caseStore.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CasePayload payload = CasePayload.builder()
                .sections(Map.of("patient", Map.of("age",
                        RawField.builder().value("63").confidence(0.9).source("p.2").build())))
                .build();

        Case result = caseService.applyFollowUp("PV-2026-0451", payload);

        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getSections().get("patient").get("age").getValue()).isEqualTo("63");
        verify(caseStore).save(result);
    }

    @Test
    void bootstrapBuildsBaselineCaseWithNoStatusAndSavesIt() {
        CaseService caseService = caseService();
        when(caseStore.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CasePayload payload = CasePayload.builder()
                .caseId("PV-2026-0451")
                .caseClassification("non-significant")
                .sections(Map.of("patient", Map.of("age",
                        RawField.builder().value("62").confidence(0.9).source("p.2 §1").build())))
                .build();

        Case result = caseService.bootstrap(payload);

        assertThat(result.getCaseId()).isEqualTo("PV-2026-0451");
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getSections().get("patient").get("age").getValue()).isEqualTo("62");
        assertThat(result.getSections().get("patient").get("age").getStatus()).isNull();
        verify(caseStore).save(result);
    }
}
