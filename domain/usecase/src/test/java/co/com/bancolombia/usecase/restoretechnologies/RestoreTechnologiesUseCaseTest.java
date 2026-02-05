package co.com.bancolombia.usecase.restoretechnologies;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.result.RestoreTechnologiesResult;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestoreTechnologiesUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    private RestoreTechnologiesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RestoreTechnologiesUseCase(technologyRepository);
    }

    @Test
    void testExecute_Success() {
        List<Long> ids = List.of(1L, 2L);
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();
        Technology tech2 = Technology.builder().id(2L).name("Python").description("Language").build();
        tech1.setDeletedAt(LocalDateTime.now());
        tech2.setDeletedAt(LocalDateTime.now());

        when(technologyRepository.findDeletedByIds(ids))
                .thenReturn(Flux.just(tech1, tech2));
        when(technologyRepository.restoreByIds(ids))
                .thenReturn(Mono.just(2));

        StepVerifier.create(useCase.execute(ids))
                .assertNext(result -> {
                    assert result.getRestoredCount().equals(2);
                    assert result.getRequestedIds().equals(ids);
                })
                .verifyComplete();

        verify(technologyRepository).findDeletedByIds(ids);
        verify(technologyRepository).restoreByIds(ids);
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
    void testExecute_NotDeleted() {
        List<Long> ids = List.of(999L, 1000L);

        when(technologyRepository.findDeletedByIds(ids))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(ids))
                .expectError(BusinessException.class)
                .verify();

        verify(technologyRepository).findDeletedByIds(ids);
        verify(technologyRepository, never()).restoreByIds(ids);
    }

    @Test
    void testExecute_PartialNotDeleted() {
        List<Long> ids = List.of(1L, 2L);
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();
        tech1.setDeletedAt(LocalDateTime.now());

        when(technologyRepository.findDeletedByIds(ids))
                .thenReturn(Flux.just(tech1));

        StepVerifier.create(useCase.execute(ids))
                .expectError(BusinessException.class)
                .verify();

        verify(technologyRepository).findDeletedByIds(ids);
        verify(technologyRepository, never()).restoreByIds(ids);
    }
}
