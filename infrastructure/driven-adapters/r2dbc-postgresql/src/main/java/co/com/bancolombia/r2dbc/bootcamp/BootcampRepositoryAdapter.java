package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityDetail;
import co.com.bancolombia.model.capacity.gateways.CapacityDetailGateway;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BootcampRepositoryAdapter extends ReactiveAdapterOperations<Bootcamp, BootcampData, Long, BootcampR2dbcRepository>
        implements BootcampRepository {

    private final BootcampCapacityR2dbcRepository bootcampCapacityRepository;
    private final CapacityDetailGateway capacityDetailGateway;

    public BootcampRepositoryAdapter(
            BootcampR2dbcRepository repository,
            BootcampCapacityR2dbcRepository bootcampCapacityRepository,
            CapacityDetailGateway capacityDetailGateway,
            ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Bootcamp.class));
        this.bootcampCapacityRepository = bootcampCapacityRepository;
        this.capacityDetailGateway = capacityDetailGateway;
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
            .flatMap(bootcampData -> {
                System.out.println("BootcampData found: " + bootcampData.getId());
                return bootcampCapacityRepository.findCapacityIdsByBootcampId(id)
                    .collectList()
                    .doOnNext(capacityIds -> System.out.println("Capacity IDs from DB: " + capacityIds))
                    .map(capacityIds -> {
                        bootcampData.setCapacityIds(capacityIds);
                        return bootcampData;
                    });
            })
            .map(bootcampData -> {
                Bootcamp bootcamp = toEntity(bootcampData);
                List<Capacity> capacities = bootcampData.getCapacityIds().stream()
                    .map(capId -> Capacity.builder().id(capId).build())
                    .toList();
                bootcamp.setCapacities(capacities);
                System.out.println("Bootcamp entity capacities after mapping: " + capacities.size());
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

    @Override
    public Mono<Page<Bootcamp>> findAllWithPagination(PageRequest pageRequest) {
        return count()
            .flatMap(totalElements -> getBootcampsWithSorting(pageRequest)
                .collectList()
                .flatMap(bootcampDataList -> processBootcampsList(bootcampDataList, pageRequest, totalElements)));
    }

    private Flux<BootcampData> getBootcampsWithSorting(PageRequest pageRequest) {
        int limit = pageRequest.getSize();
        long offset = pageRequest.getOffset();
        String sortBy = pageRequest.getSortBy();
        SortDirection direction = pageRequest.getSortDirection();

        if ("capacityCount".equals(sortBy)) {
            return direction == SortDirection.ASC
                ? repository.findAllOrderByCapacityCountAsc(limit, offset)
                : repository.findAllOrderByCapacityCountDesc(limit, offset);
        } else {
            return direction == SortDirection.ASC
                ? repository.findAllOrderByNameAsc(limit, offset)
                : repository.findAllOrderByNameDesc(limit, offset);
        }
    }

    private Mono<Long> count() {
        return repository.countAll();
    }

    private Mono<Page<Bootcamp>> processBootcampsList(
            List<BootcampData> bootcampDataList,
            PageRequest pageRequest,
            long totalElements) {
        return bootcampDataList.isEmpty()
            ? Mono.just(buildEmptyPage(pageRequest, totalElements))
            : loadAndMapCapacities(bootcampDataList, pageRequest, totalElements);
    }

    private Mono<Page<Bootcamp>> loadAndMapCapacities(
            List<BootcampData> bootcampDataList,
            PageRequest pageRequest,
            long totalElements) {
        List<Long> bootcampIds = bootcampDataList.stream()
            .map(BootcampData::getId)
            .toList();

        return loadAllCapacitiesForBootcamps(bootcampIds)
            .collectList()
            .flatMap(capacityMappings -> enrichWithCapacityDetails(bootcampDataList, capacityMappings, pageRequest, totalElements));
    }

    private Mono<Page<Bootcamp>> enrichWithCapacityDetails(
            List<BootcampData> bootcampDataList,
            List<BootcampCapacityData> capacityMappings,
            PageRequest pageRequest,
            long totalElements) {

        List<Long> capacityIds = capacityMappings.stream()
            .map(BootcampCapacityData::getCapacityId)
            .distinct()
            .toList();

        return capacityDetailGateway.getCapacitiesByIds(capacityIds)
            .collectMap(CapacityDetail::getId)
            .map(capacityDetailsMap -> buildBootcampPage(bootcampDataList, capacityMappings, capacityDetailsMap, pageRequest, totalElements));
    }

    private Page<Bootcamp> buildBootcampPage(
            List<BootcampData> bootcampDataList,
            List<BootcampCapacityData> capacityMappings,
            Map<Long, CapacityDetail> capacityDetailsMap,
            PageRequest pageRequest,
            long totalElements) {

        List<Bootcamp> bootcamps = bootcampDataList.stream()
            .map(bootcampData -> {
                List<Long> capacityIds = capacityMappings.stream()
                    .filter(cm -> cm.getBootcampId().equals(bootcampData.getId()))
                    .map(BootcampCapacityData::getCapacityId)
                    .toList();

                bootcampData.setCapacityIds(capacityIds);
                Bootcamp bootcamp = toEntity(bootcampData);

                List<Capacity> capacities = capacityIds.stream()
                    .map(id -> {
                        CapacityDetail detail = capacityDetailsMap.get(id);
                        if (detail != null) {
                            List<Capacity.Technology> technologies = detail.getTechnologies().stream()
                                .map(tech -> Capacity.Technology.builder()
                                    .id(tech.getId())
                                    .name(tech.getName())
                                    .build())
                                .toList();

                            return Capacity.builder()
                                .id(id)
                                .name(detail.getName())
                                .technologies(technologies)
                                .build();
                        }
                        return Capacity.builder().id(id).build();
                    })
                    .toList();
                bootcamp.setCapacities(capacities);

                return bootcamp;
            })
            .toList();

        return buildPage(bootcamps, pageRequest, totalElements);
    }

    private Flux<BootcampCapacityData> loadAllCapacitiesForBootcamps(List<Long> bootcampIds) {
        return Flux.fromIterable(bootcampIds)
            .flatMap(bootcampCapacityRepository::findCapacitiesByBootcampId);
    }

    private Page<Bootcamp> buildEmptyPage(PageRequest pageRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());
        return Page.<Bootcamp>builder()
            .content(List.of())
            .currentPage(pageRequest.getPage())
            .totalPages(totalPages)
            .totalElements(totalElements)
            .pageSize(pageRequest.getSize())
            .sortBy(pageRequest.getSortBy())
            .sortDirection(pageRequest.getSortDirection())
            .hasNextPage(pageRequest.getPage() + 1 < totalPages)
            .hasPreviousPage(pageRequest.getPage() > 0)
            .build();
    }

    private Page<Bootcamp> buildPage(
            List<Bootcamp> content,
            PageRequest pageRequest,
            long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());

        return Page.<Bootcamp>builder()
            .content(content)
            .currentPage(pageRequest.getPage())
            .totalPages(totalPages)
            .totalElements(totalElements)
            .pageSize(pageRequest.getSize())
            .sortBy(pageRequest.getSortBy())
            .sortDirection(pageRequest.getSortDirection())
            .hasNextPage(pageRequest.getPage() + 1 < totalPages)
            .hasPreviousPage(pageRequest.getPage() > 0)
            .build();
    }

    @Override
    public Flux<Bootcamp> findByIds(List<Long> ids) {
        log.debug("Finding bootcamps by ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        Long[] idsArray = ids.toArray(new Long[0]);
        return repository.findByIds(idsArray)
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
