package co.com.bancolombia.usecase.softdeletetechnologies;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.result.SoftDeleteTechnologiesResult;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SoftDeleteTechnologiesUseCase {
    private final TechnologyRepository technologyRepository;

    public Mono<SoftDeleteTechnologiesResult> execute(List<Long> ids) {
        return Mono.just(ids)
                .filter(list -> !list.isEmpty())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.EMPTY_IDS_LIST)))
                .flatMap(this::validateAllIdsExist);
    }

    private Mono<SoftDeleteTechnologiesResult> validateAllIdsExist(List<Long> ids) {
        return technologyRepository.findActiveByIds(ids)
                .map(Technology::getId)
                .collect(Collectors.toSet())
                .filterWhen(foundIds -> validateFoundIdsMatch(foundIds, ids))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_NOT_FOUND)))
                .flatMap(foundIds -> performSoftDelete(ids));
    }

    private Mono<Boolean> validateFoundIdsMatch(Set<Long> foundIds, List<Long> requestedIds) {
        return Mono.just(foundIds.equals(new java.util.HashSet<>(requestedIds)));
    }

    private Mono<SoftDeleteTechnologiesResult> performSoftDelete(List<Long> ids) {
        return technologyRepository.softDeleteByIds(ids)
                .map(deletedCount -> new SoftDeleteTechnologiesResult(deletedCount, ids));
    }
}
