package co.com.bancolombia.adapter.capacity;

import co.com.bancolombia.adapter.capacity.dto.CapacityValidationResponse;
import co.com.bancolombia.model.capacity.gateways.CapacityValidationGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class CapacityValidationAdapter implements CapacityValidationGateway {

    private static final String SERVICE_NAME = "capacity-service";
    private static final String VALIDATE_PATH = "/api/capacities/validate";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    public CapacityValidationAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
    }

    @Override
    public Mono<Boolean> validateCapacities(List<Long> capacityIds) {
        if (capacityIds == null || capacityIds.isEmpty()) {
            log.warn("No capacity IDs provided for validation");
            return Mono.just(false);
        }

        String idsParam = capacityIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        log.info("Starting capacity validation for IDs: {}", idsParam);

        Mono<Boolean> result = webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(VALIDATE_PATH)
                .queryParam("ids", idsParam)
                .build())
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                log.error("Error validating capacities. Status: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Error response body: {}", body))
                    .then(Mono.error(new BusinessException(
                        response.statusCode().is5xxServerError()
                            ? DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE
                            : DomainErrorCode.CAPACITY_SERVICE_ERROR
                    )));
            })
            .bodyToMono(CapacityValidationResponse.class)
            .doOnNext(response -> {
                log.info("=== CAPACITY VALIDATION RESPONSE ===");
                log.info("Full response: {}", response);
                log.info("allExist: {}", response.getAllExist());
                log.info("existingIds: {}", response.getExistingIds());
                log.info("notFoundIds: {}", response.getNotFoundIds());
                log.info("=====================================");
            })
            .map(CapacityValidationResponse::getAllExist)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnSuccess(valid -> log.info("Final validation result: {}", valid))
            .doOnError(error -> log.error("Capacity validation error: {}", error.getMessage(), error))
            .onErrorResume(this::handleRequestError);

        log.debug("Capacity validation Mono created");
        return result;
    }


    private Mono<Boolean> handleRequestError(Throwable error) {
        log.error("!!! REQUEST ERROR during capacity validation !!!", error);
        if (error instanceof WebClientRequestException) {
            log.error("Connection error to capacity service", error);
            return Mono.error(new BusinessException(DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE));
        }
        return Mono.error(error);
    }
}
