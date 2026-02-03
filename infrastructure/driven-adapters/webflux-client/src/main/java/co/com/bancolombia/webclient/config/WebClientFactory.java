package co.com.bancolombia.webclient.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebClientFactory {

    private final ExternalServicesProperties properties;
    private final Map<String, WebClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Get or create a WebClient for the specified service.
     * WebClients are cached after creation for reuse.
     */
    public WebClient getWebClient(String serviceName) {
        return clientCache.computeIfAbsent(serviceName, this::createWebClient);
    }

    private WebClient createWebClient(String serviceName) {
        log.info("Creating WebClient for service: {}", serviceName);

        ServiceProperties serviceProps = properties.getService(serviceName);

        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(serviceProps.responseTimeout()))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serviceProps.connectTimeout());

        WebClient webClient = WebClient.builder()
            .baseUrl(serviceProps.baseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

        log.debug("WebClient created for service: {} with baseUrl: {}",
            serviceName, serviceProps.baseUrl());

        return webClient;
    }

    /**
     * Clear the WebClient cache. Useful for testing.
     */
    public void clearCache() {
        log.debug("Clearing WebClient cache");
        clientCache.clear();
    }
}
