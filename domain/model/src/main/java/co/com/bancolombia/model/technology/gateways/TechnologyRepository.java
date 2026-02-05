package co.com.bancolombia.model.technology.gateways;

import co.com.bancolombia.model.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyRepository {
    Mono<Technology> save(Technology technology);

    Mono<Boolean> existsByName(String name);

    Mono<Technology> findById(Long id);

    Flux<Technology> findAll();

    Flux<Long> findExistingIdsByIds(List<Long> ids);

    Flux<Technology> findByIds(List<Long> ids);

    Mono<Integer> softDeleteByIds(List<Long> ids);

    Mono<Integer> restoreByIds(List<Long> ids);

    Flux<Technology> findActiveByIds(List<Long> ids);

    Flux<Technology> findDeletedByIds(List<Long> ids);
}
