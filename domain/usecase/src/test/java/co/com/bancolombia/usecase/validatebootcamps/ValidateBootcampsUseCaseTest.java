package co.com.bancolombia.usecase.validatebootcamps;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ValidateBootcampsUseCase.
 * Tests bootcamp existence validation and date conflict detection.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidateBootcampsUseCaseTest {

    @Mock
    private BootcampRepository bootcampRepository;

    private ValidateBootcampsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateBootcampsUseCase(bootcampRepository);
    }

    @Test
    @DisplayName("Should return empty result for empty input list")
    void testEmptyInputList() {
        StepVerifier.create(useCase.execute(List.of()))
                .expectNextMatches(result ->
                        result.getExistingIds().isEmpty() &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty result for null input")
    void testNullInput() {
        StepVerifier.create(useCase.execute(null))
                .expectNextMatches(result ->
                        result.getExistingIds().isEmpty() &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate all bootcamps exist without date conflicts")
    void testAllBootcampsExistNoConflicts() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 2, 15), 30);
        Bootcamp bootcamp3 = createBootcamp(3L, "Go Bootcamp", LocalDate.of(2024, 3, 20), 30);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2, bootcamp3));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(ids) &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should detect when some bootcamps do not exist")
    void testSomeBootcampsNotFound() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        Bootcamp bootcamp3 = createBootcamp(3L, "Go Bootcamp", LocalDate.of(2024, 3, 20), 30);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp3));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(List.of(1L, 3L)) &&
                        result.getNotFoundIds().equals(List.of(2L)) &&
                        !result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should detect date conflicts between bootcamps")
    void testDetectDateConflicts() {
        List<Long> ids = List.of(1L, 2L);
        // Bootcamp 1: Jan 1 - Jan 31 (30 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        // Bootcamp 2: Jan 15 - Feb 14 (31 days) - Overlaps with bootcamp1
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 1, 15), 31);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(ids) &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should not detect conflicts when bootcamps do not overlap")
    void testNoConflictsWhenBootcampsDoNotOverlap() {
        List<Long> ids = List.of(1L, 2L);
        // Bootcamp 1: Jan 1 - Jan 31 (30 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        // Bootcamp 2: Feb 1 - Mar 1 (29 days) - Starts after bootcamp1 ends
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 2, 1), 29);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(ids) &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should not detect conflict when bootcamp dates touch exactly")
    void testNoConflictWhenBootcampDatesTouchAtEnd() {
        List<Long> ids = List.of(1L, 2L);
        // Bootcamp 1: Jan 1 - Jan 31 (30 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        // Bootcamp 2: Jan 31 - Feb 29 (30 days) - Starts on the last day of bootcamp1
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 1, 31), 30);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result -> !result.isHasDateConflicts())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple bootcamps with complex conflict scenarios")
    void testMultipleBootcampsComplexScenario() {
        List<Long> ids = List.of(1L, 2L, 3L, 4L);
        // Bootcamp 1: Jan 1 - Jan 31 (30 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        // Bootcamp 2: Feb 1 - Mar 1 (29 days) - No conflict with 1
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 2, 1), 29);
        // Bootcamp 3: Feb 15 - Mar 15 (29 days) - Conflicts with 2
        Bootcamp bootcamp3 = createBootcamp(3L, "Go Bootcamp", LocalDate.of(2024, 2, 15), 29);
        // Bootcamp 4: Apr 1 - Apr 30 (29 days) - No conflict with others
        Bootcamp bootcamp4 = createBootcamp(4L, "Rust Bootcamp", LocalDate.of(2024, 4, 1), 29);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2, bootcamp3, bootcamp4));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(ids) &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        result.isHasDateConflicts() // Conflicts between bootcamp2 and bootcamp3
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate single bootcamp without conflicts")
    void testSingleBootcamp() {
        List<Long> ids = List.of(1L);
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().equals(ids) &&
                        result.getNotFoundIds().isEmpty() &&
                        result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should detect conflicts in overlapping bootcamps with partial overlap")
    void testPartialOverlapConflict() {
        List<Long> ids = List.of(1L, 2L);
        // Bootcamp 1: Jan 10 - Feb 10 (31 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 10), 31);
        // Bootcamp 2: Jan 20 - Feb 20 (31 days) - Partially overlaps
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 1, 20), 31);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.isHasDateConflicts() &&
                        result.isAllExist()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should detect conflicts when one bootcamp completely contains another")
    void testCompleteOverlapConflict() {
        List<Long> ids = List.of(1L, 2L);
        // Bootcamp 1: Jan 1 - Dec 31 (364 days)
        Bootcamp bootcamp1 = createBootcamp(1L, "Long Bootcamp", LocalDate.of(2024, 1, 1), 364);
        // Bootcamp 2: Feb 1 - Mar 1 (29 days) - Completely inside bootcamp1
        Bootcamp bootcamp2 = createBootcamp(2L, "Short Bootcamp", LocalDate.of(2024, 2, 1), 29);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.isHasDateConflicts() &&
                        result.isAllExist()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle all bootcamps not found")
    void testAllBootcampsNotFound() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().isEmpty() &&
                        result.getNotFoundIds().equals(ids) &&
                        !result.isAllExist() &&
                        !result.isHasDateConflicts()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should preserve order of existing IDs in result")
    void testPreserveOrderOfExistingIds() {
        List<Long> ids = List.of(3L, 1L, 2L);
        Bootcamp bootcamp1 = createBootcamp(1L, "Bootcamp 1", LocalDate.of(2024, 1, 1), 30);
        Bootcamp bootcamp3 = createBootcamp(3L, "Bootcamp 3", LocalDate.of(2024, 3, 1), 30);

        when(bootcampRepository.findByIds(ids))
                .thenReturn(Flux.just(bootcamp3, bootcamp1)); // Different order from input

        StepVerifier.create(useCase.execute(ids))
                .expectNextMatches(result ->
                        result.getExistingIds().contains(1L) &&
                        result.getExistingIds().contains(3L) &&
                        result.getNotFoundIds().equals(List.of(2L))
                )
                .verifyComplete();
    }

    // Helper method to create test bootcamps
    private Bootcamp createBootcamp(Long id, String name, LocalDate releaseDate, Integer duration) {
        return Bootcamp.builder()
                .id(id)
                .name(name)
                .description("Test bootcamp: " + name)
                .releaseDate(releaseDate)
                .duration(duration)
                .build();
    }
}
