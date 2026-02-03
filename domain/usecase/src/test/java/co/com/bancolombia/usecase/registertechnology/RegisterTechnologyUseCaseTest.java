package co.com.bancolombia.usecase.registertechnology;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterTechnologyUseCase Tests")
class RegisterTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    private RegisterTechnologyUseCase registerTechnologyUseCase;

    @BeforeEach
    void setUp() {
        registerTechnologyUseCase = new RegisterTechnologyUseCase(technologyRepository);
    }

    @Test
    @DisplayName("Should save technology successfully when all validations pass")
    void testExecute_Success() {
        // Arrange
        Technology technology = createTechnology("Spring", "Java framework");
        Technology savedTechnology = createTechnology("Spring", "Java framework");
        savedTechnology.setId(1L);

        when(technologyRepository.existsByName("Spring"))
            .thenReturn(Mono.just(false));
        when(technologyRepository.save(any(Technology.class)))
            .thenReturn(Mono.just(savedTechnology));

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectNextMatches(t -> t.getId() != null)
            .verifyComplete();

        verify(technologyRepository).existsByName("Spring");
        verify(technologyRepository).save(any(Technology.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when name already exists")
    void testExecute_DuplicateName() {
        // Arrange
        Technology technology = createTechnology("React", "JS library");

        when(technologyRepository.existsByName("React"))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectError(BusinessException.class)
            .verify();

        verify(technologyRepository).existsByName("React");
    }

    @Test
    @DisplayName("Should throw BusinessException when name is null")
    void testExecute_NullName() {
        // Arrange
        Technology technology = createTechnology(null, "Valid description");

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when name exceeds max length")
    void testExecute_NameTooLong() {
        // Arrange
        String longName = "a".repeat(51);
        Technology technology = createTechnology(longName, "Valid description");

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when description is null")
    void testExecute_NullDescription() {
        // Arrange
        Technology technology = createTechnology("Python", null);

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when description exceeds max length")
    void testExecute_DescriptionTooLong() {
        // Arrange
        String longDescription = "a".repeat(91);
        Technology technology = createTechnology("Go", longDescription);

        // Act & Assert
        StepVerifier.create(registerTechnologyUseCase.execute(technology))
            .expectError(BusinessException.class)
            .verify();
    }

    // Helper method
    private Technology createTechnology(String name, String description) {
        Technology tech = new Technology();
        tech.setName(name);
        tech.setDescription(description);
        return tech;
    }
}
