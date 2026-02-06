package co.com.bancolombia.usecase.registerbootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.bootcamp.gateways.BootcampReportGateway;
import co.com.bancolombia.model.capacity.gateways.CapacityValidationGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.usecase.validator.BootcampValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RegisterBootcampUseCase {
    private final BootcampRepository bootcampRepository;
    private final CapacityValidationGateway capacityValidationGateway;
    private final Optional<BootcampReportGateway> bootcampReportGateway;

    public Mono<Bootcamp> execute(Bootcamp bootcamp) {
        try {
            BootcampValidator.validateName(bootcamp.getName());
            BootcampValidator.validateDescription(bootcamp.getDescription());
            BootcampValidator.validateReleaseDate(bootcamp.getReleaseDate());
            BootcampValidator.validateDuration(bootcamp.getDuration());
            BootcampValidator.validateCapacities(bootcamp.getCapacities());
        } catch (BusinessException e) {
            return Mono.error(e);
        }

        return bootcampRepository.existsByName(bootcamp.getName())
            .flatMap(exists -> existsOrThrow(exists)
                .then(validateAndReturnCapacities(bootcamp)))
            .flatMap(capacityValidationGateway::validateCapacities)
            .flatMap(isValid -> isValidOrThrow(isValid)
                .then(bootcampRepository.save(bootcamp)))
            .doOnSuccess(this::sendBootcampReportAsync);
    }

    private Mono<Void> existsOrThrow(Boolean exists) {
        return Boolean.TRUE.equals(exists)
            ? Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_NAME_ALREADY_EXISTS))
            : Mono.empty();
    }

    private Mono<List<Long>> validateAndReturnCapacities(Bootcamp bootcamp) {
        return Mono.just(bootcamp.getCapacities().stream()
            .map(capacity -> capacity.getId())
            .toList());
    }

    private Mono<Void> isValidOrThrow(Boolean isValid) {
        return Boolean.FALSE.equals(isValid)
            ? Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_INVALID_CAPACITIES))
            : Mono.empty();
    }

    private void sendBootcampReportAsync(Bootcamp bootcamp) {
        if (bootcampReportGateway.isEmpty()) {
            return;
        }

        List<Long> capacityIds = bootcamp.getCapacities().stream()
            .map(capacity -> capacity.getId())
            .toList();
        long capacityCount = (long) capacityIds.size();

        capacityValidationGateway.getTechnologyCountsByCapacityIds(capacityIds)
            .subscribe(
                technologyCountsMap -> {
                    long totalTechnologyCount = technologyCountsMap.values().stream()
                        .mapToLong(Long::longValue)
                        .sum();
                    bootcampReportGateway.get().sendBootcampReportAsync(bootcamp.getId(), capacityCount, totalTechnologyCount);
                },
                error -> System.err.println("Error obtaining technology counts: " + error.getMessage())
            );
    }
}
