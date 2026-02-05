package co.com.bancolombia.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTechnologiesResult {
    private List<Long> existingIds;
    private List<Long> notFoundIds;
    private boolean allExist;
}
