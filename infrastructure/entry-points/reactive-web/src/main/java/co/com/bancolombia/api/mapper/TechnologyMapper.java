package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.dto.response.TechnologyResponse;
import co.com.bancolombia.model.technology.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TechnologyMapper {
    @Mapping(target = "id", ignore = true)
    Technology toEntity(TechnologyRequest request);

    TechnologyResponse toResponse(Technology entity);
}
