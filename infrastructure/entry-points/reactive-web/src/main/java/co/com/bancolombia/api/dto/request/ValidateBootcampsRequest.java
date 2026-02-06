package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
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
public class ValidateBootcampsRequest {

    @NotEmpty(message = "La lista de IDs de bootcamps no puede estar vacía")
    private List<Long> ids;
}
