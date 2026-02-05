package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;

public class TechnologyValidator {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 90;

    private TechnologyValidator() {
        // Utility class
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainErrorCode.NAME_REQUIRED);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(
                DomainErrorCode.INVALID_NAME_LENGTH,
                "The name must not exceed " + MAX_NAME_LENGTH + " characters"
            );
        }
    }

    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException(DomainErrorCode.DESCRIPTION_REQUIRED);
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(
                DomainErrorCode.INVALID_DESCRIPTION_LENGTH,
                "The description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters"
            );
        }
    }
}
