package com.pvreview.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.pvreview.model.Query;

@Repository
public class QueryStore {

    private final Map<String, List<Query>> queriesByCaseId = new ConcurrentHashMap<>();

    public Query save(Query query) {
        queriesByCaseId
                .computeIfAbsent(query.getCaseId(), key -> new CopyOnWriteArrayList<>())
                .add(query);
        return query;
    }

    public List<Query> findByCaseId(String caseId) {
        return List.copyOf(queriesByCaseId.getOrDefault(caseId, List.of()));
    }
}
