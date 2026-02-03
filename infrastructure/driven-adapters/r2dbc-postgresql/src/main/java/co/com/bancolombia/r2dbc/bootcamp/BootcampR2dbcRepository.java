package co.com.bancolombia.r2dbc.bootcamp;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampR2dbcRepository extends ReactiveCrudRepository<BootcampData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM bootcamps WHERE name = :name)")
    Mono<Boolean> existsByName(@Param("name") String name);

    @Query("SELECT COUNT(*) FROM bootcamps")
    Mono<Long> countAll();

    @Query("""
        SELECT b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        FROM bootcamps b
        ORDER BY b.name ASC
        LIMIT :limit OFFSET :offset
        """)
    Flux<BootcampData> findAllOrderByNameAsc(
        @Param("limit") int limit,
        @Param("offset") long offset);

    @Query("""
        SELECT b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        FROM bootcamps b
        ORDER BY b.name DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<BootcampData> findAllOrderByNameDesc(
        @Param("limit") int limit,
        @Param("offset") long offset);

    @Query("""
        SELECT b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        FROM bootcamps b
        LEFT JOIN bootcamp_capacities bc ON b.id = bc.bootcamp_id
        GROUP BY b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        ORDER BY COUNT(bc.capacity_id) ASC, b.name ASC
        LIMIT :limit OFFSET :offset
        """)
    Flux<BootcampData> findAllOrderByCapacityCountAsc(
        @Param("limit") int limit,
        @Param("offset") long offset);

    @Query("""
        SELECT b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        FROM bootcamps b
        LEFT JOIN bootcamp_capacities bc ON b.id = bc.bootcamp_id
        GROUP BY b.id, b.name, b.description, b.release_date, b.duration, b.created_at, b.updated_at
        ORDER BY COUNT(bc.capacity_id) DESC, b.name ASC
        LIMIT :limit OFFSET :offset
        """)
    Flux<BootcampData> findAllOrderByCapacityCountDesc(
        @Param("limit") int limit,
        @Param("offset") long offset);
}
