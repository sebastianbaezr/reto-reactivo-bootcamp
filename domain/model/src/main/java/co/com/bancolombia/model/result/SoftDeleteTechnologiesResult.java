package co.com.bancolombia.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SoftDeleteTechnologiesResult {
    private Integer deletedCount;
    private List<Long> requestedIds;
}
