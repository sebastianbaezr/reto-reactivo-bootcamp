package co.com.bancolombia.model.technology.gateways;

import co.com.bancolombia.model.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyRepository {
    Mono<Technology> save(Technology technology);

    Mono<Boolean> existsByName(String name);

    Mono<Technology> findById(Long id);

    Flux<Technology> findAll();
}
