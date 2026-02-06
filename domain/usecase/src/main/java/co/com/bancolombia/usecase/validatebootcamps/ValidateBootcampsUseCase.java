package co.com.bancolombia.usecase.validatebootcamps;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ValidateBootcampsUseCase {
    private final BootcampRepository bootcampRepository;

    public Mono<ValidateBootcampsResult> execute(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Mono.just(ValidateBootcampsResult.builder()
                    .existingIds(List.of())
                    .notFoundIds(List.of())
                    .allExist(true)
                    .hasDateConflicts(false)
                    .build());
        }

        return bootcampRepository.findByIds(ids)
                .collectList()
                .map(bootcamps -> buildResult(ids, bootcamps));
    }

    private ValidateBootcampsResult buildResult(List<Long> requestedIds, List<Bootcamp> foundBootcamps) {
        Set<Long> existingIds = foundBootcamps.stream()
                .map(Bootcamp::getId)
                .collect(Collectors.toSet());

        List<Long> notFoundIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());

        boolean hasDateConflicts = detectDateConflicts(foundBootcamps);

        return ValidateBootcampsResult.builder()
                .existingIds(foundBootcamps.stream().map(Bootcamp::getId).collect(Collectors.toList()))
                .notFoundIds(notFoundIds)
                .allExist(notFoundIds.isEmpty())
                .hasDateConflicts(hasDateConflicts)
                .build();
    }

    private boolean detectDateConflicts(List<Bootcamp> bootcamps) {
        for (int i = 0; i < bootcamps.size(); i++) {
            for (int j = i + 1; j < bootcamps.size(); j++) {
                if (hasConflict(bootcamps.get(i), bootcamps.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasConflict(Bootcamp bootcamp1, Bootcamp bootcamp2) {
        LocalDate start1 = bootcamp1.getReleaseDate();
        LocalDate end1 = bootcamp1.getReleaseDate().plusDays(bootcamp1.getDuration());
        LocalDate start2 = bootcamp2.getReleaseDate();
        LocalDate end2 = bootcamp2.getReleaseDate().plusDays(bootcamp2.getDuration());

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
