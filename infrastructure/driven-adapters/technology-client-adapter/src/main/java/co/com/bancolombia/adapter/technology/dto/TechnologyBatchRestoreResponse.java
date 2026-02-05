package co.com.bancolombia.adapter.technology.dto;

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
public class TechnologyBatchRestoreResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("restoredCount")
    private Integer restoredCount;

    @JsonProperty("restoredIds")
    private List<Long> restoredIds;
}
