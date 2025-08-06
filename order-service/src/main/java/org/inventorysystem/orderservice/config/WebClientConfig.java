package org.inventorysystem.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final EnvironmentConfig environmentConfig;

    @Bean
    public WebClient inventoryClient(WebClient.Builder builder) {
        return builder
                .baseUrl(environmentConfig.getDomains().getInventory())
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.createException()
                                .flatMap(Mono::error);
                    }
                    return Mono.just(clientResponse);
                }))
                .build();
    }

    @Bean
    public Retry inventoryRetrySpec() {
        return Retry.backoff(
                        environmentConfig.getRetry().getMaxAttempts(),
                        Duration.ofMillis(environmentConfig.getRetry().getDelayMs()))
                .filter(throwable -> !(throwable instanceof IllegalArgumentException))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        new RuntimeException("Max retries exceeded for inventory service", retrySignal.failure()));
    }
}