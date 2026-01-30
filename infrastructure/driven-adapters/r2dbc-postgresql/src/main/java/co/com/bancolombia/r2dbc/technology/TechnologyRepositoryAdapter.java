package co.com.bancolombia.r2dbc.technology;

import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

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
}
