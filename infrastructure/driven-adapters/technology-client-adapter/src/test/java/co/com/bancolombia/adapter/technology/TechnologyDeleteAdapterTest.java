package co.com.bancolombia.adapter.technology;

import co.com.bancolombia.webclient.config.WebClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for TechnologyDeleteAdapter.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyDeleteAdapterTest {

    @Mock
    private WebClientFactory webClientFactory;

    private TechnologyDeleteAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TechnologyDeleteAdapter(webClientFactory);
    }

    @Test
    @DisplayName("Should handle empty technology list for counts")
    void testGetCapacityCountForEmptyList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.getCapacityCountForTechnologies(emptyList))
                .expectNext(Map.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null technology list for counts")
    void testGetCapacityCountForNullList() {
        StepVerifier.create(adapter.getCapacityCountForTechnologies(null))
                .expectNext(Map.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty technology list for deletion")
    void testDeleteEmptyTechnologiesList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.deleteTechnologies(emptyList))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle null technology list for deletion")
    void testDeleteNullTechnologiesList() {
        StepVerifier.create(adapter.deleteTechnologies(null))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle empty technology list for restoration")
    void testRestoreEmptyTechnologiesList() {
        List<Long> emptyList = List.of();

        StepVerifier.create(adapter.restoreTechnologies(emptyList))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should handle null technology list for restoration")
    void testRestoreNullTechnologiesList() {
        StepVerifier.create(adapter.restoreTechnologies(null))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("CapacityCountsRequest should contain technology IDs")
    void testCapacityCountsRequest() {
        List<Long> technologyIds = List.of(10L, 20L);
        TechnologyDeleteAdapter.CapacityCountsRequest request =
                new TechnologyDeleteAdapter.CapacityCountsRequest(technologyIds);

        assertThat(request.getTechnologyIds()).isEqualTo(technologyIds);
    }

    @Test
    @DisplayName("DeleteTechnologiesRequest should contain technology IDs and reason")
    void testDeleteTechnologiesRequest() {
        List<Long> technologyIds = List.of(10L);
        String reason = "Bootcamp deletion";
        TechnologyDeleteAdapter.DeleteTechnologiesRequest request =
                new TechnologyDeleteAdapter.DeleteTechnologiesRequest(technologyIds, reason);

        assertThat(request.getTechnologyIds()).isEqualTo(technologyIds);
        assertThat(request.getReason()).isEqualTo(reason);
    }
}
