package co.com.bancolombia.model.technology.gateways;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Gateway interface for technology deletion operations.
 * Handles integration with external Technology Service for deletion and restoration.
 */
public interface TechnologyDeleteGateway {

    /**
     * Gets the count of capacities using each technology.
     * This determines if a technology can be safely deleted.
     *
     * @param technologyIds list of technology IDs to check
     * @return mono containing a map of technology ID to capacity count
     */
    Mono<Map<Long, Integer>> getCapacityCountForTechnologies(List<Long> technologyIds);

    /**
     * Deletes technologies from the external Technology Service.
     * Technologies should only be deleted if they are not in use (capacity count = 0).
     *
     * @param technologyIds list of technology IDs to delete
     * @return mono that completes when deletion is done, or error if deletion fails
     */
    Mono<Void> deleteTechnologies(List<Long> technologyIds);

    /**
     * Restores previously deleted technologies.
     * Used during saga rollback when technology deletion fails.
     *
     * @param technologyIds list of technology IDs to restore
     * @return mono that completes when restoration is done, or error if restoration fails
     */
    Mono<Void> restoreTechnologies(List<Long> technologyIds);
}
