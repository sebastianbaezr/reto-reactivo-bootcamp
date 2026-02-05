package co.com.bancolombia.adapter.capacity;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.webclient.config.WebClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CapacityDeleteAdapter.
 * Note: Requires WireMock or TestWebClient setup in full integration tests.
 * These tests verify the adapter's logic and error handling.
 */
@ExtendWith(MockitoExtension.class)
class CapacityDeleteAdapterTest {

    @Mock
    private WebClientFactory webClientFactory;

    private CapacityDeleteAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CapacityDeleteAdapter(webClientFactory);
    }

    @Test
    @DisplayName("Should handle empty capacity list for counts")
    void testGetBootcampCountForEmptyList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.getBootcampCountForCapacities(emptyList))
                .expectNext(Map.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null capacity list for counts")
    void testGetBootcampCountForNullList() {
        StepVerifier.create(adapter.getBootcampCountForCapacities(null))
                .expectNext(Map.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty capacity list for deletion")
    void testDeleteEmptyCapacitiesList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.deleteCapacities(emptyList))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle null capacity list for deletion")
    void testDeleteNullCapacitiesList() {
        StepVerifier.create(adapter.deleteCapacities(null))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle empty capacity list for restoration")
    void testRestoreEmptyCapacitiesList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.restoreCapacities(emptyList))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle null capacity list for restoration")
    void testRestoreNullCapacitiesList() {
        StepVerifier.create(adapter.restoreCapacities(null))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("BootcampCountsRequest should contain capacity IDs")
    void testBootcampCountsRequest() {
        List<Long> capacityIds = List.of(5L, 6L);
        CapacityDeleteAdapter.BootcampCountsRequest request =
                new CapacityDeleteAdapter.BootcampCountsRequest(capacityIds);

        assertThat(request.getCapacityIds()).isEqualTo(capacityIds);
    }

    @Test
    @DisplayName("DeleteCapacitiesRequest should contain capacity IDs and reason")
    void testDeleteCapacitiesRequest() {
        List<Long> capacityIds = List.of(5L);
        String reason = "Bootcamp deletion";
        CapacityDeleteAdapter.DeleteCapacitiesRequest request =
                new CapacityDeleteAdapter.DeleteCapacitiesRequest(capacityIds, reason);

        assertThat(request.getCapacityIds()).isEqualTo(capacityIds);
        assertThat(request.getReason()).isEqualTo(reason);
    }
}
