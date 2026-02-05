package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.BootcampRequest;
import co.com.bancolombia.api.dto.request.ListBootcampsRequest;
import co.com.bancolombia.api.dto.response.BootcampDeleteResponse;
import co.com.bancolombia.api.helper.BootcampHandlerHelper;
import co.com.bancolombia.api.mapper.BootcampMapper;
import co.com.bancolombia.api.mapper.BootcampListMapper;
import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.DeleteBootcampSaga;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.usecase.registerbootcamp.RegisterBootcampUseCase;
import co.com.bancolombia.usecase.listbootcamp.ListBootcampsUseCase;
import co.com.bancolombia.usecase.deletebootcamp.DeleteBootcampUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampHandler {

    private static final String BOOTCAMP_ID_PATH_VARIABLE = "bootcampId";

    private final RegisterBootcampUseCase registerBootcampUseCase;
    private final ListBootcampsUseCase listBootcampsUseCase;
    private final DeleteBootcampUseCase deleteBootcampUseCase;
    private final BootcampMapper bootcampMapper;
    private final BootcampListMapper bootcampListMapper;

    public Mono<ServerResponse> registerBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampRequest.class)
            .map(this::buildBootcamp)
            .flatMap(registerBootcampUseCase::execute)
            .map(bootcampMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamp registered successfully"))
            .doOnError(error -> log.error("Error registering bootcamp", error));
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        return extractListParams(request)
            .flatMap(listRequest -> listBootcampsUseCase.execute(bootcampListMapper.toPageRequest(listRequest)))
            .map(bootcampListMapper::toPageResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamps listed successfully"))
            .doOnError(error -> log.error("Error listing bootcamps", error));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        return Mono.fromCallable(() -> Long.parseLong(request.pathVariable(BOOTCAMP_ID_PATH_VARIABLE)))
            .flatMap(bootcampId -> deleteBootcampUseCase.execute(bootcampId)
                .map(this::buildDeleteResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(v -> log.info("Bootcamp {} deleted successfully", bootcampId))
                .doOnError(error -> log.error("Error deleting bootcamp", error)));
    }

    private BootcampDeleteResponse buildDeleteResponse(DeleteBootcampSaga saga) {
        return BootcampDeleteResponse.builder()
            .bootcampId(saga.getBootcampId())
            .bootcampName(saga.getBootcampName())
            .status(saga.getStatus().toString())
            .startTime(saga.getStartTime())
            .endTime(saga.getEndTime())
            .durationMs(BootcampHandlerHelper.calculateDurationMs(saga.getStartTime(), saga.getEndTime()))
            .bootcampDeleted(BootcampHandlerHelper.isSuccessfulSaga(saga))
            .capacitiesDeleted(BootcampHandlerHelper.countDeletedCapacities(saga))
            .technologiesDeleted(BootcampHandlerHelper.countDeletedTechnologies(saga))
            .build();
    }

    private Mono<ListBootcampsRequest> extractListParams(ServerRequest request) {
        return Mono.fromCallable(() -> ListBootcampsRequest.builder()
            .page(request.queryParam("page").map(Integer::parseInt).orElse(0))
            .size(request.queryParam("size").map(Integer::parseInt).orElse(10))
            .sortBy(request.queryParam("sortBy").orElse("name"))
            .sortOrder(request.queryParam("sortOrder").orElse("asc"))
            .build());
    }

    private Bootcamp buildBootcamp(BootcampRequest request) {
        Bootcamp bootcamp = bootcampMapper.toEntity(request);
        bootcamp.setCapacities(BootcampHandlerHelper.buildCapacitiesFromRequest(request));
        return bootcamp;
    }
}
