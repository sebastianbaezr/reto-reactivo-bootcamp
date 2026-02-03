package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.usecase.gettechnologiesbyids.GetTechnologiesByIdsUseCase;
import co.com.bancolombia.usecase.registertechnology.RegisterTechnologyUseCase;
import co.com.bancolombia.usecase.validatetechnologies.ValidateTechnologiesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TechnologyHandler {

    private final RegisterTechnologyUseCase registerTechnologyUseCase;
    private final ValidateTechnologiesUseCase validateTechnologiesUseCase;
    private final GetTechnologiesByIdsUseCase getTechnologiesByIdsUseCase;
    private final TechnologyMapper technologyMapper;

    public Mono<ServerResponse> registerTechnology(ServerRequest request) {
        return request.bodyToMono(TechnologyRequest.class)
            .map(technologyMapper::toEntity)
            .flatMap(registerTechnologyUseCase::execute)
            .map(technologyMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Technology registered successfully"));
    }

    public Mono<ServerResponse> validateTechnologies(ServerRequest request) {
        return Mono.fromCallable(() -> extractIds(request))
            .flatMap(validateTechnologiesUseCase::execute)
            .map(technologyMapper::toValidateResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Technologies validated successfully"));
    }

    public Mono<ServerResponse> getTechnologiesByIds(ServerRequest request) {
        return Mono.fromCallable(() -> extractIds(request))
            .flatMapMany(getTechnologiesByIdsUseCase::execute)
            .map(technologyMapper::toSimpleResponse)
            .collectList()
            .flatMap(technologies -> ServerResponse.ok().bodyValue(technologies))
            .doOnSuccess(v -> log.info("Technologies retrieved successfully"));
    }

    private List<Long> extractIds(ServerRequest request) {
        String idsParam = request.queryParam("ids").orElse("");
        if (idsParam.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(idsParam.split(","))
            .map(String::trim)
            .map(Long::parseLong)
            .toList();
    }
}
