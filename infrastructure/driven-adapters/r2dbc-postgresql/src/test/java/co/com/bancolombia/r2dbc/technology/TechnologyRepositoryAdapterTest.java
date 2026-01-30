package co.com.bancolombia.r2dbc.technology;

import co.com.bancolombia.model.technology.Technology;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TechnologyRepositoryAdapter Tests")
class TechnologyRepositoryAdapterTest {

    @Mock
    private TechnologyR2dbcRepository technologyR2dbcRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TechnologyRepositoryAdapter technologyRepositoryAdapter;

    @Test
    @DisplayName("Should check if technology name exists and return true")
    void testExistsByName_NameExists() {
        // Arrange
        String name = "Spring Boot";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectNext(true)
            .verifyComplete();

        verify(technologyR2dbcRepository).existsByName(name);
    }

    @Test
    @DisplayName("Should check if technology name exists and return false")
    void testExistsByName_NameDoesNotExist() {
        // Arrange
        String name = "NonExistent";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectNext(false)
            .verifyComplete();

        verify(technologyR2dbcRepository).existsByName(name);
    }

    @Test
    @DisplayName("Should handle error when checking technology existence")
    void testExistsByName_DatabaseError() {
        // Arrange
        String name = "React";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectError(RuntimeException.class)
            .verify();

        verify(technologyR2dbcRepository).existsByName(name);
    }

    @Test
    @DisplayName("Should handle null name parameter for existence check")
    void testExistsByName_NullName() {
        // Arrange
        when(technologyR2dbcRepository.existsByName(null))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(null))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty string name for existence check")
    void testExistsByName_EmptyName() {
        // Arrange
        String name = "";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle case-sensitive name search")
    void testExistsByName_CaseSensitive() {
        // Arrange
        String lowerCaseName = "spring boot";
        String upperCaseName = "SPRING BOOT";

        when(technologyR2dbcRepository.existsByName(lowerCaseName))
            .thenReturn(Mono.just(true));
        when(technologyR2dbcRepository.existsByName(upperCaseName))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(lowerCaseName))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(technologyRepositoryAdapter.existsByName(upperCaseName))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return true for names with special characters that exist")
    void testExistsByName_SpecialCharacters() {
        // Arrange
        String name = "C++";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return true for names with dots that exist")
    void testExistsByName_WithDots() {
        // Arrange
        String name = ".NET Framework";
        when(technologyR2dbcRepository.existsByName(name))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.existsByName(name))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should save technology successfully")
    void testSave_Success() {
        // Arrange
        Technology technology = buildTechnology(null, "Python", "Programming language");
        TechnologyData technologyData = buildTechnologyData(null, "Python", "Programming language");
        TechnologyData savedData = buildTechnologyData(1L, "Python", "Programming language");
        Technology savedTechnology = buildTechnology(1L, "Python", "Programming language");

        when(objectMapper.map(technology, TechnologyData.class))
            .thenReturn(technologyData);
        when(technologyR2dbcRepository.save(technologyData))
            .thenReturn(Mono.just(savedData));
        when(objectMapper.map(savedData, Technology.class))
            .thenReturn(savedTechnology);

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.save(technology))
            .expectNext(savedTechnology)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find technology by id")
    void testFindById_Found() {
        // Arrange
        Long id = 1L;
        TechnologyData technologyData = buildTechnologyData(id, "Java", "Programming language");
        Technology technology = buildTechnology(id, "Java", "Programming language");

        when(technologyR2dbcRepository.findById(id))
            .thenReturn(Mono.just(technologyData));
        when(objectMapper.map(technologyData, Technology.class))
            .thenReturn(technology);

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.findById(id))
            .expectNext(technology)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when technology not found by id")
    void testFindById_NotFound() {
        // Arrange
        Long id = 999L;

        when(technologyR2dbcRepository.findById(id))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.findById(id))
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should find all technologies")
    void testFindAll_Success() {
        // Arrange
        TechnologyData tech1 = buildTechnologyData(1L, "JavaScript", "Web language");
        TechnologyData tech2 = buildTechnologyData(2L, "TypeScript", "JavaScript superset");
        Technology mappedTech1 = buildTechnology(1L, "JavaScript", "Web language");
        Technology mappedTech2 = buildTechnology(2L, "TypeScript", "JavaScript superset");

        when(technologyR2dbcRepository.findAll())
            .thenReturn(Flux.just(tech1, tech2));
        when(objectMapper.map(tech1, Technology.class))
            .thenReturn(mappedTech1);
        when(objectMapper.map(tech2, Technology.class))
            .thenReturn(mappedTech2);

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.findAll())
            .expectNext(mappedTech1)
            .expectNext(mappedTech2)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when no technologies exist")
    void testFindAll_Empty() {
        // Arrange
        when(technologyR2dbcRepository.findAll())
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(technologyRepositoryAdapter.findAll())
            .expectComplete()
            .verify();
    }

    // Helper methods
    private Technology buildTechnology(Long id, String name, String description) {
        return Technology.builder()
            .id(id)
            .name(name)
            .description(description)
            .build();
    }

    private TechnologyData buildTechnologyData(Long id, String name, String description) {
        return TechnologyData.builder()
            .id(id)
            .name(name)
            .description(description)
            .build();
    }
}
