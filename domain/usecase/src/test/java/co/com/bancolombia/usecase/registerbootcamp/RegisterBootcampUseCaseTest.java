package co.com.bancolombia.usecase.registerbootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.bootcamp.gateways.BootcampReportGateway;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.gateways.CapacityValidationGateway;
import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RegisterBootcampUseCase.
 * Tests validation logic, persistence, and async reporting.
 */
@ExtendWith(MockitoExtension.class)
class RegisterBootcampUseCaseTest {

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapacityValidationGateway capacityValidationGateway;

    @Mock
    private BootcampReportGateway bootcampReportGateway;

    private RegisterBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterBootcampUseCase(
                bootcampRepository,
                capacityValidationGateway,
                bootcampReportGateway
        );
    }

    @Test
    @DisplayName("Should successfully register a bootcamp with valid data")
    void testSuccessfulBootcampRegistration() {
        Bootcamp bootcamp = createBootcamp(1L, "Java Bootcamp", "Learn Java", LocalDate.now(), 8);
        Bootcamp savedBootcamp = createBootcamp(1L, "Java Bootcamp", "Learn Java", LocalDate.now(), 8);

        when(bootcampRepository.existsByName("Java Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L, 11L)))
                .thenReturn(Mono.just(Map.of("Java", 5L, "Spring", 3L)));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                        result.getName().equals("Java Bootcamp")
                )
                .verifyComplete();

        verify(bootcampRepository).existsByName("Java Bootcamp");
        verify(capacityValidationGateway).validateCapacities(List.of(10L, 11L));
        verify(bootcampRepository).save(bootcamp);
        verify(bootcampReportGateway).sendBootcampReportAsync(1L, 2L, 8L);
    }

    @Test
    @DisplayName("Should fail when bootcamp name is duplicated")
    void testDuplicateBootcampName() {
        Bootcamp bootcamp = createBootcamp(null, "Python Bootcamp", "Learn Python", LocalDate.now(), 12);

        when(bootcampRepository.existsByName("Python Bootcamp")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectError(BusinessException.class)
                .verify();

        verify(bootcampRepository, never()).save(any());
        verify(bootcampReportGateway, never()).sendBootcampReportAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should fail when capacities are invalid")
    void testInvalidCapacities() {
        Bootcamp bootcamp = createBootcamp(null, "Go Bootcamp", "Learn Go", LocalDate.now(), 6);

        when(bootcampRepository.existsByName("Go Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectError(BusinessException.class)
                .verify();

        verify(bootcampRepository, never()).save(any());
        verify(bootcampReportGateway, never()).sendBootcampReportAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void testRepositorySaveFailure() {
        Bootcamp bootcamp = createBootcamp(null, "Rust Bootcamp", "Learn Rust", LocalDate.now(), 10);

        when(bootcampRepository.existsByName("Rust Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp))
                .thenReturn(Mono.error(new RuntimeException("Database connection error")));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectError(RuntimeException.class)
                .verify();

        verify(bootcampReportGateway, never()).sendBootcampReportAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should aggregate technology counts correctly for report")
    void testTechnologyCountAggregation() {
        Bootcamp bootcamp = createBootcamp(1L, "Full Stack Bootcamp", "Full Stack Dev", LocalDate.now(), 16);
        Bootcamp savedBootcamp = createBootcamp(1L, "Full Stack Bootcamp", "Full Stack Dev", LocalDate.now(), 16);

        when(bootcampRepository.existsByName("Full Stack Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L, 11L)))
                .thenReturn(Mono.just(Map.of("Java", 5L, "JavaScript", 4L, "SQL", 3L)));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<Long> technologyCountCaptor = ArgumentCaptor.forClass(Long.class);
        verify(bootcampReportGateway).sendBootcampReportAsync(
                any(),
                any(),
                technologyCountCaptor.capture()
        );

        assertThat(technologyCountCaptor.getValue()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Should handle technology count retrieval error gracefully")
    void testTechnologyCountRetrievalError() {
        Bootcamp bootcamp = createBootcamp(1L, "DevOps Bootcamp", "DevOps", LocalDate.now(), 8);
        Bootcamp savedBootcamp = createBootcamp(1L, "DevOps Bootcamp", "DevOps", LocalDate.now(), 8);

        when(bootcampRepository.existsByName("DevOps Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L, 11L)))
                .thenReturn(Mono.error(new RuntimeException("External service unavailable")));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextCount(1)
                .verifyComplete();

        verify(bootcampRepository).save(bootcamp);
    }

    @Test
    @DisplayName("Should register bootcamp with single capacity")
    void testBootcampWithSingleCapacity() {
        Bootcamp bootcamp = createBootcampWithCapacities(
                null,
                "Frontend Bootcamp",
                "Learn Frontend",
                LocalDate.now(),
                6,
                List.of(createCapacity(10L))
        );
        Bootcamp savedBootcamp = createBootcampWithCapacities(
                1L,
                "Frontend Bootcamp",
                "Learn Frontend",
                LocalDate.now(),
                6,
                List.of(createCapacity(10L))
        );

        when(bootcampRepository.existsByName("Frontend Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L)))
                .thenReturn(Mono.just(Map.of("JavaScript", 8L)));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextMatches(result -> result.getName().equals("Frontend Bootcamp"))
                .verifyComplete();

        verify(bootcampReportGateway).sendBootcampReportAsync(1L, 1L, 8L);
    }

    @Test
    @DisplayName("Should register bootcamp with multiple capacities")
    void testBootcampWithMultipleCapacities() {
        List<Capacity> capacities = List.of(
                createCapacity(10L),
                createCapacity(11L),
                createCapacity(12L)
        );
        Bootcamp bootcamp = createBootcampWithCapacities(
                null,
                "Enterprise Bootcamp",
                "Enterprise Development",
                LocalDate.now(),
                20,
                capacities
        );
        Bootcamp savedBootcamp = createBootcampWithCapacities(
                1L,
                "Enterprise Bootcamp",
                "Enterprise Development",
                LocalDate.now(),
                20,
                capacities
        );

        when(bootcampRepository.existsByName("Enterprise Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L, 12L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L, 11L, 12L)))
                .thenReturn(Mono.just(Map.of("Java", 6L, "Python", 5L, "Go", 4L)));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextCount(1)
                .verifyComplete();

        verify(bootcampReportGateway).sendBootcampReportAsync(1L, 3L, 15L);
    }

    @Test
    @DisplayName("Should handle empty technology counts map")
    void testEmptyTechnologyCountsMap() {
        Bootcamp bootcamp = createBootcamp(1L, "Basic Bootcamp", "Basic", LocalDate.now(), 4);
        Bootcamp savedBootcamp = createBootcamp(1L, "Basic Bootcamp", "Basic", LocalDate.now(), 4);

        when(bootcampRepository.existsByName("Basic Bootcamp")).thenReturn(Mono.just(false));
        when(capacityValidationGateway.validateCapacities(List.of(10L, 11L))).thenReturn(Mono.just(true));
        when(bootcampRepository.save(bootcamp)).thenReturn(Mono.just(savedBootcamp));
        when(capacityValidationGateway.getTechnologyCountsByCapacityIds(List.of(10L, 11L)))
                .thenReturn(Mono.just(Map.of()));

        StepVerifier.create(useCase.execute(bootcamp))
                .expectNextCount(1)
                .verifyComplete();

        verify(bootcampReportGateway).sendBootcampReportAsync(1L, 2L, 0L);
    }

    // Helper methods

    private Bootcamp createBootcamp(Long id, String name, String description, LocalDate releaseDate, Integer duration) {
        return createBootcampWithCapacities(
                id,
                name,
                description,
                releaseDate,
                duration,
                List.of(createCapacity(10L), createCapacity(11L))
        );
    }

    private Bootcamp createBootcampWithCapacities(
            Long id,
            String name,
            String description,
            LocalDate releaseDate,
            Integer duration,
            List<Capacity> capacities
    ) {
        return Bootcamp.builder()
                .id(id)
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .capacities(capacities)
                .build();
    }

    private Capacity createCapacity(Long id) {
        return Capacity.builder()
                .id(id)
                .name("Capacity " + id)
                .technologies(List.of())
                .build();
    }
}
