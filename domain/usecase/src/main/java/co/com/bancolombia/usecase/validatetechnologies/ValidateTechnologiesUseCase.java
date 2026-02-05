package co.com.bancolombia.usecase.validatetechnologies;

import co.com.bancolombia.model.result.ValidateTechnologiesResult;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ValidateTechnologiesUseCase {
    private final TechnologyRepository technologyRepository;

    public Mono<ValidateTechnologiesResult> execute(List<Long> ids) {
        return Mono.just(ids)
                .filter(list -> list != null && !list.isEmpty())
                .flatMap(this::validateAndBuildResult)
                .switchIfEmpty(Mono.fromCallable(this::buildEmptyResult));
    }

    private Mono<ValidateTechnologiesResult> validateAndBuildResult(List<Long> ids) {
        return technologyRepository.findExistingIdsByIds(ids)
                .collect(Collectors.toSet())
                .map(existingIds -> buildResult(ids, existingIds));
    }

    private ValidateTechnologiesResult buildEmptyResult() {
        return ValidateTechnologiesResult.builder()
                .existingIds(List.of())
                .notFoundIds(List.of())
                .allExist(true)
                .build();
    }

    private ValidateTechnologiesResult buildResult(List<Long> requestedIds, Set<Long> existingIds) {
        List<Long> notFoundIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        return ValidateTechnologiesResult.builder()
                .existingIds(List.copyOf(existingIds))
                .notFoundIds(notFoundIds)
                .allExist(notFoundIds.isEmpty())
                .build();
    }
}
