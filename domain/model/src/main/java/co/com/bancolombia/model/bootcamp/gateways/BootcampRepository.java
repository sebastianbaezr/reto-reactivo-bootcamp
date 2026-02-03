package co.com.bancolombia.model.bootcamp.gateways;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampRepository {
    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Boolean> existsByName(String name);

    Mono<Bootcamp> findById(Long id);

    Flux<Bootcamp> findAll();
}
