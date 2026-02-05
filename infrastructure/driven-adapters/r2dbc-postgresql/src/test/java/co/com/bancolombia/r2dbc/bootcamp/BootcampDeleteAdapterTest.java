package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for BootcampDeleteAdapter.
 */
@ExtendWith(MockitoExtension.class)
class BootcampDeleteAdapterTest {

    @Mock
    private BootcampR2dbcRepository bootcampRepository;

    @Mock
    private BootcampCapacityR2dbcRepository bootcampCapacityRepository;

    private BootcampDeleteAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BootcampDeleteAdapter(bootcampRepository, bootcampCapacityRepository);
    }

    @Test
    @DisplayName("Should retrieve bootcamp info with capacity IDs from bootcamp entity")
    void testGetBootcampInfo() {
        Long bootcampId = 1L;
        List<Long> capacityIds = List.of(5L, 6L);

        BootcampData bootcampData = BootcampData.builder()
                .id(bootcampId)
                .name("Java Bootcamp")
                .capacityIds(capacityIds)
                .build();

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcampData));

        StepVerifier.create(adapter.getBootcampInfo(bootcampId))
                .expectNextMatches(info ->
                        info.getId().equals(bootcampId) &&
                        info.getName().equals("Java Bootcamp") &&
                        info.getCapacityIds().size() == 2 &&
                        info.getCapacityIds().equals(capacityIds)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail when bootcamp not found")
    void testGetBootcampInfoNotFound() {
        Long bootcampId = 999L;

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.getBootcampInfo(bootcampId))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should delete bootcamp and its relationships transactionally")
    void testDeleteBootcamp() {
        Long bootcampId = 1L;

        when(bootcampCapacityRepository.deleteByBootcampId(bootcampId)).thenReturn(Mono.empty());
        when(bootcampRepository.deleteById(bootcampId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteBootcamp(bootcampId))
                .expectComplete()
                .verify();

        verify(bootcampCapacityRepository).deleteByBootcampId(bootcampId);
        verify(bootcampRepository).deleteById(bootcampId);
    }

    @Test
    @DisplayName("Should check if bootcamp exists")
    void testExistsById() {
        Long bootcampId = 1L;

        when(bootcampRepository.existsById(bootcampId)).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsById(bootcampId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when bootcamp does not exist")
    void testExistsByIdNotFound() {
        Long bootcampId = 999L;

        when(bootcampRepository.existsById(bootcampId)).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsById(bootcampId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail to restore bootcamp (hard delete)")
    void testRestoreBootcampNotSupported() {
        Long bootcampId = 1L;

        StepVerifier.create(adapter.restoreBootcamp(bootcampId))
                .expectError(BusinessException.class)
                .verify();
    }
}
