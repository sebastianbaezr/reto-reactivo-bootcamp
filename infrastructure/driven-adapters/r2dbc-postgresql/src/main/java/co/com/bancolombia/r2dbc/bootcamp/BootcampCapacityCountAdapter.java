package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.model.capacity.gateways.BootcampCapacityCountGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BootcampCapacityCountAdapter implements BootcampCapacityCountGateway {

    private final BootcampCapacityR2dbcRepository bootcampCapacityRepository;

    @Override
    public Mono<Map<Long, Integer>> getBootcampCountForCapacities(List<Long> capacityIds) {
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return bootcampCapacityRepository.findBootcampCapacitiesByCapacityIds(capacityIds)
                .collect(
                    () -> new java.util.HashMap<Long, Set<Long>>(),
                    (map, record) -> {
                        Long capacityId = record.getCapacityId();
                        Long bootcampId = record.getBootcampId();
                        map.computeIfAbsent(capacityId, k -> new HashSet<>()).add(bootcampId);
                    }
                )
                .map(groupedData ->
                    groupedData.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().size()
                        ))
                )
                .defaultIfEmpty(Map.of());
    }
}
