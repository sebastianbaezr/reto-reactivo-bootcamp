package co.com.bancolombia.usecase.softdeletetechnologies;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.result.SoftDeleteTechnologiesResult;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftDeleteTechnologiesUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    private SoftDeleteTechnologiesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SoftDeleteTechnologiesUseCase(technologyRepository);
    }

    @Test
    void testExecute_Success() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();
        Technology tech2 = Technology.builder().id(2L).name("Python").description("Language").build();
        Technology tech3 = Technology.builder().id(3L).name("Go").description("Language").build();

        when(technologyRepository.findActiveByIds(ids))
                .thenReturn(Flux.just(tech1, tech2, tech3));
        when(technologyRepository.softDeleteByIds(ids))
                .thenReturn(Mono.just(3));

        StepVerifier.create(useCase.execute(ids))
                .assertNext(result -> {
                    assert result.getDeletedCount().equals(3);
                    assert result.getRequestedIds().equals(ids);
                })
                .verifyComplete();

        verify(technologyRepository).findActiveByIds(ids);
        verify(technologyRepository).softDeleteByIds(ids);
    }

    @Test
    void testExecute_EmptyList() {
        StepVerifier.create(useCase.execute(List.of()))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void testExecute_NullList() {
        StepVerifier.create(useCase.execute(null))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void testExecute_NotFound() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();

        when(technologyRepository.findActiveByIds(ids))
                .thenReturn(Flux.just(tech1));

        StepVerifier.create(useCase.execute(ids))
                .expectError(BusinessException.class)
                .verify();

        verify(technologyRepository).findActiveByIds(ids);
        verify(technologyRepository, never()).softDeleteByIds(ids);
    }

    @Test
    void testExecute_AlreadyDeleted() {
        List<Long> ids = List.of(999L, 1000L);

        when(technologyRepository.findActiveByIds(ids))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(ids))
                .expectError(BusinessException.class)
                .verify();

        verify(technologyRepository).findActiveByIds(ids);
        verify(technologyRepository, never()).softDeleteByIds(ids);
    }
}
