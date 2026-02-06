package co.com.bancolombia.adapter.bootcampreport;

import co.com.bancolombia.adapter.bootcampreport.dto.BootcampReportRequest;
import co.com.bancolombia.adapter.bootcampreport.dto.BootcampReportResponse;
import co.com.bancolombia.model.bootcamp.gateways.BootcampReportGateway;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Repository
@Slf4j
public class BootcampReportAdapter implements BootcampReportGateway {

    private static final String SERVICE_NAME = "bootcamp-report-service";
    private final WebClient webClient;

    public BootcampReportAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
        log.info("✅ BootcampReportAdapter inicializado y disponible como bean de Spring");
    }

    @Override
    public void sendBootcampReportAsync(Long bootcampId, Long capacityCount, Long technologyCount) {
        BootcampReportRequest reportRequest = BootcampReportRequest.builder()
            .capacityCount(capacityCount)
            .technologyCount(technologyCount)
            .build();

        log.info("🚀 BOOTCAMP REPORT - Iniciando envío asincrónico");
        log.info("   📌 Bootcamp ID: {}", bootcampId);
        log.info("   📊 Capacity Count: {}", capacityCount);
        log.info("   🔧 Technology Count: {}", technologyCount);
        log.info("   🔄 Despachando a thread: boundedElastic");

        sendBootcampReportAsyncInternal(bootcampId, reportRequest);
    }

    private void sendBootcampReportAsyncInternal(Long bootcampId, BootcampReportRequest reportRequest) {
        sendBootcampReportMono(bootcampId, reportRequest)
            .subscribeOn(Schedulers.boundedElastic())
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(5))
                .doBeforeRetry(signal -> {
                    int retryAttempt = (int) (signal.totalRetries() + 1);
                    long delaySeconds = Math.min(1L * (long) Math.pow(2, retryAttempt - 1), 5);
                    log.warn("⚠️  REINTENTO #{} para bootcamp id: {} (esperando {} segundos)",
                        retryAttempt, bootcampId, delaySeconds);
                }))
            .subscribe(
                response -> {
                    log.info("✅ BOOTCAMP REPORT ENVIADO EXITOSAMENTE");
                    log.info("   ✓ Bootcamp ID: {}", bootcampId);
                    log.info("   ✓ Response Timestamp: {}", response.getUpdatedAt());
                    log.info("   ✓ Registered People: {}", response.getRegisteredPeople());
                },
                error -> {
                    log.error("❌ BOOTCAMP REPORT FALLÓ después de reintentos");
                    log.error("   ✗ Bootcamp ID: {}", bootcampId);
                    log.error("   ✗ Error Message: {}", error.getMessage());
                    log.error("   ✗ Error Type: {}", error.getClass().getSimpleName());
                },
                () -> log.debug("📭 Bootcamp report stream completado para id: {}", bootcampId)
            );
    }

    public Mono<BootcampReportResponse> sendBootcampReport(Long bootcampId, BootcampReportRequest reportRequest) {
        return sendBootcampReportMono(bootcampId, reportRequest)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(5)));
    }

    private Mono<BootcampReportResponse> sendBootcampReportMono(Long bootcampId, BootcampReportRequest reportRequest) {
        log.info("📤 Construyendo request PATCH para bootcamp id: {}", bootcampId);
        log.debug("   Request body: capacity_count={}, technology_count={}",
            reportRequest.getCapacityCount(), reportRequest.getTechnologyCount());

        return webClient.patch()
            .uri("/api/bootcamp-report/{bootcampId}", bootcampId)
            .bodyValue(reportRequest)
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                response -> response.bodyToMono(String.class)
                    .doOnNext(body -> log.error("   ❌ Response body: {}", body))
                    .flatMap(body -> Mono.error(new RuntimeException(
                        "Bootcamp report service returned error: " + response.statusCode() + " - " + body))))
            .bodyToMono(BootcampReportResponse.class)
            .doFirst(() -> log.info("🌐 Enviando PATCH a: http://localhost:8084/api/bootcamp-report/{}", bootcampId))
            .doOnSuccess(response -> {
                log.info("📨 RESPUESTA RECIBIDA del servicio de reporte para bootcamp id: {}", bootcampId);
                log.info("   ✓ Bootcamp ID: {}", response.getBootcampId());
                log.info("   ✓ Capacity Count: {}", response.getCapacityCount());
                log.info("   ✓ Technology Count: {}", response.getTechnologyCount());
                log.info("   ✓ Registered People: {}", response.getRegisteredPeople());
                log.info("   ✓ Created At: {}", response.getCreatedAt());
                log.info("   ✓ Updated At: {}", response.getUpdatedAt());
            })
            .doOnError(error -> {
                log.error("❌ ERROR en la comunicación con el servicio de reporte");
                log.error("   Bootcamp ID: {}", bootcampId);
                log.error("   Error: {}", error.getMessage());
            });
    }
}
