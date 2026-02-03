package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BootcampRepositoryAdapter extends ReactiveAdapterOperations<Bootcamp, BootcampData, Long, BootcampR2dbcRepository>
        implements BootcampRepository {

    private final BootcampCapacityR2dbcRepository bootcampCapacityRepository;

    public BootcampRepositoryAdapter(
            BootcampR2dbcRepository repository,
            BootcampCapacityR2dbcRepository bootcampCapacityRepository,
            ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Bootcamp.class));
        this.bootcampCapacityRepository = bootcampCapacityRepository;
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        log.debug("Saving bootcamp: {}", bootcamp.getName());

        List<Long> capacityIds = bootcamp.getCapacities().stream()
            .map(Capacity::getId)
            .collect(Collectors.toList());

        BootcampData bootcampData = toData(bootcamp);
        bootcampData.setCapacityIds(capacityIds);

        return repository.save(bootcampData)
            .flatMap(savedBootcamp ->
                saveBootcampCapacities(savedBootcamp.getId(), capacityIds)
                    .thenReturn(savedBootcamp)
            )
            .map(this::toEntity)
            .map(entity -> {
                entity.setCapacities(bootcamp.getCapacities());
                return entity;
            })
            .doOnSuccess(b -> log.info("Bootcamp saved successfully: {}", b.getId()));
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        log.debug("Finding bootcamp by id: {}", id);

        return repository.findById(id)
            .flatMap(bootcampData ->
                bootcampCapacityRepository.findCapacityIdsByBootcampId(id)
                    .collectList()
                    .map(capacityIds -> {
                        bootcampData.setCapacityIds(capacityIds);
                        return bootcampData;
                    })
            )
            .map(this::toEntity)
            .map(bootcamp -> {
                List<Capacity> capacities = bootcamp.getCapacities() != null
                    ? bootcamp.getCapacities()
                    : List.of();
                bootcamp.setCapacities(capacities);
                return bootcamp;
            });
    }

    @Override
    public Flux<Bootcamp> findAll() {
        log.debug("Finding all bootcamps");

        return repository.findAll()
            .flatMap(bootcampData ->
                bootcampCapacityRepository.findCapacityIdsByBootcampId(bootcampData.getId())
                    .collectList()
                    .map(capacityIds -> {
                        bootcampData.setCapacityIds(capacityIds);
                        return bootcampData;
                    })
            )
            .map(this::toEntity);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    /**
     * Guarda las relaciones bootcamp-capacidades
     */
    private Mono<Void> saveBootcampCapacities(Long bootcampId, List<Long> capacityIds) {
        return Flux.fromIterable(capacityIds)
            .map(capacityId -> BootcampCapacityData.builder()
                .bootcampId(bootcampId)
                .capacityId(capacityId)
                .build())
            .collectList()
            .flatMapMany(bootcampCapacityRepository::saveAll)
            .then();
    }
}
