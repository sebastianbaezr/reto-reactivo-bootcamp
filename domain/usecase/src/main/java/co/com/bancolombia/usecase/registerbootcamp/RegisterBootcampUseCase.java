package co.com.bancolombia.usecase.registerbootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.bootcamp.gateways.BootcampReportGateway;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.gateways.CapacityValidationGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.usecase.validator.BootcampValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class RegisterBootcampUseCase {
    private final BootcampRepository bootcampRepository;
    private final CapacityValidationGateway capacityValidationGateway;
    private final BootcampReportGateway bootcampReportGateway;

    public Mono<Bootcamp> execute(Bootcamp bootcamp) {
        return validateBootcamp(bootcamp)
            .flatMap(this::validateBootcampNameUniqueness)
            .flatMap(this::validateBootcampCapacities)
            .flatMap(bootcampRepository::save)
            .doOnSuccess(this::triggerBootcampReportAsync);
    }

    private Mono<Bootcamp> validateBootcamp(Bootcamp bootcamp) {
        return Mono.fromCallable(() -> {
            List.of(
                    (Consumer<Bootcamp>) b -> BootcampValidator.validateName(b.getName()),
                    b -> BootcampValidator.validateDescription(b.getDescription()),
                    b -> BootcampValidator.validateReleaseDate(b.getReleaseDate()),
                    b -> BootcampValidator.validateDuration(b.getDuration()),
                    b -> BootcampValidator.validateCapacities(b.getCapacities())
                )
                .forEach(validator -> validator.accept(bootcamp));
            return bootcamp;
        });
    }

    private Mono<Bootcamp> validateBootcampNameUniqueness(Bootcamp bootcamp) {
        return bootcampRepository.existsByName(bootcamp.getName())
            .filter(exists -> !exists)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_NAME_ALREADY_EXISTS)))
            .map(v -> bootcamp);
    }

    private Mono<Bootcamp> validateBootcampCapacities(Bootcamp bootcamp) {
        return Mono.just(bootcamp.getCapacities().stream()
                .map(Capacity::getId)
                .toList())
            .flatMap(capacityValidationGateway::validateCapacities)
            .filter(isValid -> isValid)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMP_INVALID_CAPACITIES)))
            .map(v -> bootcamp);
    }

    private void triggerBootcampReportAsync(Bootcamp bootcamp) {
        Mono.just(extractCapacityIds(bootcamp))
            .flatMap(capacityIds -> capacityValidationGateway.getTechnologyCountsByCapacityIds(capacityIds)
                .doOnNext(technologyCountsMap -> sendBootcampReport(bootcamp.getId(), capacityIds.size(), technologyCountsMap)))
            .onErrorResume(error -> { handleReportError(error); return Mono.empty(); })
            .subscribe();
    }

    private List<Long> extractCapacityIds(Bootcamp bootcamp) {
        return bootcamp.getCapacities().stream()
            .map(Capacity::getId)
            .toList();
    }

    private void sendBootcampReport(Long bootcampId, long capacityCount, Map<String, Long> technologyCountsMap) {
        long totalTechnologyCount = calculateTotalTechnologyCount(technologyCountsMap);
        bootcampReportGateway.sendBootcampReportAsync(bootcampId, capacityCount, totalTechnologyCount);
    }

    private long calculateTotalTechnologyCount(Map<String, Long> technologyCountsMap) {
        return technologyCountsMap.values().stream()
            .mapToLong(Long::longValue)
            .sum();
    }

    private void handleReportError(Throwable error) {
        System.err.println("Error obtaining technology counts: " + error.getMessage());
    }
}
