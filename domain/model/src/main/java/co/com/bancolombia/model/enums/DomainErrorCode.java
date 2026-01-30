package co.com.bancolombia.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {
    TECHNOLOGY_NAME_ALREADY_EXISTS("TECHNOLOGY_NAME_ALREADY_EXISTS", "Ya existe una tecnología con este nombre"),
    INVALID_NAME_LENGTH("INVALID_NAME_LENGTH", "El nombre de la tecnología no debe exceder 50 caracteres"),
    INVALID_DESCRIPTION_LENGTH("INVALID_DESCRIPTION_LENGTH", "La descripción de la tecnología no debe exceder 90 caracteres"),
    DESCRIPTION_REQUIRED("DESCRIPTION_REQUIRED", "La descripción es obligatoria"),
    NAME_REQUIRED("NAME_REQUIRED", "El nombre es obligatorio");

    private final String code;
    private final String message;
}
