package co.com.bancolombia.r2dbc.technology;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TechnologyR2dbcRepository extends ReactiveCrudRepository<TechnologyData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM technologies WHERE name = :name)")
    Mono<Boolean> existsByName(@Param("name") String name);
}
