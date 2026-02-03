package co.com.bancolombia.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListBootcampsRequest {
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortOrder;
}
