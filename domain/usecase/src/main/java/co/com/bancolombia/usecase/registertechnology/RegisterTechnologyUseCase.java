package co.com.bancolombia.usecase.registertechnology;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import co.com.bancolombia.usecase.validator.TechnologyValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterTechnologyUseCase {
    private final TechnologyRepository technologyRepository;

    public Mono<Technology> execute(Technology technology) {
        try {
            TechnologyValidator.validateName(technology.getName());
            TechnologyValidator.validateDescription(technology.getDescription());
        } catch (BusinessException e) {
            return Mono.error(e);
        }

        return technologyRepository.existsByName(technology.getName())
            .flatMap(exists -> {
                if (Boolean.TRUE.equals(exists)) {
                    return Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_NAME_ALREADY_EXISTS));
                }
                return technologyRepository.save(technology);
            });
    }

    private Mono<Void> validateTechnology(Technology technology) {
        return Mono.fromRunnable(() -> {
            TechnologyValidator.validateName(technology.getName());
            TechnologyValidator.validateDescription(technology.getDescription());
        });
    }

    private Mono<Void> checkDuplicate(String name) {
        return technologyRepository.existsByName(name)
            .flatMap(exists ->
                exists
                    ? Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_NAME_ALREADY_EXISTS))
                    : Mono.empty()
            );
    }
}
