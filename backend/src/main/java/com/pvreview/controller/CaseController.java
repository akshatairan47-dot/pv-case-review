package com.pvreview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pvreview.dto.CasePayload;
import com.pvreview.model.Case;
import com.pvreview.service.CaseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @GetMapping("/{caseId}")
    public ResponseEntity<Case> getCase(@PathVariable String caseId) {
        return ResponseEntity.ok(caseService.getCase(caseId));
    }

    @PostMapping("/{caseId}/follow-ups")
    public ResponseEntity<Case> applyFollowUp(@PathVariable String caseId, @RequestBody CasePayload payload) {
        return ResponseEntity.ok(caseService.applyFollowUp(caseId, payload));
    }
}
