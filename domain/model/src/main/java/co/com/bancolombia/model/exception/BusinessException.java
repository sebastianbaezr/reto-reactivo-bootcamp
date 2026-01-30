package co.com.bancolombia.model.exception;

import co.com.bancolombia.model.enums.DomainErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1001L;
    private final String code;

    public BusinessException(DomainErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(DomainErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
