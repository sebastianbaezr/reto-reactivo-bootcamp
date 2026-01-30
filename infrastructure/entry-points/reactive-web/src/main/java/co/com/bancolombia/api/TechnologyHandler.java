package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.dto.response.ApiResponseData;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.usecase.registertechnology.RegisterTechnologyUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TechnologyHandler {

    private final RegisterTechnologyUseCase registerTechnologyUseCase;
    private final TechnologyMapper technologyMapper;

    public Mono<ServerResponse> registerTechnology(ServerRequest request) {
        return request.bodyToMono(TechnologyRequest.class)
            .map(technologyMapper::toEntity)
            .flatMap(registerTechnologyUseCase::execute)
            .map(technologyMapper::toResponse)
            .map(ApiResponseData::of)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Technology registered successfully"));
    }
}
