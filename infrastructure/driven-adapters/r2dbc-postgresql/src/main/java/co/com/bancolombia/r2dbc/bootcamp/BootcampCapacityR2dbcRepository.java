package co.com.bancolombia.r2dbc.bootcamp;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampCapacityR2dbcRepository extends ReactiveCrudRepository<BootcampCapacityData, Long> {

    @Query("SELECT capacity_id FROM bootcamp_capacities WHERE bootcamp_id = :bootcampId")
    Flux<Long> findCapacityIdsByBootcampId(@Param("bootcampId") Long bootcampId);

    @Query("SELECT id, bootcamp_id, capacity_id FROM bootcamp_capacities WHERE bootcamp_id = :bootcampId")
    Flux<BootcampCapacityData> findCapacitiesByBootcampId(@Param("bootcampId") Long bootcampId);

    @Query("DELETE FROM bootcamp_capacities WHERE bootcamp_id = :bootcampId")
    Mono<Void> deleteByBootcampId(@Param("bootcampId") Long bootcampId);

    @Query("SELECT capacity_id, bootcamp_id FROM bootcamp_capacities WHERE capacity_id IN (:capacityIds)")
    Flux<BootcampCapacityData> findBootcampCapacitiesByCapacityIds(@Param("capacityIds") List<Long> capacityIds);
}
