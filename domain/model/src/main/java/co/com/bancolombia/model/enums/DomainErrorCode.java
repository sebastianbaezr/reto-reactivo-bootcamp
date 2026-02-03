package co.com.bancolombia.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {
    // Technology errors
    TECHNOLOGY_NAME_ALREADY_EXISTS("TECHNOLOGY_NAME_ALREADY_EXISTS", "Ya existe una tecnología con este nombre"),
    INVALID_NAME_LENGTH("INVALID_NAME_LENGTH", "El nombre de la tecnología no debe exceder 50 caracteres"),
    INVALID_DESCRIPTION_LENGTH("INVALID_DESCRIPTION_LENGTH", "La descripción de la tecnología no debe exceder 90 caracteres"),
    DESCRIPTION_REQUIRED("DESCRIPTION_REQUIRED", "La descripción es obligatoria"),
    NAME_REQUIRED("NAME_REQUIRED", "El nombre es obligatorio"),

    // Bootcamp errors
    BOOTCAMP_NAME_ALREADY_EXISTS("BOOTCAMP_NAME_ALREADY_EXISTS", "Ya existe un bootcamp con este nombre"),
    BOOTCAMP_INVALID_NAME_LENGTH("BOOTCAMP_INVALID_NAME_LENGTH", "El nombre del bootcamp no debe exceder 100 caracteres"),
    BOOTCAMP_INVALID_DESCRIPTION_LENGTH("BOOTCAMP_INVALID_DESCRIPTION_LENGTH", "La descripción del bootcamp no debe exceder 200 caracteres"),
    BOOTCAMP_NAME_REQUIRED("BOOTCAMP_NAME_REQUIRED", "El nombre del bootcamp es obligatorio"),
    BOOTCAMP_DESCRIPTION_REQUIRED("BOOTCAMP_DESCRIPTION_REQUIRED", "La descripción del bootcamp es obligatoria"),
    BOOTCAMP_RELEASE_DATE_REQUIRED("BOOTCAMP_RELEASE_DATE_REQUIRED", "La fecha de lanzamiento es obligatoria"),
    BOOTCAMP_RELEASE_DATE_PAST("BOOTCAMP_RELEASE_DATE_PAST", "La fecha de lanzamiento no puede ser en el pasado"),
    BOOTCAMP_DURATION_REQUIRED("BOOTCAMP_DURATION_REQUIRED", "La duración es obligatoria"),
    BOOTCAMP_DURATION_INVALID("BOOTCAMP_DURATION_INVALID", "La duración debe ser mayor a 0"),
    BOOTCAMP_CAPACITIES_REQUIRED("BOOTCAMP_CAPACITIES_REQUIRED", "Debe especificar al menos una capacidad"),
    BOOTCAMP_MIN_CAPACITIES("BOOTCAMP_MIN_CAPACITIES", "El bootcamp debe tener al menos 1 capacidad"),
    BOOTCAMP_MAX_CAPACITIES("BOOTCAMP_MAX_CAPACITIES", "El bootcamp no puede tener más de 20 capacidades"),
    BOOTCAMP_INVALID_CAPACITIES("BOOTCAMP_INVALID_CAPACITIES", "Una o más capacidades no existen en el sistema"),

    // Capacity service errors
    CAPACITY_SERVICE_UNAVAILABLE("CAPACITY_SERVICE_UNAVAILABLE", "El servicio de capacidades no está disponible"),
    CAPACITY_SERVICE_ERROR("CAPACITY_SERVICE_ERROR", "Error al comunicarse con el servicio de capacidades"),

    // Pagination errors
    INVALID_PAGE_NUMBER("INVALID_PAGE_NUMBER", "El número de página no puede ser negativo"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "El tamaño de página debe estar entre 1 y 50"),
    INVALID_SORT_FIELD("INVALID_SORT_FIELD", "El campo de ordenamiento no es válido");

    private final String code;
    private final String message;
}
