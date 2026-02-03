package co.com.bancolombia.usecase.gettechnologiesbyids;

import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTechnologiesByIdsUseCase Tests")
class GetTechnologiesByIdsUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    private GetTechnologiesByIdsUseCase getTechnologiesByIdsUseCase;

    @BeforeEach
    void setUp() {
        getTechnologiesByIdsUseCase = new GetTechnologiesByIdsUseCase(technologyRepository);
    }

    @Test
    @DisplayName("Should return empty Flux when ids is null")
    void testExecute_NullIds() {
        // Act & Assert
        StepVerifier.create(getTechnologiesByIdsUseCase.execute(null))
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should return empty Flux when ids is empty")
    void testExecute_EmptyIds() {
        // Act & Assert
        StepVerifier.create(getTechnologiesByIdsUseCase.execute(List.of()))
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should return technologies from repository when ids are provided")
    void testExecute_WithIds() {
        // Arrange
        List<Long> ids = List.of(1L, 2L, 3L);
        Technology tech1 = createTechnology(1L, "Spring", "Java framework");
        Technology tech2 = createTechnology(2L, "React", "JS library");
        Technology tech3 = createTechnology(3L, "Python", "Programming language");

        when(technologyRepository.findByIds(ids))
            .thenReturn(Flux.just(tech1, tech2, tech3));

        // Act & Assert
        StepVerifier.create(getTechnologiesByIdsUseCase.execute(ids))
            .expectNext(tech1, tech2, tech3)
            .verifyComplete();

        verify(technologyRepository).findByIds(ids);
    }

    @Test
    @DisplayName("Should return single technology when single id is provided")
    void testExecute_WithSingleId() {
        // Arrange
        List<Long> ids = List.of(1L);
        Technology tech = createTechnology(1L, "Java", "Programming language");

        when(technologyRepository.findByIds(ids))
            .thenReturn(Flux.just(tech));

        // Act & Assert
        StepVerifier.create(getTechnologiesByIdsUseCase.execute(ids))
            .expectNext(tech)
            .verifyComplete();

        verify(technologyRepository).findByIds(ids);
    }

    @Test
    @DisplayName("Should return empty Flux when no technologies found")
    void testExecute_NoTechnologiesFound() {
        // Arrange
        List<Long> ids = List.of(999L);

        when(technologyRepository.findByIds(ids))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(getTechnologiesByIdsUseCase.execute(ids))
            .expectComplete()
            .verify();

        verify(technologyRepository).findByIds(ids);
    }

    // Helper method
    private Technology createTechnology(Long id, String name, String description) {
        Technology tech = new Technology();
        tech.setId(id);
        tech.setName(name);
        tech.setDescription(description);
        return tech;
    }
}
