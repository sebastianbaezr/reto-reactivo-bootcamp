package co.com.bancolombia.model.capacity.gateways;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Gateway interface for capacity deletion operations.
 * Handles integration with external Capacity Service for deletion and restoration.
 */
public interface CapacityDeleteGateway {

    /**
     * Gets the count of bootcamps using each capacity.
     * This determines if a capacity can be safely deleted.
     *
     * @param capacityIds list of capacity IDs to check
     * @return mono containing a map of capacity ID to bootcamp count
     */
    Mono<Map<Long, Integer>> getBootcampCountForCapacities(List<Long> capacityIds);

    /**
     * Gets the count of capacities using each technology.
     * This determines if a technology can be safely deleted.
     *
     * @param technologyIds list of technology IDs to check
     * @return mono containing a map of technology ID to capacity count
     */
    Mono<Map<Long, Integer>> getCapacityCountForTechnologies(List<Long> technologyIds);

    /**
     * Deletes capacities from the external Capacity Service.
     * Capacities should only be deleted if they are not in use (bootcamp count = 0).
     *
     * @param capacityIds list of capacity IDs to delete
     * @return mono that completes when deletion is done, or error if deletion fails
     */
    Mono<Void> deleteCapacities(List<Long> capacityIds);

    /**
     * Restores previously deleted capacities.
     * Used during saga rollback when capacity deletion fails.
     *
     * @param capacityIds list of capacity IDs to restore
     * @return mono that completes when restoration is done, or error if restoration fails
     */
    Mono<Void> restoreCapacities(List<Long> capacityIds);

    /**
     * Gets the technologies that use a specific capacity.
     * This is used to identify technologies related to capacities being deleted.
     *
     * @param capacityId the capacity ID to check
     * @return mono containing a list of technology IDs that use this capacity
     */
    Mono<List<Long>> getTechnologiesByCapacityId(Long capacityId);
}
