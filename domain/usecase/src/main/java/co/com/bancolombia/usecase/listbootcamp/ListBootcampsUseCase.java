package co.com.bancolombia.usecase.listbootcamp;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListBootcampsUseCase {
    private final BootcampRepository bootcampRepository;

    private static final int MAX_PAGE_SIZE = 50;
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_CAPACITY_COUNT = "capacityCount";

    public Mono<Page<Bootcamp>> execute(PageRequest pageRequest) {
        try {
            validatePageRequest(pageRequest);
        } catch (BusinessException e) {
            return Mono.error(e);
        }

        return bootcampRepository.findAllWithPagination(pageRequest);
    }

    private void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest.getPage() < 0) {
            throw new BusinessException(DomainErrorCode.INVALID_PAGE_NUMBER);
        }

        if (pageRequest.getSize() < 1 || pageRequest.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(DomainErrorCode.INVALID_PAGE_SIZE);
        }

        String sortBy = pageRequest.getSortBy();
        if (sortBy != null &&
            !sortBy.equals(SORT_BY_NAME) &&
            !sortBy.equals(SORT_BY_CAPACITY_COUNT)) {
            throw new BusinessException(DomainErrorCode.INVALID_SORT_FIELD);
        }
    }
}
