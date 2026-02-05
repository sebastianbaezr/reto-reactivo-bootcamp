package co.com.bancolombia.adapter.capacity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyCapacityCountResponse {

    @JsonProperty("capacity_count")
    private Integer capacityCount;

    @JsonProperty("related_technology_ids")
    private List<Long> relatedTechnologyIds;

    @JsonProperty("technology_id")
    private Long technologyId;
}
