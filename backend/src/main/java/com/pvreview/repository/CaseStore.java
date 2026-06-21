package com.pvreview.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.pvreview.model.Case;

@Repository
public class CaseStore {

    private final Map<String, Case> cases = new ConcurrentHashMap<>();

    public Optional<Case> find(String caseId) {
        return Optional.ofNullable(cases.get(caseId));
    }

    public Case save(Case caseToSave) {
        cases.put(caseToSave.getCaseId(), caseToSave);
        return caseToSave;
    }
}
