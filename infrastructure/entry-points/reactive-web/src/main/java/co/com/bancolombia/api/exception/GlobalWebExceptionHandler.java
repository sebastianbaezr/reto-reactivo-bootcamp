package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.response.ErrorResponse;
import co.com.bancolombia.model.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private static final String LOGGER_PREFIX = "[GlobalWebExceptionHandler]";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("{} Exception occurred: {}", LOGGER_PREFIX, ex.getMessage(), ex);

        if (ex instanceof BusinessException) {
            return handleBusinessException(exchange, (BusinessException) ex);
        }

        return handleGenericException(exchange, ex);
    }

    private Mono<Void> handleBusinessException(ServerWebExchange exchange, BusinessException ex) {
        HttpStatus status = determineStatusFromBusinessException(ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .timestamp(System.currentTimeMillis())
            .details(new ArrayList<>())
            .build();

        return writeResponse(exchange, status, errorResponse);
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = "INTERNAL_ERROR";
        String message = "Error interno del servidor";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .details(new ArrayList<>())
            .build();

        return writeResponse(exchange, status, errorResponse);
    }

    private HttpStatus determineStatusFromBusinessException(BusinessException ex) {
        return switch (ex.getCode()) {
            case "TECHNOLOGY_NAME_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, ErrorResponse errorResponse) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonResponse = convertToJsonString(errorResponse);
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes()))
            );
        } catch (Exception e) {
            log.error("{} Error writing response", LOGGER_PREFIX, e);
            return Mono.empty();
        }
    }

    private String convertToJsonString(ErrorResponse errorResponse) {
        return String.format(
            "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d,\"details\":[]}",
            escapeJson(errorResponse.getCode()),
            escapeJson(errorResponse.getMessage()),
            errorResponse.getTimestamp()
        );
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
