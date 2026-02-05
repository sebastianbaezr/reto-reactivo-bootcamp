package co.com.bancolombia.usecase.deletebootcamp;

import co.com.bancolombia.model.bootcamp.DeleteBootcampSaga;
import co.com.bancolombia.model.bootcamp.gateways.BootcampDeleteGateway;
import co.com.bancolombia.model.capacity.gateways.CapacityDeleteGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyDeleteGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeleteBootcampUseCase.
 * Tests saga orchestration, retry logic, and rollback behavior.
 */
@ExtendWith(MockitoExtension.class)
class DeleteBootcampUseCaseTest {

    @Mock
    private BootcampDeleteGateway bootcampDeleteGateway;

    @Mock
    private CapacityDeleteGateway capacityDeleteGateway;

    @Mock
    private TechnologyDeleteGateway technologyDeleteGateway;

    private DeleteBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBootcampUseCase(
                bootcampDeleteGateway,
                capacityDeleteGateway,
                technologyDeleteGateway
        );
    }

    @Test
    @DisplayName("Should complete saga successfully when all steps succeed")
    void testCompleteSagaSuccessfully() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L, 6L),
                List.of(10L, 20L)
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L, 6L)))
                .thenReturn(Mono.just(Map.of(5L, 1, 6L, 3)));
        when(capacityDeleteGateway.deleteCapacities(List.of(5L))).thenReturn(Mono.empty());
        when(technologyDeleteGateway.getCapacityCountForTechnologies(List.of(10L, 20L)))
                .thenReturn(Mono.just(Map.of(10L, 1, 20L, 1)));
        when(technologyDeleteGateway.deleteTechnologies(List.of(10L, 20L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.COMPLETED &&
                        saga.getBootcampDeleteResult().isSuccess() &&
                        saga.getCapacityIds().size() == 1 &&
                        saga.getTechnologyIds().size() == 2
                )
                .verifyComplete();

        verify(bootcampDeleteGateway).deleteBootcamp(bootcampId);
        verify(capacityDeleteGateway).deleteCapacities(List.of(5L));
        verify(technologyDeleteGateway).deleteTechnologies(List.of(10L, 20L));
        verify(capacityDeleteGateway, never()).restoreCapacities(any());
        verify(technologyDeleteGateway, never()).restoreTechnologies(any());
    }

    @Test
    @DisplayName("Should perform selective deletion - capacities with count <= 1")
    void testSelectiveDeletionCapacities() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L, 6L, 7L),
                List.of()
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L, 6L, 7L)))
                .thenReturn(Mono.just(Map.of(5L, 1, 6L, 3, 7L, 1)));
        when(capacityDeleteGateway.deleteCapacities(List.of(5L, 7L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.COMPLETED &&
                        saga.getCapacityIds().size() == 2
                )
                .verifyComplete();

        verify(capacityDeleteGateway).deleteCapacities(List.of(5L, 7L));
    }

    @Test
    @DisplayName("Should rollback when capacity deletion fails")
    void testRollbackOnCapacityDeletionFailure() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L),
                List.of(10L)
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L)))
                .thenReturn(Mono.just(Map.of(5L, 1)));
        when(capacityDeleteGateway.deleteCapacities(List.of(5L)))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));
        when(capacityDeleteGateway.restoreCapacities(List.of(5L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.ROLLED_BACK
                )
                .verifyComplete();

        verify(capacityDeleteGateway).restoreCapacities(List.of(5L));
        verify(bootcampDeleteGateway, never()).restoreBootcamp(bootcampId);
    }

    @Test
    @DisplayName("Should rollback when technology deletion fails")
    void testRollbackOnTechnologyDeletionFailure() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L),
                List.of(10L)
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L)))
                .thenReturn(Mono.just(Map.of(5L, 1)));
        when(capacityDeleteGateway.deleteCapacities(List.of(5L))).thenReturn(Mono.empty());
        when(technologyDeleteGateway.getCapacityCountForTechnologies(List.of(10L)))
                .thenReturn(Mono.just(Map.of(10L, 1)));
        when(technologyDeleteGateway.deleteTechnologies(List.of(10L)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));
        when(capacityDeleteGateway.restoreCapacities(List.of(5L))).thenReturn(Mono.empty());
        when(technologyDeleteGateway.restoreTechnologies(List.of(10L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.ROLLED_BACK
                )
                .verifyComplete();

        verify(capacityDeleteGateway).restoreCapacities(List.of(5L));
        verify(technologyDeleteGateway).restoreTechnologies(List.of(10L));
    }

    @Test
    @DisplayName("Should fail when bootcamp does not exist")
    void testBootcampNotFound() {
        Long bootcampId = 999L;

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should not restore bootcamp in rollback - it's the point of no return")
    void testBootcampNotRestoredInRollback() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L),
                List.of()
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L)))
                .thenReturn(Mono.just(Map.of(5L, 1)));
        when(capacityDeleteGateway.deleteCapacities(List.of(5L)))
                .thenReturn(Mono.error(new RuntimeException("Error")));
        when(capacityDeleteGateway.restoreCapacities(List.of(5L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.ROLLED_BACK &&
                        saga.getRollbackInfo().isBootcampDeleted()
                )
                .verifyComplete();

        verify(bootcampDeleteGateway, never()).restoreBootcamp(any());
    }

    @Test
    @DisplayName("Should handle bootcamp with no capacities")
    void testBootcampWithNoCapacities() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(),
                List.of()
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.COMPLETED &&
                        saga.getCapacityIds().isEmpty() &&
                        saga.getTechnologyIds().isEmpty()
                )
                .verifyComplete();

        verify(capacityDeleteGateway, never()).deleteCapacities(any());
        verify(technologyDeleteGateway, never()).deleteTechnologies(any());
    }

    @Test
    @DisplayName("Should keep capacities that are in use by other bootcamps")
    void testKeepCapacitiesInUse() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(5L, 6L),
                List.of()
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());
        when(capacityDeleteGateway.getBootcampCountForCapacities(List.of(5L, 6L)))
                .thenReturn(Mono.just(Map.of(5L, 5, 6L, 10)));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga ->
                        saga.getStatus() == DeleteBootcampSaga.SagaStatus.COMPLETED &&
                        saga.getCapacityIds().isEmpty()
                )
                .verifyComplete();

        verify(capacityDeleteGateway, never()).deleteCapacities(any());
    }

    @Test
    @DisplayName("Should track saga timing correctly")
    void testSagaTimingTracking() {
        Long bootcampId = 1L;
        BootcampDeleteGateway.BootcampDeleteInfo info = createBootcampInfo(
                bootcampId, "Java Bootcamp",
                List.of(),
                List.of()
        );

        when(bootcampDeleteGateway.existsById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampDeleteGateway.getBootcampInfo(bootcampId)).thenReturn(Mono.just(info));
        when(bootcampDeleteGateway.deleteBootcamp(bootcampId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(saga -> {
                    LocalDateTime startTime = saga.getStartTime();
                    LocalDateTime endTime = saga.getEndTime();
                    return startTime != null && endTime != null && endTime.isAfter(startTime);
                })
                .verifyComplete();
    }

    private BootcampDeleteGateway.BootcampDeleteInfo createBootcampInfo(
            Long bootcampId, String name, List<Long> capacityIds, List<Long> technologyIds) {
        return new BootcampDeleteGateway.BootcampDeleteInfo() {
            @Override
            public Long getId() {
                return bootcampId;
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
        };
    }
}
