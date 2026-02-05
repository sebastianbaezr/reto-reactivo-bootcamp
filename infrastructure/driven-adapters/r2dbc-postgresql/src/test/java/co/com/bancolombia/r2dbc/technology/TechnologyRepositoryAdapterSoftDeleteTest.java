package co.com.bancolombia.r2dbc.technology;

import co.com.bancolombia.model.technology.Technology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnologyRepositoryAdapterSoftDeleteTest {

    @Mock
    private TechnologyR2dbcRepository r2dbcRepository;

    @Mock
    private ObjectMapper objectMapper;

    private TechnologyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TechnologyRepositoryAdapter(r2dbcRepository, objectMapper);
    }

    @Test
    void testSoftDeleteByIds_Success() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(r2dbcRepository.softDeleteByIds(ids.toArray(new Long[0])))
                .thenReturn(Mono.just(3));

        StepVerifier.create(adapter.softDeleteByIds(ids))
                .expectNext(3)
                .verifyComplete();

        verify(r2dbcRepository).softDeleteByIds(ids.toArray(new Long[0]));
    }

    @Test
    void testSoftDeleteByIds_NoRowsAffected() {
        List<Long> ids = List.of(999L);

        when(r2dbcRepository.softDeleteByIds(ids.toArray(new Long[0])))
                .thenReturn(Mono.just(0));

        StepVerifier.create(adapter.softDeleteByIds(ids))
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    void testRestoreByIds_Success() {
        List<Long> ids = List.of(1L, 2L);

        when(r2dbcRepository.restoreByIds(ids.toArray(new Long[0])))
                .thenReturn(Mono.just(2));

        StepVerifier.create(adapter.restoreByIds(ids))
                .expectNext(2)
                .verifyComplete();

        verify(r2dbcRepository).restoreByIds(ids.toArray(new Long[0]));
    }

    @Test
    void testRestoreByIds_NoRowsAffected() {
        List<Long> ids = List.of(999L);

        when(r2dbcRepository.restoreByIds(ids.toArray(new Long[0])))
                .thenReturn(Mono.just(0));

        StepVerifier.create(adapter.restoreByIds(ids))
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    void testFindActiveByIds_Success() {
        List<Long> ids = List.of(1L, 2L);
        TechnologyData data1 = TechnologyData.builder().id(1L).name("Java").description("Language").build();
        TechnologyData data2 = TechnologyData.builder().id(2L).name("Python").description("Language").build();
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();
        Technology tech2 = Technology.builder().id(2L).name("Python").description("Language").build();

        when(r2dbcRepository.findActiveByIds(ids.toArray(new Long[0])))
                .thenReturn(Flux.just(data1, data2));
        when(objectMapper.map(data1, Technology.class)).thenReturn(tech1);
        when(objectMapper.map(data2, Technology.class)).thenReturn(tech2);

        StepVerifier.create(adapter.findActiveByIds(ids))
                .expectNext(tech1, tech2)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByIds(ids.toArray(new Long[0]));
    }

    @Test
    void testFindDeletedByIds_Success() {
        List<Long> ids = List.of(1L, 2L);
        TechnologyData data1 = TechnologyData.builder().id(1L).name("Java").description("Language").build();
        TechnologyData data2 = TechnologyData.builder().id(2L).name("Python").description("Language").build();
        data1.setDeletedAt(LocalDateTime.now());
        data2.setDeletedAt(LocalDateTime.now());
        Technology tech1 = Technology.builder().id(1L).name("Java").description("Language").build();
        Technology tech2 = Technology.builder().id(2L).name("Python").description("Language").build();

        when(r2dbcRepository.findDeletedByIds(ids.toArray(new Long[0])))
                .thenReturn(Flux.just(data1, data2));
        when(objectMapper.map(data1, Technology.class)).thenReturn(tech1);
        when(objectMapper.map(data2, Technology.class)).thenReturn(tech2);

        StepVerifier.create(adapter.findDeletedByIds(ids))
                .expectNext(tech1, tech2)
                .verifyComplete();

        verify(r2dbcRepository).findDeletedByIds(ids.toArray(new Long[0]));
    }

    @Test
    void testFindDeletedByIds_Empty() {
        List<Long> ids = List.of(999L);

        when(r2dbcRepository.findDeletedByIds(ids.toArray(new Long[0])))
                .thenReturn(Flux.empty());

        StepVerifier.create(adapter.findDeletedByIds(ids))
                .verifyComplete();
    }
}
