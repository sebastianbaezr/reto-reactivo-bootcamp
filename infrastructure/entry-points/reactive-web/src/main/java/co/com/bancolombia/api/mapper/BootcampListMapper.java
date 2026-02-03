package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.ListBootcampsRequest;
import co.com.bancolombia.api.dto.response.BootcampListItemResponse;
import co.com.bancolombia.api.dto.response.PageResponse;
import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import org.mapstruct.Mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BootcampListMapper {

    default PageRequest toPageRequest(ListBootcampsRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "name";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "asc";

        return PageRequest.builder()
            .page(request.getPage() != null ? request.getPage() : 0)
            .size(request.getSize() != null ? request.getSize() : 10)
            .sortBy(sortBy)
            .sortDirection("desc".equalsIgnoreCase(sortOrder)
                ? SortDirection.DESC
                : SortDirection.ASC)
            .build();
    }

    default PageResponse<BootcampListItemResponse> toPageResponse(Page<Bootcamp> page) {
        List<BootcampListItemResponse> content = page.getContent().stream()
            .map(this::toListItemResponse)
            .toList();

        return PageResponse.<BootcampListItemResponse>builder()
            .content(content)
            .pageMetadata(PageResponse.PageMetadata.builder()
                .currentPage(page.getCurrentPage())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getPageSize())
                .hasNextPage(page.isHasNextPage())
                .hasPreviousPage(page.isHasPreviousPage())
                .build())
            .sortMetadata(PageResponse.SortMetadata.builder()
                .field(page.getSortBy())
                .direction(page.getSortDirection().name().toLowerCase())
                .build())
            .build();
    }

    default BootcampListItemResponse toListItemResponse(Bootcamp bootcamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<BootcampListItemResponse.CapacityWithTechnologiesResponse> capacitiesResponse =
            bootcamp.getCapacities() != null
                ? bootcamp.getCapacities().stream()
                    .map(this::toCapacityWithTechnologiesResponse)
                    .toList()
                : List.of();

        return BootcampListItemResponse.builder()
            .id(bootcamp.getId())
            .name(bootcamp.getName())
            .description(bootcamp.getDescription())
            .releaseDate(bootcamp.getReleaseDate().format(formatter))
            .duration(bootcamp.getDuration())
            .capacityCount(capacitiesResponse.size())
            .capacities(capacitiesResponse)
            .createdAt(bootcamp.getCreatedAt())
            .updatedAt(bootcamp.getUpdatedAt())
            .build();
    }

    default BootcampListItemResponse.CapacityWithTechnologiesResponse toCapacityWithTechnologiesResponse(co.com.bancolombia.model.capacity.Capacity capacity) {
        List<BootcampListItemResponse.TechnologyResponse> technologies = capacity.getTechnologies() != null
            ? capacity.getTechnologies().stream()
                .map(tech -> BootcampListItemResponse.TechnologyResponse.builder()
                    .id(tech.getId())
                    .name(tech.getName())
                    .build())
                .toList()
            : List.of();

        return BootcampListItemResponse.CapacityWithTechnologiesResponse.builder()
            .id(capacity.getId())
            .name(capacity.getName())
            .technologies(technologies)
            .build();
    }
}
