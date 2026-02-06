package co.com.bancolombia.model.capacity.gateways;

import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

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

    /**
     * Obtiene el conteo de tecnologías por capacidad
     * @param capacityIds Lista de IDs de capacidades
     * @return Mono con Map de capacityId -> technologyCount
     */
    Mono<Map<String, Long>> getTechnologyCountsByCapacityIds(List<Long> capacityIds);
}
