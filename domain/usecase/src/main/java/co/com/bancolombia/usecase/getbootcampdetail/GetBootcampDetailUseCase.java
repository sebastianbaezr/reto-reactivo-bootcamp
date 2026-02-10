package co.com.bancolombia.usecase.getbootcampdetail;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.BootcampDetail;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityDetail;
import co.com.bancolombia.model.capacity.gateways.CapacityDetailGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.person.gateways.PersonGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class GetBootcampDetailUseCase {
    private final BootcampRepository bootcampRepository;
    private final PersonGateway personGateway;
    private final CapacityDetailGateway capacityDetailGateway;

    public Mono<BootcampDetail> execute() {
        return personGateway.getBootcampWithMostPeople()
            .flatMap(enrollment -> bootcampRepository.findById(enrollment.bootcampId())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_NOT_FOUND)))
            )
            .flatMap(this::buildBootcampDetail);
    }

    private Mono<BootcampDetail> buildBootcampDetail(Bootcamp bootcamp) {
        List<Long> capacityIds = bootcamp.getCapacities() != null
            ? bootcamp.getCapacities().stream()
                .map(Capacity::getId)
                .toList()
            : List.of();

        System.out.println("Capacity IDs for bootcamp " + bootcamp.getId() + ": " + capacityIds);

        Mono<List<CapacityDetail>> capacitiesMono = capacityIds.isEmpty()
            ? Mono.just(List.of())
            : capacityDetailGateway.getCapacitiesByIds(capacityIds).collectList();

        return Mono.zip(
            personGateway.getPersonsByBootcampId(bootcamp.getId()).collectList(),
            capacitiesMono
        ).map(tuple -> BootcampDetail.builder()
            .id(bootcamp.getId())
            .name(bootcamp.getName())
            .description(bootcamp.getDescription())
            .releaseDate(bootcamp.getReleaseDate())
            .duration(bootcamp.getDuration())
            .persons(tuple.getT1())
            .capacities(tuple.getT2())
            .build());
    }

}
