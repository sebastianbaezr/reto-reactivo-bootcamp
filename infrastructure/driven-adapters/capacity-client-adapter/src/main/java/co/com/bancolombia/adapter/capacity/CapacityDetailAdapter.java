package co.com.bancolombia.adapter.capacity;

import co.com.bancolombia.model.capacity.CapacityDetail;
import co.com.bancolombia.model.capacity.gateways.CapacityDetailGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class CapacityDetailAdapter implements CapacityDetailGateway {

    private static final String SERVICE_NAME = "capacity-service";
    private static final String GET_BY_IDS_PATH = "/api/capacities/by-ids";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    public CapacityDetailAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
    }

    @Override
    public Flux<CapacityDetail> getCapacitiesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("No capacity IDs provided");
            return Flux.empty();
        }

        String idsParam = ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        log.info("Fetching capacity details for IDs: {}", idsParam);
        System.out.println("[CapacityDetailAdapter] Calling capacity service with IDs: " + idsParam);

        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(GET_BY_IDS_PATH)
                .queryParam("ids", idsParam)
                .build())
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                log.error("Error fetching capacities. Status: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Error response body: {}", body))
                    .then(Mono.error(new BusinessException(
                        response.statusCode().is5xxServerError()
                            ? DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE
                            : DomainErrorCode.CAPACITY_SERVICE_ERROR
                    )));
            })
            .bodyToFlux(CapacityDetail.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnNext(capacity -> {
                log.debug("Fetched capacity: {} with {} technologies",
                    capacity.getName(), capacity.getTechnologies().size());
                System.out.println("[CapacityDetailAdapter] Received capacity: " + capacity.getName() + " with " + capacity.getTechnologies().size() + " technologies");
            })
            .doOnError(error -> log.error("Error fetching capacity details: {}", error.getMessage(), error))
            .onErrorResume(error -> {
                log.error("Capacity detail fetch failed, returning empty", error);
                return Mono.error(new BusinessException(DomainErrorCode.CAPACITY_SERVICE_UNAVAILABLE));
            });
    }
}
