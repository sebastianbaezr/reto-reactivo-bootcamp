package co.com.bancolombia.model.capacity.gateways;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampCapacityCountGateway {
    Mono<Map<Long, Integer>> getBootcampCountForCapacities(List<Long> capacityIds);
}
