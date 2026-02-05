package co.com.bancolombia.usecase.deletebootcamp;

import co.com.bancolombia.model.bootcamp.DeleteBootcampSaga;
import co.com.bancolombia.model.bootcamp.gateways.BootcampDeleteGateway;
import co.com.bancolombia.model.capacity.gateways.CapacityDeleteGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyDeleteGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for deleting a bootcamp with cascading deletion of unused capacities and technologies.
 * Implements the Saga Pattern with orchestrated steps and automatic rollback on failure.
 * Uses soft delete (logical deletion) for all entities.
 *
 * Execution flow:
 * 1. Validate bootcamp exists
 * 2. Get bootcamp information (capacity and technology IDs)
 * 3. Soft delete bootcamp (marks as deleted, doesn't remove data)
 * 4. Filter capacities (only delete if bootcamp count <= 1)
 * 5. Soft delete capacities via Capacity Service (with retry)
 * 6. Filter technologies (only delete if capacity count <= 1, calls Capacity Service)
 * 7. Soft delete technologies via Technology Service (with retry)
 * 8. Complete saga
 *
 * If any step fails, rollback bootcamp, capacities, and technologies by restoring them.
 */
@RequiredArgsConstructor
public class DeleteBootcampUseCase {

    private static final String LOG_PREFIX = "[DeleteBootcamp]";

    private final BootcampDeleteGateway bootcampDeleteGateway;
    private final CapacityDeleteGateway capacityDeleteGateway;
    private final TechnologyDeleteGateway technologyDeleteGateway;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final long MAX_BACKOFF_MS = 2000;

    /**
     * Executes the bootcamp deletion saga.
     *
     * @param bootcampId the ID of the bootcamp to delete
     * @return mono containing the final saga state
     */
    public Mono<DeleteBootcampSaga> execute(Long bootcampId) {
        System.out.println("========== INICIANDO ELIMINACIÓN DE BOOTCAMP ==========");
        System.out.println(LOG_PREFIX + " bootcampId: " + bootcampId);

        return validateBootcampExists(bootcampId)
                .flatMap(exists -> getBootcampInfo(bootcampId))
                .flatMap(this::initializeSaga)
                .flatMap(this::deleteBootcampStep)
                .flatMap(this::identifyCapacitiesToDelete)
                .flatMap(this::deleteCapacitiesStep)
                .flatMap(this::identifyTechnologiesToDelete)
                .flatMap(this::deleteTechnologiesStep)
                .flatMap(this::completeSaga)
                .doOnSuccess(saga -> System.out.println("========== ELIMINACIÓN COMPLETADA EXITOSAMENTE ==========\n"))
                .onErrorResume(error -> handleSagaError(error, bootcampId));
    }

    private Mono<Boolean> validateBootcampExists(Long bootcampId) {
        System.out.println(LOG_PREFIX + " PASO 1: Validando existencia del bootcamp...");
        return bootcampDeleteGateway.existsById(bootcampId)
                .flatMap(exists -> {
                    if (exists) {
                        System.out.println(LOG_PREFIX + " ✅ Bootcamp EXISTE y está activo");
                        return Mono.just(true);
                    }
                    System.out.println(LOG_PREFIX + " ❌ Bootcamp NO EXISTE o está eliminado");
                    return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_NOT_FOUND));
                });
    }

    private Mono<BootcampDeleteGateway.BootcampDeleteInfo> getBootcampInfo(Long bootcampId) {
        System.out.println(LOG_PREFIX + " PASO 2: Obteniendo información del bootcamp...");
        return bootcampDeleteGateway.getBootcampInfo(bootcampId)
                .doOnSuccess(info -> {
                    System.out.println(LOG_PREFIX + " ✅ Info obtenida:");
                    System.out.println(LOG_PREFIX + "    - Nombre: " + info.getName());
                    System.out.println(LOG_PREFIX + "    - Capacidades relacionadas: " + info.getCapacityIds().size() + " [IDs: " + info.getCapacityIds() + "]");
                    System.out.println(LOG_PREFIX + "    - Tecnologías relacionadas: " + info.getTechnologyIds().size() + " [IDs: " + info.getTechnologyIds() + "]");
                });
    }

    private Mono<DeleteBootcampSaga> initializeSaga(BootcampDeleteGateway.BootcampDeleteInfo info) {
        System.out.println(LOG_PREFIX + " PASO 3: Inicializando SAGA...");
        DeleteBootcampSaga saga = DeleteBootcampSaga.builder()
                .bootcampId(info.getId())
                .bootcampName(info.getName())
                .capacityIds(info.getCapacityIds())
                .technologyIds(info.getTechnologyIds())
                .status(DeleteBootcampSaga.SagaStatus.PENDING)
                .startTime(LocalDateTime.now())
                .build();

        System.out.println(LOG_PREFIX + " ✅ SAGA inicializada: " + saga.getStatus());
        return Mono.just(saga);
    }

    private Mono<DeleteBootcampSaga> deleteBootcampStep(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 4: SOFT DELETE del Bootcamp (ID: " + saga.getBootcampId() + ")...");
        return bootcampDeleteGateway.deleteBootcamp(saga.getBootcampId())
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofMillis(INITIAL_BACKOFF_MS))
                        .maxBackoff(Duration.ofMillis(MAX_BACKOFF_MS)))
                .thenReturn(saga.toBuilder()
                        .status(DeleteBootcampSaga.SagaStatus.BOOTCAMP_DELETED)
                        .bootcampDeleteResult(DeleteBootcampSaga.StepResult.builder()
                                .success(true)
                                .build())
                        .build())
                .doOnSuccess(s -> System.out.println(LOG_PREFIX + " ✅ Bootcamp SOFT DELETED exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + " ❌ Error al eliminar bootcamp: " + error.getMessage());
                    return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_DELETE_FAILED, error.getMessage()));
                });
    }

    private Mono<DeleteBootcampSaga> identifyCapacitiesToDelete(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 5: Identificando capacidades a eliminar...");
        System.out.println(LOG_PREFIX + "    - Total capacidades relacionadas: " + saga.getCapacityIds().size());

        if (saga.getCapacityIds().isEmpty()) {
            System.out.println(LOG_PREFIX + " ✅ No hay capacidades relacionadas");
            return Mono.just(saga);
        }

        return capacityDeleteGateway.getBootcampCountForCapacities(saga.getCapacityIds())
                .map(counts -> {
                    System.out.println(LOG_PREFIX + " 📊 Conteos de bootcamps por capacidad:");
                    counts.forEach((capacityId, count) ->
                            System.out.println(LOG_PREFIX + "    - CapacityID " + capacityId + ": " + count + " bootcamp(s)"));

                    List<Long> capacitiesToDelete = saga.getCapacityIds().stream()
                            .filter(capacityId -> {
                                int count = counts.getOrDefault(capacityId, 0);
                                boolean shouldDelete = count <= 1;
                                System.out.println(LOG_PREFIX + "    - CapacityID " + capacityId + ": count=" + count + ", eliminar=" + shouldDelete);
                                return shouldDelete;
                            })
                            .collect(Collectors.toList());

                    System.out.println(LOG_PREFIX + " ✅ Capacidades a eliminar: " + capacitiesToDelete.size() + " [IDs: " + capacitiesToDelete + "]");
                    return saga.toBuilder()
                            .capacityIds(capacitiesToDelete)
                            .status(DeleteBootcampSaga.SagaStatus.CAPACITIES_DELETING)
                            .build();
                });
    }

    private Mono<DeleteBootcampSaga> deleteCapacitiesStep(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 6: Eliminando capacidades via Capacity Service...");

        if (saga.getCapacityIds().isEmpty()) {
            System.out.println(LOG_PREFIX + " ℹ️ No hay capacidades a eliminar, saltando este paso");
            return Mono.just(saga.toBuilder()
                    .status(DeleteBootcampSaga.SagaStatus.CAPACITIES_DELETED)
                    .capacityDeleteResult(DeleteBootcampSaga.StepResult.builder()
                            .success(true)
                            .build())
                    .build());
        }

        System.out.println(LOG_PREFIX + " 🔄 Llamando a DELETE /api/capacities/batch con IDs: " + saga.getCapacityIds());
        return capacityDeleteGateway.deleteCapacities(saga.getCapacityIds())
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofMillis(INITIAL_BACKOFF_MS))
                        .maxBackoff(Duration.ofMillis(MAX_BACKOFF_MS)))
                .thenReturn(saga.toBuilder()
                        .status(DeleteBootcampSaga.SagaStatus.CAPACITIES_DELETED)
                        .capacityDeleteResult(DeleteBootcampSaga.StepResult.builder()
                                .success(true)
                                .build())
                        .build())
                .doOnSuccess(s -> System.out.println(LOG_PREFIX + " ✅ Capacidades eliminadas exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + " ❌ Error al eliminar capacidades: " + error.getMessage());
                    return Mono.error(new BusinessException(DomainErrorCode.CAPACITY_DELETE_FAILED, error.getMessage()));
                });
    }

    private Mono<DeleteBootcampSaga> identifyTechnologiesToDelete(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 7: Identificando tecnologías a eliminar...");

        if (saga.getCapacityIds().isEmpty()) {
            System.out.println(LOG_PREFIX + " ✅ No hay capacidades relacionadas, por lo tanto no hay tecnologías");
            return Mono.just(saga.toBuilder()
                    .technologyIds(List.of())
                    .status(DeleteBootcampSaga.SagaStatus.TECHNOLOGIES_DELETING)
                    .build());
        }

        System.out.println(LOG_PREFIX + "    - Obteniendo tecnologías de las capacidades a eliminar...");

        return Flux.fromIterable(saga.getCapacityIds())
                .flatMap(capacityId ->
                    capacityDeleteGateway.getTechnologiesByCapacityId(capacityId)
                        .map(techIds -> new java.util.AbstractMap.SimpleEntry<Long, List<Long>>(capacityId, techIds))
                )
                .collect(
                    () -> new java.util.HashSet<Long>(),
                    (set, entry) -> set.addAll(((java.util.Map.Entry<Long, List<Long>>) entry).getValue())
                )
                .flatMap(allTechnologyIds -> {
                    if (allTechnologyIds.isEmpty()) {
                        System.out.println(LOG_PREFIX + " ✅ No hay tecnologías relacionadas a las capacidades");
                        return Mono.just(saga.toBuilder()
                                .technologyIds(List.of())
                                .status(DeleteBootcampSaga.SagaStatus.TECHNOLOGIES_DELETING)
                                .build());
                    }

                    System.out.println(LOG_PREFIX + "    - Total tecnologías encontradas: " + allTechnologyIds.size());
                    System.out.println(LOG_PREFIX + " 🔄 Consultando conteo de capacidades por tecnología...");

                    return technologyDeleteGateway.getCapacityCountForTechnologies(new java.util.ArrayList<>(allTechnologyIds))
                            .map(counts -> {
                                System.out.println(LOG_PREFIX + " 📊 Conteos de capacidades por tecnología:");
                                counts.forEach((technologyId, count) ->
                                        System.out.println(LOG_PREFIX + "    - TechnologyID " + technologyId + ": " + count + " capacidad(es)"));

                                List<Long> technologiesToDelete = allTechnologyIds.stream()
                                        .filter(technologyId -> {
                                            int count = counts.getOrDefault(technologyId, 0);
                                            boolean shouldDelete = count <= 1;
                                            System.out.println(LOG_PREFIX + "    - TechnologyID " + technologyId + ": count=" + count + ", eliminar=" + shouldDelete);
                                            return shouldDelete;
                                        })
                                        .collect(Collectors.toList());

                                System.out.println(LOG_PREFIX + " ✅ Tecnologías a eliminar: " + technologiesToDelete.size() + " [IDs: " + technologiesToDelete + "]");
                                return saga.toBuilder()
                                        .technologyIds(technologiesToDelete)
                                        .status(DeleteBootcampSaga.SagaStatus.TECHNOLOGIES_DELETING)
                                        .build();
                            });
                });
    }

    private Mono<DeleteBootcampSaga> deleteTechnologiesStep(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 8: Eliminando tecnologías via Technology Service...");

        if (saga.getTechnologyIds().isEmpty()) {
            System.out.println(LOG_PREFIX + " ℹ️ No hay tecnologías a eliminar, saltando este paso");
            return Mono.just(saga.toBuilder()
                    .status(DeleteBootcampSaga.SagaStatus.TECHNOLOGIES_DELETED)
                    .technologyDeleteResult(DeleteBootcampSaga.StepResult.builder()
                            .success(true)
                            .build())
                    .build());
        }

        System.out.println(LOG_PREFIX + " 🔄 Llamando a DELETE /api/technologies/batch con IDs: " + saga.getTechnologyIds());
        return technologyDeleteGateway.deleteTechnologies(saga.getTechnologyIds())
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofMillis(INITIAL_BACKOFF_MS))
                        .maxBackoff(Duration.ofMillis(MAX_BACKOFF_MS)))
                .thenReturn(saga.toBuilder()
                        .status(DeleteBootcampSaga.SagaStatus.TECHNOLOGIES_DELETED)
                        .technologyDeleteResult(DeleteBootcampSaga.StepResult.builder()
                                .success(true)
                                .build())
                        .build())
                .doOnSuccess(s -> System.out.println(LOG_PREFIX + " ✅ Tecnologías eliminadas exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + " ❌ Error al eliminar tecnologías: " + error.getMessage());
                    return Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_DELETE_FAILED, error.getMessage()));
                });
    }

    private Mono<DeleteBootcampSaga> completeSaga(DeleteBootcampSaga saga) {
        System.out.println(LOG_PREFIX + " PASO 9: Completando SAGA...");
        DeleteBootcampSaga completedSaga = saga.toBuilder()
                .status(DeleteBootcampSaga.SagaStatus.COMPLETED)
                .endTime(LocalDateTime.now())
                .build();
        System.out.println(LOG_PREFIX + " ✅ SAGA completada exitosamente: " + completedSaga.getStatus());
        return Mono.just(completedSaga);
    }

    private Mono<DeleteBootcampSaga> handleSagaError(Throwable error, Long bootcampId) {
        System.out.println(LOG_PREFIX + " ⚠️ ERROR DURANTE LA ELIMINACIÓN DEL BOOTCAMP (ID: " + bootcampId + ")");
        if (error instanceof BusinessException) {
            BusinessException be = (BusinessException) error;
            System.out.println(LOG_PREFIX + " ❌ BusinessException: " + be.getMessage() + " - Code: " + be.getCode());
            return rollbackSaga(bootcampId, be.getMessage());
        }

        System.out.println(LOG_PREFIX + " ❌ Exception: " + error.getMessage());
        error.printStackTrace();
        return rollbackSaga(bootcampId, error.getMessage());
    }

    private Mono<DeleteBootcampSaga> rollbackSaga(Long bootcampId, String errorMessage) {
        System.out.println(LOG_PREFIX + " 🔄 INICIANDO ROLLBACK DE SAGA para bootcampId: " + bootcampId);
        System.out.println(LOG_PREFIX + "    Motivo del error: " + errorMessage);

        DeleteBootcampSaga saga = DeleteBootcampSaga.builder()
                .bootcampId(bootcampId)
                .status(DeleteBootcampSaga.SagaStatus.ROLLING_BACK)
                .endTime(LocalDateTime.now())
                .rollbackInfo(DeleteBootcampSaga.RollbackInfo.builder()
                        .bootcampDeleted(true)
                        .build())
                .build();

        return Mono.just(saga)
                .flatMap(s -> restoreBootcampIfNeeded(s)
                        .then(Mono.just(s)))
                .flatMap(s -> restoreCapacitiesIfNeeded(s)
                        .then(Mono.just(s)))
                .flatMap(s -> restoreTechnologiesIfNeeded(s)
                        .then(Mono.just(s)))
                .map(s -> s.toBuilder()
                        .status(DeleteBootcampSaga.SagaStatus.ROLLED_BACK)
                        .endTime(LocalDateTime.now())
                        .build())
                .doOnSuccess(s -> System.out.println(LOG_PREFIX + " ✅ ROLLBACK COMPLETADO: SAGA restaurada a estado anterior"))
                .onErrorResume(rollbackError -> {
                    System.out.println(LOG_PREFIX + " ❌ ERROR DURANTE ROLLBACK: " + rollbackError.getMessage());
                    rollbackError.printStackTrace();
                    return Mono.just(saga.toBuilder()
                            .status(DeleteBootcampSaga.SagaStatus.FAILED)
                            .endTime(LocalDateTime.now())
                            .build());
                });
    }

    private Mono<Void> restoreBootcampIfNeeded(DeleteBootcampSaga saga) {
        if (saga.getBootcampId() == null) {
            System.out.println(LOG_PREFIX + "    ℹ️ No hay bootcampId para restaurar");
            return Mono.empty();
        }

        System.out.println(LOG_PREFIX + "    🔄 Restaurando Bootcamp (ID: " + saga.getBootcampId() + ")...");
        return bootcampDeleteGateway.restoreBootcamp(saga.getBootcampId())
                .doOnSuccess(v -> System.out.println(LOG_PREFIX + "    ✅ Bootcamp restaurado exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + "    ❌ Error restaurando bootcamp: " + error.getMessage());
                    return Mono.error(error);
                });
    }

    private Mono<Void> restoreCapacitiesIfNeeded(DeleteBootcampSaga saga) {
        if (saga.getCapacityIds() == null || saga.getCapacityIds().isEmpty()) {
            System.out.println(LOG_PREFIX + "    ℹ️ No hay capacidades para restaurar");
            return Mono.empty();
        }

        System.out.println(LOG_PREFIX + "    🔄 Restaurando Capacidades (IDs: " + saga.getCapacityIds() + ")...");
        return capacityDeleteGateway.restoreCapacities(saga.getCapacityIds())
                .doOnSuccess(v -> System.out.println(LOG_PREFIX + "    ✅ Capacidades restauradas exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + "    ❌ Error restaurando capacidades: " + error.getMessage());
                    return Mono.error(error);
                });
    }

    private Mono<Void> restoreTechnologiesIfNeeded(DeleteBootcampSaga saga) {
        if (saga.getTechnologyIds() == null || saga.getTechnologyIds().isEmpty()) {
            System.out.println(LOG_PREFIX + "    ℹ️ No hay tecnologías para restaurar");
            return Mono.empty();
        }

        System.out.println(LOG_PREFIX + "    🔄 Restaurando Tecnologías (IDs: " + saga.getTechnologyIds() + ")...");
        return technologyDeleteGateway.restoreTechnologies(saga.getTechnologyIds())
                .doOnSuccess(v -> System.out.println(LOG_PREFIX + "    ✅ Tecnologías restauradas exitosamente"))
                .onErrorResume(error -> {
                    System.out.println(LOG_PREFIX + "    ❌ Error restaurando tecnologías: " + error.getMessage());
                    return Mono.error(error);
                });
    }
}
