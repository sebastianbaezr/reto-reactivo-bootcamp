package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacityIdRequest {

    @NotNull(message = "El ID de la capacidad es obligatorio")
    @Positive(message = "El ID de la capacidad debe ser positivo")
    private Long id;
}
