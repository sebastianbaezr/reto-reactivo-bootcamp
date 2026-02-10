package co.com.bancolombia.r2dbc.mapper;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.r2dbc.bootcamp.BootcampData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampDataMapper {

    @Mapping(target = "capacities", ignore = true)
    Bootcamp toDomain(BootcampData bootcampData);

    @Mapping(target = "capacityIds", ignore = true)
    BootcampData toData(Bootcamp bootcamp);
}
