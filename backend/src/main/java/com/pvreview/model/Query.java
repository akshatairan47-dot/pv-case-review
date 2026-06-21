package com.pvreview.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Query {

    private String id;
    private String caseId;
    private String fieldPath;
    private String question;
    private Instant createdAt;
}
