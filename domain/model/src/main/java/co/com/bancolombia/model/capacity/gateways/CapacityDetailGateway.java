package co.com.bancolombia.model.capacity.gateways;

import co.com.bancolombia.model.capacity.CapacityDetail;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CapacityDetailGateway {
    Flux<CapacityDetail> getCapacitiesByIds(List<Long> ids);
}
