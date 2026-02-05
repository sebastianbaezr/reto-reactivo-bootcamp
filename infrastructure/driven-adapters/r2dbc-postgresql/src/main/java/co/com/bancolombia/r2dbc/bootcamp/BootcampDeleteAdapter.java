package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.model.bootcamp.gateways.BootcampDeleteGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for bootcamp deletion operations.
 * Handles local database operations for bootcamp deletion and restoration.
 * All deletion operations are transactional to ensure consistency.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BootcampDeleteAdapter implements BootcampDeleteGateway {

    private final BootcampR2dbcRepository bootcampRepository;
    private final BootcampCapacityR2dbcRepository bootcampCapacityRepository;

    @Override
    public Mono<BootcampDeleteInfo> getBootcampInfo(Long bootcampId) {
        log.debug("Fetching bootcamp info for deletion: bootcampId: {}", bootcampId);

        return bootcampRepository.findActiveById(bootcampId)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_NOT_FOUND)))
                .zipWith(bootcampRepository.findCapacityIdsByBootcampId(bootcampId).collectList())
                .map(tuple -> {
                    BootcampData bootcamp = tuple.getT1();
                    List<Long> capacityIds = tuple.getT2();
                    return (BootcampDeleteInfo) new BootcampDeleteInfoImpl(
                            bootcamp.getId(),
                            bootcamp.getName(),
                            capacityIds != null ? capacityIds : List.of(),
                            List.of()
                    );
                })
                .doOnNext(info -> log.debug("Bootcamp info retrieved: id={}, capacities={}",
                        info.getId(), info.getCapacityIds().size()));
    }

    @Override
    @Transactional
    public Mono<Void> deleteBootcamp(Long bootcampId) {
        log.info("Soft deleting bootcamp transactionally: bootcampId: {}", bootcampId);

        return bootcampRepository.softDeleteById(bootcampId)
                .doOnSuccess(rowsUpdated -> log.info("Bootcamp soft deleted successfully: bootcampId: {}, rowsUpdated: {}",
                        bootcampId, rowsUpdated))
                .then()
                .onErrorResume(error -> {
                    log.error("Error soft deleting bootcamp: bootcampId: {}", bootcampId, error);
                    return Mono.error(new BusinessException(
                            DomainErrorCode.BOOTCAMP_DELETE_FAILED,
                            error.getMessage()
                    ));
                });
    }

    @Override
    @Transactional
    public Mono<Void> restoreBootcamp(Long bootcampId) {
        log.info("Restoring soft deleted bootcamp: bootcampId: {}", bootcampId);

        return bootcampRepository.restoreById(bootcampId)
                .doOnSuccess(rowsUpdated -> log.info("Bootcamp restored successfully: bootcampId: {}, rowsUpdated: {}",
                        bootcampId, rowsUpdated))
                .then()
                .onErrorResume(error -> {
                    log.error("Error restoring bootcamp: bootcampId: {}", bootcampId, error);
                    return Mono.error(new BusinessException(
                            DomainErrorCode.BOOTCAMP_DELETE_FAILED,
                            error.getMessage()
                    ));
                });
    }

    @Override
    public Mono<Boolean> existsById(Long bootcampId) {
        return bootcampRepository.existsActiveById(bootcampId)
                .doOnNext(exists -> log.debug("Bootcamp exists check: bootcampId: {}, exists: {}", bootcampId, exists));
    }

    /**
     * Implementation of BootcampDeleteInfo interface.
     */
    private static class BootcampDeleteInfoImpl implements BootcampDeleteInfo {
        private final Long id;
        private final String name;
        private final List<Long> capacityIds;
        private final List<Long> technologyIds;

        public BootcampDeleteInfoImpl(Long id, String name, List<Long> capacityIds, List<Long> technologyIds) {
            this.id = id;
            this.name = name;
            this.capacityIds = capacityIds;
            this.technologyIds = technologyIds;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<Long> getCapacityIds() {
            return capacityIds;
        }

        @Override
        public List<Long> getTechnologyIds() {
            return technologyIds;
        }
    }
}
