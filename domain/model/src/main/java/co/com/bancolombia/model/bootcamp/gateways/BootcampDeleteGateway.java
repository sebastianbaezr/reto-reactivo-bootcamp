package co.com.bancolombia.model.bootcamp.gateways;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway interface for bootcamp deletion operations.
 * Handles local database operations for bootcamp deletion and restoration.
 */
public interface BootcampDeleteGateway {

    /**
     * Retrieves bootcamp information including capacity and technology IDs.
     *
     * @param bootcampId the bootcamp ID
     * @return mono containing bootcamp deletion info, or error if not found
     */
    Mono<BootcampDeleteInfo> getBootcampInfo(Long bootcampId);

    /**
     * Deletes a bootcamp and its relationships.
     * This is the point of no return in the saga - cannot be rolled back.
     *
     * @param bootcampId the bootcamp ID to delete
     * @return mono that completes when deletion is done, or error if deletion fails
     */
    Mono<Void> deleteBootcamp(Long bootcampId);

    /**
     * Restores a previously deleted bootcamp (for soft deletes).
     *
     * @param bootcampId the bootcamp ID to restore
     * @return mono that completes when restoration is done, or error if it fails
     */
    Mono<Void> restoreBootcamp(Long bootcampId);

    /**
     * Checks if a bootcamp exists.
     *
     * @param bootcampId the bootcamp ID
     * @return mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsById(Long bootcampId);

    /**
     * Information about a bootcamp needed for deletion.
     */
    interface BootcampDeleteInfo {

        /**
         * @return the bootcamp ID
         */
        Long getId();

        /**
         * @return the bootcamp name
         */
        String getName();

        /**
         * @return list of capacity IDs associated with this bootcamp
         */
        List<Long> getCapacityIds();

        /**
         * @return list of technology IDs used by the bootcamp's capacities
         */
        List<Long> getTechnologyIds();
    }
}
