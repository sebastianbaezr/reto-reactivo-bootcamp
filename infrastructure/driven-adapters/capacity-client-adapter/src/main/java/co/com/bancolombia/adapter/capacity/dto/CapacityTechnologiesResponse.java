package co.com.bancolombia.adapter.capacity.dto;

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
public class CapacityTechnologiesResponse {

    private Long id;
    private String name;
    private List<TechnologyInfo> technologies;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TechnologyInfo {
        private Long id;
        private String name;
    }
}
