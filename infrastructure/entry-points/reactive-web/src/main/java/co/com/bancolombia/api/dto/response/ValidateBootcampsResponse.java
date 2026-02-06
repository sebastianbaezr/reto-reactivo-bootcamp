package co.com.bancolombia.api.dto.response;

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
@Builder(toBuilder = true)
public class ValidateBootcampsResponse {
    private boolean allExist;
    private List<Long> existingIds;
    private List<Long> notFoundIds;
    private boolean hasDateConflicts;
}
