package com.pvreview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pvReviewOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PV Case Review API")
                        .description("Pharmacovigilance case review service: merges follow-up reports "
                                + "into stored cases and tracks reviewer queries against individual fields.")
                        .version("v0.0.1"));
    }

    // Without this, swagger-core resolves schema property names from raw Java field
    // names (camelCase) instead of the app's actual snake_case wire format, since it
    // otherwise builds its own ObjectMapper instead of using the Spring-configured one.
    @Bean
    public ModelConverter modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}
