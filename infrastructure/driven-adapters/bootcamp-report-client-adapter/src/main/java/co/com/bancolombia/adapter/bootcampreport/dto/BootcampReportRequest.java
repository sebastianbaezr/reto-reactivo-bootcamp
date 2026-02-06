package co.com.bancolombia.adapter.bootcampreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampReportRequest {
    @JsonProperty("capacity_count")
    private Long capacityCount;

    @JsonProperty("technology_count")
    private Long technologyCount;
}
