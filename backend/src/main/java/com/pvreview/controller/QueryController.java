package com.pvreview.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pvreview.dto.QueryRequest;
import com.pvreview.model.Query;
import com.pvreview.service.QueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/queries")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @PostMapping
    public ResponseEntity<Query> createQuery(@Valid @RequestBody QueryRequest request) {
        Query query = queryService.createQuery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(query);
    }

    @GetMapping
    public ResponseEntity<List<Query>> getQueries(@RequestParam String caseId) {
        return ResponseEntity.ok(queryService.getQueriesForCase(caseId));
    }
}
