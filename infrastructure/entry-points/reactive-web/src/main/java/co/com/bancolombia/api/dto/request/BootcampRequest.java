package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BootcampRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no debe exceder 200 caracteres")
    private String description;

    @NotNull(message = "La fecha de lanzamiento es obligatoria")
    @Future(message = "La fecha de lanzamiento debe ser futura")
    private LocalDate releaseDate;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    private Integer duration;

    @NotEmpty(message = "Debe especificar al menos una capacidad")
    @Size(min = 1, max = 20, message = "Debe tener entre 1 y 20 capacidades")
    private List<@Positive(message = "Los IDs de capacidades deben ser positivos") Integer> capacities;
}
