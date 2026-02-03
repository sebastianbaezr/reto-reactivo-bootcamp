package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.BootcampRequest;
import co.com.bancolombia.api.mapper.BootcampMapper;
import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.usecase.registerbootcamp.RegisterBootcampUseCase;
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
    private final BootcampMapper bootcampMapper;

    public Mono<ServerResponse> registerBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampRequest.class)
            .map(this::buildBootcamp)
            .flatMap(registerBootcampUseCase::execute)
            .map(bootcampMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamp registered successfully"))
            .doOnError(error -> log.error("Error registering bootcamp: {}", error.getMessage()));
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
