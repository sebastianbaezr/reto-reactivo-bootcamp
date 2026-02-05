package co.com.bancolombia.adapter.capacity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyCapacityCountsResponse {
    @JsonProperty("technology_counts")
    private Map<String, Integer> technology_counts;

    public Map<Long, Integer> getTechnologyCounts() {
        if (technology_counts == null) {
            return Map.of();
        }
        return technology_counts.entrySet().stream()
            .collect(Collectors.toMap(
                e -> Long.parseLong(e.getKey()),
                Map.Entry::getValue
            ));
    }
}
