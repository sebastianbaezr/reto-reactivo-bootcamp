package co.com.bancolombia.r2dbc.bootcamp;

import org.springframework.data.r2dbc.repository.Modifying;
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

    @Modifying
    @Query("UPDATE bootcamps SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND deleted_at IS NULL")
    Mono<Integer> softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE bootcamps SET deleted_at = NULL WHERE id = :id AND deleted_at IS NOT NULL")
    Mono<Integer> restoreById(@Param("id") Long id);

    @Query("SELECT * FROM bootcamps WHERE id = :id AND deleted_at IS NULL")
    Mono<BootcampData> findActiveById(@Param("id") Long id);

    @Query("SELECT EXISTS(SELECT 1 FROM bootcamps WHERE id = :id AND deleted_at IS NULL)")
    Mono<Boolean> existsActiveById(@Param("id") Long id);

    @Query("SELECT capacity_id FROM bootcamp_capacities WHERE bootcamp_id = :bootcampId")
    Flux<Long> findCapacityIdsByBootcampId(@Param("bootcampId") Long bootcampId);

    @Query("SELECT * FROM bootcamps WHERE id = ANY(:ids) AND deleted_at IS NULL")
    Flux<BootcampData> findByIds(@Param("ids") Long[] ids);
}
