package co.com.bancolombia.adapter.bootcampreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampReportResponse {
    @JsonProperty("bootcamp_id")
    private Long bootcampId;

    @JsonProperty("capacity_count")
    private Long capacityCount;

    @JsonProperty("technology_count")
    private Long technologyCount;

    @JsonProperty("registered_people")
    private Long registeredPeople;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
