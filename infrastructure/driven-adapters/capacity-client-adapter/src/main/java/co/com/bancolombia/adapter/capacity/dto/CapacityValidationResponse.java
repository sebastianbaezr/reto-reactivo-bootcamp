package co.com.bancolombia.adapter.capacity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacityValidationResponse {
    private Boolean allExist;
    private List<Long> existingIds;
    private List<Long> notFoundIds;
}
