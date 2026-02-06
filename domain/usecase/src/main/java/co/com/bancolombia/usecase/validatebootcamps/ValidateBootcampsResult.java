package co.com.bancolombia.usecase.validatebootcamps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateBootcampsResult {
    private List<Long> existingIds;
    private List<Long> notFoundIds;
    private boolean allExist;
    private boolean hasDateConflicts;
}
