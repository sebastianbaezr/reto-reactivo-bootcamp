package co.com.bancolombia.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ErrorResponse {
    private String code;
    private String message;
    private long timestamp;
    private List<String> details;
}
