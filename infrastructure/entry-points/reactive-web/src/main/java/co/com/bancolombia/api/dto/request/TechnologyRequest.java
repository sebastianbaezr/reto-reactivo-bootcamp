package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TechnologyRequest {

    @NotBlank(message = "The name is required")
    @Size(max = 50, message = "The name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "The description is required")
    @Size(max = 90, message = "The description must not exceed 90 characters")
    private String description;
}
