package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.BootcampRequest;
import co.com.bancolombia.api.dto.response.BootcampResponse;
import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.capacity.Capacity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BootcampMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "capacities", ignore = true)
    Bootcamp toEntity(BootcampRequest request);

    @Mapping(target = "capacities", expression = "java(mapCapacitiesToIds(entity.getCapacities()))")
    BootcampResponse toResponse(Bootcamp entity);

    default List<Long> mapCapacitiesToIds(List<Capacity> capacities) {
        return capacities == null ? null : capacities.stream()
            .map(Capacity::getId)
            .toList();
    }
}
