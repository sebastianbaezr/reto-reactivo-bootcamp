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
public class CapacityBatchDeleteResponse {
    @JsonProperty("capacities_deleted")
    private List<Long> capacities_deleted;

    @JsonProperty("deleted_count")
    private Integer deleted_count;
}
