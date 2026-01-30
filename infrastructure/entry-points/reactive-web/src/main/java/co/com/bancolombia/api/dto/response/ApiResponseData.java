package co.com.bancolombia.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseData<T> {
    private final T data;

    public static <T> ApiResponseData<T> of(T data) {
        return new ApiResponseData<>(data);
    }
}
