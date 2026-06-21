package com.pvreview.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pvreview.dto.QueryRequest;
import com.pvreview.model.Query;
import com.pvreview.repository.QueryStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final QueryStore queryStore;
    private final CaseService caseService;

    public Query createQuery(QueryRequest request) {
        caseService.getCase(request.getCaseId());

        Query query = Query.builder()
                .id(UUID.randomUUID().toString())
                .caseId(request.getCaseId())
                .fieldPath(request.getFieldPath())
                .question(request.getQuestion())
                .createdAt(Instant.now())
                .build();

        return queryStore.save(query);
    }

    public List<Query> getQueriesForCase(String caseId) {
        return queryStore.findByCaseId(caseId);
    }
}
