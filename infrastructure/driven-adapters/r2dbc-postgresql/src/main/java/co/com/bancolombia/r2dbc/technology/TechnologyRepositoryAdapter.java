package co.com.bancolombia.r2dbc.technology;

import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class TechnologyRepositoryAdapter extends ReactiveAdapterOperations<Technology, TechnologyData, Long, TechnologyR2dbcRepository>
        implements TechnologyRepository {

    public TechnologyRepositoryAdapter(TechnologyR2dbcRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Technology.class));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Flux<Long> findExistingIdsByIds(List<Long> ids) {
        return repository.findExistingIdsByIds(ids.toArray(new Long[0]));
    }

    @Override
    public Flux<Technology> findByIds(List<Long> ids) {
        return repository.findByIds(ids.toArray(new Long[0]))
                .map(data -> mapper.map(data, Technology.class));
    }
}
