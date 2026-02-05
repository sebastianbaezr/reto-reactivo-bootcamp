package co.com.bancolombia.api.helper;

import co.com.bancolombia.api.dto.request.BootcampRequest;
import co.com.bancolombia.model.bootcamp.DeleteBootcampSaga;
import co.com.bancolombia.model.capacity.Capacity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BootcampHandlerHelper {

    public static boolean isSuccessfulSaga(DeleteBootcampSaga saga) {
        return saga.getStatus() != DeleteBootcampSaga.SagaStatus.ROLLED_BACK;
    }

    public static int countDeletedCapacities(DeleteBootcampSaga saga) {
        return saga.getCapacityIds() != null ? saga.getCapacityIds().size() : 0;
    }

    public static int countDeletedTechnologies(DeleteBootcampSaga saga) {
        return saga.getTechnologyIds() != null ? saga.getTechnologyIds().size() : 0;
    }

    public static Long calculateDurationMs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return ChronoUnit.MILLIS.between(startTime, endTime);
    }

    public static List<Capacity> buildCapacitiesFromRequest(BootcampRequest request) {
        return request.getCapacities().stream()
            .map(capacityId -> Capacity.builder().id(capacityId.longValue()).build())
            .toList();
    }
}
