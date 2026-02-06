package co.com.bancolombia.adapter.capacity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyCountsResponse {
    @JsonProperty("capacity_technology_counts")
    private Map<String, Long> capacityTechnologyCounts;
}
