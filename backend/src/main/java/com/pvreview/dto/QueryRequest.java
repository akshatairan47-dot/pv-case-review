package com.pvreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    @NotBlank(message = "is required")
    private String caseId;

    @NotBlank(message = "is required")
    private String fieldPath;

    @NotBlank(message = "is required")
    private String question;
}
