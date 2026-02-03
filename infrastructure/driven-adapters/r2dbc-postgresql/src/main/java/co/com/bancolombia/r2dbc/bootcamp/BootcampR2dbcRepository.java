package co.com.bancolombia.r2dbc.bootcamp;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BootcampR2dbcRepository extends ReactiveCrudRepository<BootcampData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM bootcamps WHERE name = :name)")
    Mono<Boolean> existsByName(@Param("name") String name);
}
