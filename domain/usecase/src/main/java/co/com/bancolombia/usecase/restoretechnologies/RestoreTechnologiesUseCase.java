package co.com.bancolombia.usecase.restoretechnologies;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.result.RestoreTechnologiesResult;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestoreTechnologiesUseCase {
    private final TechnologyRepository technologyRepository;

    public Mono<RestoreTechnologiesResult> execute(List<Long> ids) {
        return Mono.just(ids)
                .filter(list -> !list.isEmpty())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.EMPTY_IDS_LIST)))
                .flatMap(this::validateAllIdsDeleted);
    }

    private Mono<RestoreTechnologiesResult> validateAllIdsDeleted(List<Long> ids) {
        return technologyRepository.findDeletedByIds(ids)
                .map(Technology::getId)
                .collect(Collectors.toSet())
                .filterWhen(foundIds -> validateFoundIdsMatch(foundIds, ids))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_NOT_DELETED)))
                .flatMap(foundIds -> performRestore(ids));
    }

    private Mono<Boolean> validateFoundIdsMatch(Set<Long> foundIds, List<Long> requestedIds) {
        Set<Long> requested = new java.util.HashSet<>(requestedIds);
        return Mono.just(foundIds.equals(requested));
    }

    private Mono<RestoreTechnologiesResult> performRestore(List<Long> ids) {
        return technologyRepository.restoreByIds(ids)
                .map(restoredCount -> new RestoreTechnologiesResult(restoredCount, ids));
    }
}
