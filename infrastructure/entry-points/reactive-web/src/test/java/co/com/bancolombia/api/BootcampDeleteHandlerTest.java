package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.response.BootcampDeleteResponse;
import co.com.bancolombia.model.bootcamp.DeleteBootcampSaga;
import co.com.bancolombia.usecase.deletebootcamp.DeleteBootcampUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;

/**
 * Tests for BootcampHandler.deleteBootcamp method.
 */
@ExtendWith(MockitoExtension.class)
class BootcampDeleteHandlerTest {

    @Mock
    private DeleteBootcampUseCase deleteBootcampUseCase;

    private BootcampHandler handler;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        handler = new BootcampHandler(
                null,
                null,
                deleteBootcampUseCase,
                null,
                null
        );

        webTestClient = WebTestClient.bindToRouterFunction(
                RouterFunctions.route(
                        DELETE("/api/bootcamps/{bootcampId}"),
                        handler::deleteBootcamp
                )
        ).build();
    }

    @Test
    @DisplayName("Should delete bootcamp successfully and return 200")
    void testDeleteBootcampSuccess() {
        Long bootcampId = 1L;
        DeleteBootcampSaga saga = DeleteBootcampSaga.builder()
                .bootcampId(bootcampId)
                .bootcampName("Java Bootcamp")
                .status(DeleteBootcampSaga.SagaStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .capacityIds(List.of(5L))
                .technologiesIds(List.of(10L))
                .bootcampDeleteResult(DeleteBootcampSaga.StepResult.builder().success(true).build())
                .build();

        when(deleteBootcampUseCase.execute(bootcampId)).thenReturn(Mono.just(saga));

        webTestClient.delete()
                .uri("/api/bootcamps/{bootcampId}", bootcampId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BootcampDeleteResponse.class)
                .hasSize(1)
                .consumeWith(result -> {
                    BootcampDeleteResponse response = result.getResponseBody().get(0);
                    assertThat(response.getBootcampId()).isEqualTo(bootcampId);
                    assertThat(response.getBootcampName()).isEqualTo("Java Bootcamp");
                    assertThat(response.getStatus()).isEqualTo("COMPLETED");
                });
    }

    @Test
    @DisplayName("Should calculate duration in milliseconds")
    void testDurationCalculation() {
        Long bootcampId = 1L;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(1);

        DeleteBootcampSaga saga = DeleteBootcampSaga.builder()
                .bootcampId(bootcampId)
                .bootcampName("Java Bootcamp")
                .status(DeleteBootcampSaga.SagaStatus.COMPLETED)
                .startTime(startTime)
                .endTime(endTime)
                .capacityIds(List.of())
                .technologiesIds(List.of())
                .bootcampDeleteResult(DeleteBootcampSaga.StepResult.builder().success(true).build())
                .build();

        when(deleteBootcampUseCase.execute(bootcampId)).thenReturn(Mono.just(saga));

        webTestClient.delete()
                .uri("/api/bootcamps/{bootcampId}", bootcampId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BootcampDeleteResponse.class)
                .consumeWith(result -> {
                    BootcampDeleteResponse response = result.getResponseBody().get(0);
                    assertThat(response.getDurationMs()).isGreaterThan(0);
                });
    }
}
