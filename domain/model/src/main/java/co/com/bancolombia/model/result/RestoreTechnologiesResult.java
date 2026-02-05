package co.com.bancolombia.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RestoreTechnologiesResult {
    private Integer restoredCount;
    private List<Long> requestedIds;
}
