package co.com.bancolombia.usecase.listbootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ListBootcampsUseCase.
 * Tests pagination validation and bootcamp listing with various page configurations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListBootcampsUseCaseTest {

    @Mock
    private BootcampRepository bootcampRepository;

    private ListBootcampsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListBootcampsUseCase(bootcampRepository);
    }

    @Test
    @DisplayName("Should list bootcamps with valid pagination parameters")
    void testListBootcampsWithValidPagination() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .sortBy("name")
                .build();

        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 2, 1), 30);

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of(bootcamp1, bootcamp2))
                .currentPage(0)
                .pageSize(10)
                .totalElements(2L)
                .totalPages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .sortBy("name")
                .sortDirection(SortDirection.ASC)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextMatches(result ->
                        result.getContent().size() == 2 &&
                        result.getCurrentPage() == 0 &&
                        result.getPageSize() == 10
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail with negative page number")
    void testFailWithNegativePageNumber() {
        PageRequest pageRequest = PageRequest.builder()
                .page(-1)
                .size(10)
                .build();

        StepVerifier.create(useCase.execute(pageRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should fail with page size less than 1")
    void testFailWithPageSizeLessThanOne() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(0)
                .build();

        StepVerifier.create(useCase.execute(pageRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should fail with page size greater than max")
    void testFailWithPageSizeGreaterThanMax() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(51)
                .build();

        StepVerifier.create(useCase.execute(pageRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should succeed with page size at maximum limit")
    void testSucceedWithMaxPageSize() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(50)
                .build();

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(50)
                .totalElements(0L)
                .totalPages(0)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextMatches(result -> result.getPageSize() == 50)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail with invalid sort field")
    void testFailWithInvalidSortField() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .sortBy("invalidField")
                .build();

        StepVerifier.create(useCase.execute(pageRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should accept sort by name")
    void testSortByName() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .sortBy("name")
                .build();

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should accept sort by capacityCount")
    void testSortByCapacityCount() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .sortBy("capacityCount")
                .build();

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should allow null sortBy field")
    void testNullSortByField() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .sortBy(null)
                .build();

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should list multiple pages of bootcamps")
    void testMultiplePages() {
        PageRequest pageRequest1 = PageRequest.builder()
                .page(0)
                .size(10)
                .build();

        Bootcamp bootcamp1 = createBootcamp(1L, "Java Bootcamp", LocalDate.of(2024, 1, 1), 30);
        Bootcamp bootcamp2 = createBootcamp(2L, "Python Bootcamp", LocalDate.of(2024, 2, 1), 30);

        Page<Bootcamp> page1 = Page.<Bootcamp>builder()
                .content(List.of(bootcamp1, bootcamp2))
                .currentPage(0)
                .pageSize(10)
                .totalElements(20L)
                .totalPages(2)
                .hasNextPage(true)
                .hasPreviousPage(false)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest1))
                .thenReturn(Mono.just(page1));

        StepVerifier.create(useCase.execute(pageRequest1))
                .expectNextMatches(result ->
                        result.getCurrentPage() == 0 &&
                        result.getTotalElements() == 20L
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty list when no bootcamps exist")
    void testEmptyBootcampList() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(10)
                .build();

        Page<Bootcamp> emptyPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(emptyPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextMatches(result ->
                        result.getContent().isEmpty() &&
                        result.getTotalElements() == 0L
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate page request with minimum valid values")
    void testMinimumValidPageRequest() {
        PageRequest pageRequest = PageRequest.builder()
                .page(0)
                .size(1)
                .build();

        Page<Bootcamp> expectedPage = Page.<Bootcamp>builder()
                .content(List.of())
                .currentPage(0)
                .pageSize(1)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(bootcampRepository.findAllWithPagination(pageRequest))
                .thenReturn(Mono.just(expectedPage));

        StepVerifier.create(useCase.execute(pageRequest))
                .expectNextCount(1)
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
