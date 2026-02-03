package co.com.bancolombia.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampListItemResponse {
    private Long id;
    private String name;
    private String description;
    private String releaseDate;
    private Integer duration;
    private Integer capacityCount;
    private List<CapacityWithTechnologiesResponse> capacities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CapacityWithTechnologiesResponse {
        private Long id;
        private String name;
        private List<TechnologyResponse> technologies;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TechnologyResponse {
        private Long id;
        private String name;
    }
}
