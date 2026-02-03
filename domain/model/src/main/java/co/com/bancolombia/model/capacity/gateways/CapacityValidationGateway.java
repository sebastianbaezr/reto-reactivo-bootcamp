package co.com.bancolombia.model.capacity.gateways;

import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Gateway para validar capacidades con el microservicio externo
 */
public interface CapacityValidationGateway {
    /**
     * Valida que todas las capacidades existan en el microservicio externo
     * @param capacityIds Lista de IDs de capacidades a validar
     * @return Mono<Boolean> true si todas son válidas, false si alguna no existe
     */
    Mono<Boolean> validateCapacities(List<Long> capacityIds);
}
