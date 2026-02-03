package co.com.bancolombia.model.capacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacityDetail {
    private Long id;
    private String name;
    private List<Technology> technologies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Technology {
        private Long id;
        private String name;
    }
}
