package co.com.bancolombia.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for bootcamp deletion.
 * Contains details about the saga execution including status, timing, and results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampDeleteResponse {

    @JsonProperty("bootcamp_id")
    private Long bootcampId;

    @JsonProperty("bootcamp_name")
    private String bootcampName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("duration_ms")
    private Long durationMs;

    @JsonProperty("bootcamp_deleted")
    private Boolean bootcampDeleted;

    @JsonProperty("capacities_deleted")
    private Integer capacitiesDeleted;

    @JsonProperty("technologies_deleted")
    private Integer technologiesDeleted;
}
