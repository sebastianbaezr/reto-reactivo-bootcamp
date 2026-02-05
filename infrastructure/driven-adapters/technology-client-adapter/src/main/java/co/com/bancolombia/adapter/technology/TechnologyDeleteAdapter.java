package co.com.bancolombia.adapter.technology;

import co.com.bancolombia.adapter.technology.dto.TechnologyBatchDeleteRequest;
import co.com.bancolombia.adapter.technology.dto.TechnologyBatchDeleteResponse;
import co.com.bancolombia.adapter.technology.dto.TechnologyBatchRestoreRequest;
import co.com.bancolombia.adapter.technology.dto.TechnologyBatchRestoreResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyDeleteGateway;
import co.com.bancolombia.webclient.config.WebClientFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TechnologyDeleteAdapter implements TechnologyDeleteGateway {

    private static final String CAPACITY_SERVICE = "capacity-service";
    private static final String TECHNOLOGY_SERVICE = "technology-service";
    private static final String TECHNOLOGY_CAPACITY_COUNTS_PATH = "/api/technologies/capacity-counts";
    private static final String DELETE_PATH = "/api/technologies/batch";
    private static final String RESTORE_PATH = "/api/technologies/restore-batch";
    private static final int TIMEOUT_SECONDS = 10;

    private final WebClient capacityServiceWebClient;
    private final WebClient technologyServiceWebClient;

    public TechnologyDeleteAdapter(WebClientFactory webClientFactory) {
        this.capacityServiceWebClient = webClientFactory.getWebClient(CAPACITY_SERVICE);
        this.technologyServiceWebClient = webClientFactory.getWebClient(TECHNOLOGY_SERVICE);
    }

    @Override
    public Mono<Map<Long, Integer>> getCapacityCountForTechnologies(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        TechnologyCapacityCountsRequest request = new TechnologyCapacityCountsRequest(technologyIds);

        return capacityServiceWebClient.post()
                .uri(TECHNOLOGY_CAPACITY_COUNTS_PATH)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(TechnologyCapacityCountsResponse.class)
                .map(TechnologyCapacityCountsResponse::getCounts)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(this::handleRequestError);
    }

    @Override
    public Mono<Void> deleteTechnologies(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Mono.empty();
        }

        TechnologyBatchDeleteRequest request = TechnologyBatchDeleteRequest.builder()
                .ids(technologyIds)
                .build();

        return technologyServiceWebClient.method(HttpMethod.DELETE)
                .uri(DELETE_PATH)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Saga-ID", generateSagaId())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.CONFLICT),
                        response -> Mono.error(new BusinessException(
                                DomainErrorCode.TECHNOLOGY_IN_USE,
                                "One or more technologies are in use"
                        )))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(TechnologyBatchDeleteResponse.class)
                .then()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(this::handleRequestError);
    }

    @Override
    public Mono<Void> restoreTechnologies(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Mono.empty();
        }

        TechnologyBatchRestoreRequest request = TechnologyBatchRestoreRequest.builder()
                .ids(technologyIds)
                .build();

        return technologyServiceWebClient.post()
                .uri(RESTORE_PATH)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Saga-ID", generateSagaId())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(TechnologyBatchRestoreResponse.class)
                .then()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(this::handleRequestError);
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .doOnNext(body -> log.error("Service error response: {}", body))
                .then(Mono.error(new BusinessException(
                        DomainErrorCode.TECHNOLOGY_DELETE_FAILED,
                        "Service returned an error"
                )));
    }

    private <T> Mono<T> handleRequestError(Throwable error) {
        log.error("Service request error", error);
        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(
                    DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE,
                    "Service is unavailable"
            ));
        }
        return Mono.error(error);
    }

    private String generateSagaId() {
        return "saga-" + UUID.randomUUID();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyCapacityCountsRequest {
        private List<Long> technologyIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyCapacityCountsResponse {
        @JsonProperty("technology_counts")
        private Map<String, Integer> technology_counts;

        public Map<Long, Integer> getCounts() {
            if (technology_counts == null) {
                return Map.of();
            }
            return technology_counts.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> Long.parseLong(e.getKey()),
                    Map.Entry::getValue
                ));
        }
    }
}
