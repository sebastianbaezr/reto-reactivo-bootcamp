package co.com.bancolombia.r2dbc.technology;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyR2dbcRepository extends ReactiveCrudRepository<TechnologyData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM technologies WHERE name = :name AND deleted_at IS NULL)")
    Mono<Boolean> existsByName(@Param("name") String name);

    @Query("SELECT id FROM technologies WHERE id = ANY(:ids) AND deleted_at IS NULL")
    Flux<Long> findExistingIdsByIds(@Param("ids") Long[] ids);

    @Query("SELECT * FROM technologies WHERE id = ANY(:ids) AND deleted_at IS NULL ORDER BY id")
    Flux<TechnologyData> findByIds(@Param("ids") Long[] ids);

    @Modifying
    @Query("UPDATE technologies SET deleted_at = CURRENT_TIMESTAMP WHERE id = ANY(:ids) AND deleted_at IS NULL")
    Mono<Integer> softDeleteByIds(@Param("ids") Long[] ids);

    @Modifying
    @Query("UPDATE technologies SET deleted_at = NULL WHERE id = ANY(:ids) AND deleted_at IS NOT NULL")
    Mono<Integer> restoreByIds(@Param("ids") Long[] ids);

    @Query("SELECT * FROM technologies WHERE id = ANY(:ids) AND deleted_at IS NULL ORDER BY id")
    Flux<TechnologyData> findActiveByIds(@Param("ids") Long[] ids);

    @Query("SELECT * FROM technologies WHERE id = ANY(:ids) AND deleted_at IS NOT NULL ORDER BY id")
    Flux<TechnologyData> findDeletedByIds(@Param("ids") Long[] ids);
}
