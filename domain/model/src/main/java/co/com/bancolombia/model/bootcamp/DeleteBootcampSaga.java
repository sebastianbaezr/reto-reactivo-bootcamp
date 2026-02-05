package co.com.bancolombia.model.bootcamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of a bootcamp deletion saga.
 * Uses immutable state transitions with builder pattern to track saga execution.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DeleteBootcampSaga {

    private Long bootcampId;
    private String bootcampName;
    private List<Long> capacityIds;
    private List<Long> technologyIds;

    private SagaStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private StepResult bootcampDeleteResult;
    private StepResult capacityDeleteResult;
    private StepResult technologyDeleteResult;

    private RollbackInfo rollbackInfo;

    /**
     * Saga execution status machine.
     */
    public enum SagaStatus {
        PENDING,
        BOOTCAMP_DELETING,
        BOOTCAMP_DELETED,
        CAPACITIES_DELETING,
        CAPACITIES_DELETED,
        TECHNOLOGIES_DELETING,
        TECHNOLOGIES_DELETED,
        COMPLETED,
        ROLLING_BACK,
        ROLLED_BACK,
        FAILED
    }

    /**
     * Result of a single saga step.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class StepResult {
        private boolean success;
        private String errorMessage;
        private int retryCount;
    }

    /**
     * Information about what needs to be restored during rollback.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class RollbackInfo {
        @Builder.Default
        private List<Long> capacitiesToRestore = new ArrayList<>();
        @Builder.Default
        private List<Long> technologiesToRestore = new ArrayList<>();
        private boolean bootcampDeleted;
    }
}
