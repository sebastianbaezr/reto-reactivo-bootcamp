package co.com.bancolombia.adapter.capacity;

import co.com.bancolombia.adapter.capacity.dto.CapacityBatchDeleteRequest;
import co.com.bancolombia.adapter.capacity.dto.CapacityBatchDeleteResponse;
import co.com.bancolombia.adapter.capacity.dto.CapacityBatchRestoreRequest;
import co.com.bancolombia.adapter.capacity.dto.CapacityBatchRestoreResponse;
import co.com.bancolombia.adapter.capacity.dto.CapacityTechnologiesResponse;
import co.com.bancolombia.adapter.capacity.dto.TechnologyCapacityCountsRequest;
import co.com.bancolombia.adapter.capacity.dto.TechnologyCapacityCountsResponse;
import co.com.bancolombia.adapter.capacity.dto.TechnologyCapacityCountResponse;
import co.com.bancolombia.model.capacity.gateways.CapacityDeleteGateway;
import co.com.bancolombia.model.capacity.gateways.BootcampCapacityCountGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@Slf4j
public class CapacityDeleteAdapter implements CapacityDeleteGateway {

    private static final String SERVICE_NAME = "capacity-service";
    private static final String DELETE_PATH = "/api/capacities/batch";
    private static final String RESTORE_PATH = "/api/capacities/restore-batch";
    private static final String TECHNOLOGY_COUNTS_PATH = "/api/technologies/capacity-counts";
    private static final int TIMEOUT_SECONDS = 10;

    private final WebClient webClient;
    private final BootcampCapacityCountGateway bootcampCapacityCountGateway;

    public CapacityDeleteAdapter(WebClientFactory webClientFactory,
                                  BootcampCapacityCountGateway bootcampCapacityCountGateway) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
        this.bootcampCapacityCountGateway = bootcampCapacityCountGateway;
    }

    @Override
    public Mono<Map<Long, Integer>> getBootcampCountForCapacities(List<Long> capacityIds) {
        return bootcampCapacityCountGateway.getBootcampCountForCapacities(capacityIds);
    }

    @Override
    public Mono<Map<Long, Integer>> getCapacityCountForTechnologies(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return Flux.fromIterable(technologyIds)
                .flatMap(technologyId ->
                    webClient.get()
                            .uri("/api/technologies/{technologyId}/capacity-count", technologyId)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    this::handleErrorResponse)
                            .bodyToMono(TechnologyCapacityCountResponse.class)
                            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                            .onErrorResume(this::handleRequestError)
                )
                .collect(
                    () -> new java.util.HashMap<Long, Integer>(),
                    (map, response) -> map.put(response.getTechnologyId(), response.getCapacityCount())
                )
                .map(map -> (Map<Long, Integer>) map)
                .defaultIfEmpty(Map.of());
    }

    public Mono<List<Long>> getTechnologiesByCapacityId(Long capacityId) {
        if (capacityId == null) {
            return Mono.just(List.of());
        }

        return webClient.get()
                .uri("/api/capacities/{capacityId}/technologies", capacityId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(CapacityTechnologiesResponse.class)
                .map(response -> response.getTechnologies().stream()
                        .map(CapacityTechnologiesResponse.TechnologyInfo::getId)
                        .toList())
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(error -> {
                    log.error("Error getting technologies for capacity: {}", capacityId, error);
                    return Mono.just(List.of());
                });
    }

    @Override
    public Mono<Void> deleteCapacities(List<Long> capacityIds) {
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.empty();
        }

        CapacityBatchDeleteRequest request = CapacityBatchDeleteRequest.builder()
                .capacityIds(capacityIds)
                .reason("Bootcamp deletion")
                .build();

        return webClient.method(HttpMethod.DELETE)
                .uri(DELETE_PATH)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Saga-ID", generateSagaId())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.CONFLICT),
                        response -> Mono.error(new BusinessException(
                                DomainErrorCode.CAPACITY_IN_USE,
                                "One or more capacities are in use"
                        )))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(CapacityBatchDeleteResponse.class)
                .then()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(this::handleRequestError);
    }

    @Override
    public Mono<Void> restoreCapacities(List<Long> capacityIds) {
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.empty();
        }

        CapacityBatchRestoreRequest request = CapacityBatchRestoreRequest.builder()
                .capacityIds(capacityIds)
                .reason("Rollback")
                .build();

        return webClient.post()
                .uri(RESTORE_PATH)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Saga-ID", generateSagaId())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleErrorResponse)
                .bodyToMono(CapacityBatchRestoreResponse.class)
                .then()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .onErrorResume(this::handleRequestError);
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .doOnNext(body -> log.error("Capacity service error response: {}", body))
                .then(Mono.error(new BusinessException(
                        DomainErrorCode.CAPACITY_SERVICE_ERROR,
                        "Capacity service returned an error"
                )));
    }

    private <T> Mono<T> handleRequestError(Throwable error) {
        log.error("Capacity service request error", error);
        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(
                    DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE,
                    "Capacity service is unavailable"
            ));
        }
        return Mono.error(error);
    }

    private String generateSagaId() {
        return "saga-" + UUID.randomUUID();
    }
}
