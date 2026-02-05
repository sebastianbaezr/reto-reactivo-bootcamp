package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.dto.response.RestoreTechnologiesResponse;
import co.com.bancolombia.api.dto.response.SoftDeleteTechnologiesResponse;
import co.com.bancolombia.api.dto.response.TechnologyResponse;
import co.com.bancolombia.api.dto.response.TechnologySimpleResponse;
import co.com.bancolombia.api.dto.response.ValidateTechnologiesResponse;
import co.com.bancolombia.model.result.RestoreTechnologiesResult;
import co.com.bancolombia.model.result.SoftDeleteTechnologiesResult;
import co.com.bancolombia.model.result.ValidateTechnologiesResult;
import co.com.bancolombia.model.technology.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TechnologyMapper {
    @Mapping(target = "id", ignore = true)
    Technology toDomain(TechnologyRequest request);

    TechnologyResponse toResponse(Technology entity);

    TechnologySimpleResponse toSimpleResponse(Technology entity);

    ValidateTechnologiesResponse toValidateResponse(ValidateTechnologiesResult result);

    @Mapping(target = "deletedIds", source = "requestedIds")
    @Mapping(target = "message", constant = "Tecnologías eliminadas exitosamente")
    SoftDeleteTechnologiesResponse toSoftDeleteResponse(SoftDeleteTechnologiesResult result);

    @Mapping(target = "restoredIds", source = "requestedIds")
    @Mapping(target = "message", constant = "Tecnologías restauradas exitosamente")
    RestoreTechnologiesResponse toRestoreResponse(RestoreTechnologiesResult result);
}
