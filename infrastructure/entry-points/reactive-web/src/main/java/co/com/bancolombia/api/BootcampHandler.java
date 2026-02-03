package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.BootcampRequest;
import co.com.bancolombia.api.dto.request.ListBootcampsRequest;
import co.com.bancolombia.api.mapper.BootcampMapper;
import co.com.bancolombia.api.mapper.BootcampListMapper;
import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.usecase.registerbootcamp.RegisterBootcampUseCase;
import co.com.bancolombia.usecase.listbootcamp.ListBootcampsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampHandler {

    private final RegisterBootcampUseCase registerBootcampUseCase;
    private final ListBootcampsUseCase listBootcampsUseCase;
    private final BootcampMapper bootcampMapper;
    private final BootcampListMapper bootcampListMapper;

    public Mono<ServerResponse> registerBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampRequest.class)
            .map(this::buildBootcamp)
            .flatMap(registerBootcampUseCase::execute)
            .map(bootcampMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamp registered successfully"))
            .doOnError(error -> log.error("Error registering bootcamp: {}", error.getMessage()));
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        return extractListParams(request)
            .flatMap(listRequest -> listBootcampsUseCase.execute(
                bootcampListMapper.toPageRequest(listRequest)))
            .map(bootcampListMapper::toPageResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamps listed successfully"))
            .doOnError(error -> log.error("Error listing bootcamps: {}", error.getMessage()));
    }

    private Mono<ListBootcampsRequest> extractListParams(ServerRequest request) {
        return Mono.fromCallable(() -> ListBootcampsRequest.builder()
            .page(request.queryParam("page").map(Integer::parseInt).orElse(0))
            .size(request.queryParam("size").map(Integer::parseInt).orElse(10))
            .sortBy(request.queryParam("sortBy").orElse("name"))
            .sortOrder(request.queryParam("sortOrder").orElse("asc"))
            .build());
    }

    private Bootcamp buildBootcamp(BootcampRequest request) {
        Bootcamp bootcamp = bootcampMapper.toEntity(request);
        var capacities = request.getCapacities().stream()
            .map(capacityId -> Capacity.builder().id(capacityId.longValue()).build())
            .toList();
        bootcamp.setCapacities(capacities);
        return bootcamp;
    }
}
