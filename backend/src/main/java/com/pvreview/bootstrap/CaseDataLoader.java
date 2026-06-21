package com.pvreview.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvreview.dto.CasePayload;
import com.pvreview.service.CaseService;

@Component
public class CaseDataLoader implements ApplicationRunner {

    private final ObjectMapper objectMapper;
    private final CaseService caseService;
    private final Resource caseDataResource;

    public CaseDataLoader(ObjectMapper objectMapper,
                           CaseService caseService,
                           @Value("${app.bootstrap.case-data-path}") Resource caseDataResource) {
        this.objectMapper = objectMapper;
        this.caseService = caseService;
        this.caseDataResource = caseDataResource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        CasePayload payload = objectMapper.readValue(caseDataResource.getInputStream(), CasePayload.class);
        caseService.bootstrap(payload);
    }
}
