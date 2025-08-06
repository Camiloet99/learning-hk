package org.inventorysystem.storeservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final EnvironmentConfig config;

    @Bean
    public WebClient orderClient(WebClient.Builder builder) {
        return builder
                .baseUrl(config.getDomains().getOrder())
                .build();
    }
}

