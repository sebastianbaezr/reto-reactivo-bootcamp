package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.BatchOperationRequest;
import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.usecase.gettechnologiesbyids.GetTechnologiesByIdsUseCase;
import co.com.bancolombia.usecase.registertechnology.RegisterTechnologyUseCase;
import co.com.bancolombia.usecase.restoretechnologies.RestoreTechnologiesUseCase;
import co.com.bancolombia.usecase.softdeletetechnologies.SoftDeleteTechnologiesUseCase;
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
    private final SoftDeleteTechnologiesUseCase softDeleteTechnologiesUseCase;
    private final RestoreTechnologiesUseCase restoreTechnologiesUseCase;
    private final TechnologyMapper technologyMapper;

    public Mono<ServerResponse> registerTechnology(ServerRequest request) {
        return request.bodyToMono(TechnologyRequest.class)
            .map(technologyMapper::toDomain)
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

    public Mono<ServerResponse> softDeleteTechnologies(ServerRequest request) {
        return request.bodyToMono(BatchOperationRequest.class)
            .flatMap(req -> softDeleteTechnologiesUseCase.execute(req.getIds()))
            .map(technologyMapper::toSoftDeleteResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Technologies soft-deleted"));
    }

    public Mono<ServerResponse> restoreTechnologies(ServerRequest request) {
        return request.bodyToMono(BatchOperationRequest.class)
            .flatMap(req -> restoreTechnologiesUseCase.execute(req.getIds()))
            .map(technologyMapper::toRestoreResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Technologies restored"));
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
