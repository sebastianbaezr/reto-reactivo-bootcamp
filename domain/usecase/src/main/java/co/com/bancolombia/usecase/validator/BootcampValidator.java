package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;

import java.time.LocalDate;
import java.util.List;

public class BootcampValidator {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MIN_CAPACITIES = 1;
    private static final int MAX_CAPACITIES = 20;

    private BootcampValidator() {
        // Utility class
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_NAME_REQUIRED);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(
                DomainErrorCode.BOOTCAMP_INVALID_NAME_LENGTH,
                "El nombre del bootcamp no debe exceder " + MAX_NAME_LENGTH + " caracteres"
            );
        }
    }

    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_DESCRIPTION_REQUIRED);
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(
                DomainErrorCode.BOOTCAMP_INVALID_DESCRIPTION_LENGTH,
                "La descripción del bootcamp no debe exceder " + MAX_DESCRIPTION_LENGTH + " caracteres"
            );
        }
    }

    public static void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_RELEASE_DATE_REQUIRED);
        }
        if (releaseDate.isBefore(LocalDate.now())) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_RELEASE_DATE_PAST);
        }
    }

    public static void validateDuration(Integer duration) {
        if (duration == null) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_DURATION_REQUIRED);
        }
        if (duration <= 0) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_DURATION_INVALID);
        }
    }

    public static void validateCapacities(List<Capacity> capacities) {
        if (capacities == null || capacities.isEmpty()) {
            throw new BusinessException(DomainErrorCode.BOOTCAMP_CAPACITIES_REQUIRED);
        }
        if (capacities.size() < MIN_CAPACITIES) {
            throw new BusinessException(
                DomainErrorCode.BOOTCAMP_MIN_CAPACITIES,
                "El bootcamp debe tener al menos " + MIN_CAPACITIES + " capacidad(es)"
            );
        }
        if (capacities.size() > MAX_CAPACITIES) {
            throw new BusinessException(
                DomainErrorCode.BOOTCAMP_MAX_CAPACITIES,
                "El bootcamp no puede tener más de " + MAX_CAPACITIES + " capacidades"
            );
        }

        boolean hasInvalidIds = capacities.stream()
            .anyMatch(capacity -> capacity.getId() == null || capacity.getId() <= 0);

        if (hasInvalidIds) {
            throw new BusinessException(
                DomainErrorCode.BOOTCAMP_CAPACITIES_REQUIRED,
                "Todas las capacidades deben tener un ID válido"
            );
        }
    }
}
