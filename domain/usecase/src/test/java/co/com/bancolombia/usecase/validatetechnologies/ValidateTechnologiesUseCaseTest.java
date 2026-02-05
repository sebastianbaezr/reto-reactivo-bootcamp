package co.com.bancolombia.usecase.validatetechnologies;

import co.com.bancolombia.model.result.ValidateTechnologiesResult;
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
@DisplayName("ValidateTechnologiesUseCase Tests")
class ValidateTechnologiesUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    private ValidateTechnologiesUseCase validateTechnologiesUseCase;

    @BeforeEach
    void setUp() {
        validateTechnologiesUseCase = new ValidateTechnologiesUseCase(technologyRepository);
    }

    @Test
    @DisplayName("Should return empty result when ids is null")
    void testExecute_NullIds() {
        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(null))
            .assertNext(result -> {
                assert result.getExistingIds().isEmpty();
                assert result.getNotFoundIds().isEmpty();
                assert result.isAllExist();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty result when ids is empty")
    void testExecute_EmptyIds() {
        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(List.of()))
            .assertNext(result -> {
                assert result.getExistingIds().isEmpty();
                assert result.getNotFoundIds().isEmpty();
                assert result.isAllExist();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return all found when all ids exist")
    void testExecute_AllIdsExist() {
        // Arrange
        List<Long> ids = List.of(1L, 2L, 3L);

        when(technologyRepository.findExistingIdsByIds(ids))
            .thenReturn(Flux.just(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(ids))
            .assertNext(result -> {
                assert result.getExistingIds().size() == 3;
                assert result.getNotFoundIds().isEmpty();
                assert result.isAllExist();
            })
            .verifyComplete();

        verify(technologyRepository).findExistingIdsByIds(ids);
    }

    @Test
    @DisplayName("Should return found and not found ids when some ids don't exist")
    void testExecute_SomeIdsMissing() {
        // Arrange
        List<Long> ids = List.of(1L, 2L, 3L, 4L);

        when(technologyRepository.findExistingIdsByIds(ids))
            .thenReturn(Flux.just(1L, 3L));

        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(ids))
            .assertNext(result -> {
                assert result.getExistingIds().size() == 2;
                assert result.getExistingIds().contains(1L);
                assert result.getExistingIds().contains(3L);
                assert result.getNotFoundIds().size() == 2;
                assert result.getNotFoundIds().contains(2L);
                assert result.getNotFoundIds().contains(4L);
                assert !result.isAllExist();
            })
            .verifyComplete();

        verify(technologyRepository).findExistingIdsByIds(ids);
    }

    @Test
    @DisplayName("Should return all ids as not found when none exist")
    void testExecute_NoIdsFound() {
        // Arrange
        List<Long> ids = List.of(10L, 20L, 30L);

        when(technologyRepository.findExistingIdsByIds(ids))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(ids))
            .assertNext(result -> {
                assert result.getExistingIds().isEmpty();
                assert result.getNotFoundIds().size() == 3;
                assert result.getNotFoundIds().containsAll(ids);
                assert !result.isAllExist();
            })
            .verifyComplete();

        verify(technologyRepository).findExistingIdsByIds(ids);
    }

    @Test
    @DisplayName("Should handle single id when it exists")
    void testExecute_SingleIdExists() {
        // Arrange
        List<Long> ids = List.of(1L);

        when(technologyRepository.findExistingIdsByIds(ids))
            .thenReturn(Flux.just(1L));

        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(ids))
            .assertNext(result -> {
                assert result.getExistingIds().size() == 1;
                assert result.getExistingIds().contains(1L);
                assert result.getNotFoundIds().isEmpty();
                assert result.isAllExist();
            })
            .verifyComplete();

        verify(technologyRepository).findExistingIdsByIds(ids);
    }

    @Test
    @DisplayName("Should handle single id when it doesn't exist")
    void testExecute_SingleIdNotFound() {
        // Arrange
        List<Long> ids = List.of(1L);

        when(technologyRepository.findExistingIdsByIds(ids))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(validateTechnologiesUseCase.execute(ids))
            .assertNext(result -> {
                assert result.getExistingIds().isEmpty();
                assert result.getNotFoundIds().size() == 1;
                assert result.getNotFoundIds().contains(1L);
                assert !result.isAllExist();
            })
            .verifyComplete();

        verify(technologyRepository).findExistingIdsByIds(ids);
    }
}
