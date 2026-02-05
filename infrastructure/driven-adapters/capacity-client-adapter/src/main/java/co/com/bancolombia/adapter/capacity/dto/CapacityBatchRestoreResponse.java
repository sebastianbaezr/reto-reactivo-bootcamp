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
public class CapacityBatchRestoreResponse {
    @JsonProperty("capacities_restored")
    private List<Long> capacities_restored;

    @JsonProperty("restored_count")
    private Integer restored_count;
}
