# Prompt: Implementar WebClient Genérico Reutilizable

Copia este prompt y úsalo en cualquier microservicio con la misma arquitectura (Clean Architecture + Spring WebFlux + Gradle).

---

## CONTEXTO

Necesito implementar un cliente HTTP genérico y reutilizable que permita conectar este microservicio con múltiples otros microservicios (APIs externas) de forma escalable y mantenible.

**Problema actual:**
Si crecemos necesitaremos conectar con N servicios. Crear un módulo específico para cada uno no es escalable.

**Solución:**
Crear un módulo genérico `webflux-client` que cualquier adaptador pueda usar, permitiendo configurar múltiples servicios en `application.yaml`.

## REQUISITOS FUNCIONALES

### 1. Configuración Multi-Servicio
- Una sola sección `external-services` en `application.yaml` para definir todos los servicios externos
- Cada servicio tiene: `base-url`, `connect-timeout` (ms), `response-timeout` (segundos)
- Ejemplo:
  ```yaml
  external-services:
    services:
      payment:
        base-url: "http://payment-service:8080"
        connect-timeout: 3000
        response-timeout: 5
      user-management:
        base-url: "http://user-service:8081"
        connect-timeout: 5000
        response-timeout: 10
  ```

### 2. Módulo Genérico Reutilizable
- Crear módulo `infrastructure/driven-adapters/webflux-client/`
- **SIN dependencias** de dominios específicos (no debe importar clases del model)
- WebClientFactory que crea clientes bajo demanda
- Cachea clientes para no recrearlos

### 3. Inyección Dinámica
- Adapters específicos inyectan `WebClientFactory`
- En constructor, piden el WebClient por nombre de servicio: `factory.getWebClient("payment")`
- Factory lanza excepción si el servicio no está configurado

### 4. Implementación de Gateways
- Mantener gateways específicos en el `model` module
- Crear adapters específicos que implementen los gateways
- Cada adapter usa el `WebClientFactory` genérico

## ARQUITECTURA

```
infrastructure/driven-adapters/
├── webflux-client/                 # Módulo genérico (NUEVO)
│   ├── build.gradle
│   └── src/main/java/co/com/bancolombia/webclient/
│       └── config/
│           ├── ExternalServicesProperties.java
│           ├── ServiceProperties.java
│           ├── WebClientFactory.java
│           └── WebClientAutoConfiguration.java
│
├── payment-client-adapter/         # Ejemplo: adapter específico (NUEVO)
│   ├── build.gradle
│   └── src/main/java/co/com/bancolombia/adapter/payment/
│       └── PaymentClientAdapter.java  (usa WebClientFactory)
│
└── other-adapters/                 # Otros adapters...
```

## INSTRUCCIONES DE IMPLEMENTACIÓN

### Paso 1: Crear Módulo Genérico

#### 1.1 Crear `infrastructure/driven-adapters/webflux-client/build.gradle`

```gradle
dependencies {
    // IMPORTANTE: Sin dependencias de 'model' - este módulo es genérico
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-configuration-processor'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

#### 1.2 Crear `ServiceProperties.java`

```java
package co.com.bancolombia.webclient.config;

public record ServiceProperties(
    String baseUrl,
    Integer connectTimeout,      // milliseconds
    Integer responseTimeout      // seconds
) {}
```

#### 1.3 Crear `ExternalServicesProperties.java`

```java
package co.com.bancolombia.webclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "external-services")
public record ExternalServicesProperties(
    Map<String, ServiceProperties> services
) {
    public ExternalServicesProperties {
        if (services == null) {
            services = Map.of();
        }
    }

    public ServiceProperties getService(String serviceName) {
        ServiceProperties props = services.get(serviceName);
        if (props == null) {
            throw new IllegalArgumentException(
                "Service configuration not found: " + serviceName);
        }
        return props;
    }
}
```

#### 1.4 Crear `WebClientFactory.java` (Corazón del módulo)

```java
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
```

#### 1.5 Crear `WebClientAutoConfiguration.java`

```java
package co.com.bancolombia.webclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnClass(WebClient.class)
@EnableConfigurationProperties(ExternalServicesProperties.class)
public class WebClientAutoConfiguration {

    @Bean
    public WebClientFactory webClientFactory(ExternalServicesProperties properties) {
        return new WebClientFactory(properties);
    }
}
```

### Paso 2: Crear Adapter Específico

#### 2.1 Crear `infrastructure/driven-adapters/[service]-client-adapter/build.gradle`

```gradle
dependencies {
    implementation project(':model')              // Domain gateways
    implementation project(':webflux-client')     // Generic HTTP client

    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
}
```

#### 2.2 Crear Adapter Específico (Ejemplo: PaymentClientAdapter)

```java
package co.com.bancolombia.adapter.payment;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.payment.gateways.PaymentGateway;  // Tu gateway específico
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@Slf4j
public class PaymentClientAdapter implements PaymentGateway {

    private static final String SERVICE_NAME = "payment";  // Nombre de la config en YAML
    private static final String PROCESS_PATH = "/api/payments/process";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    // Inyecta el factory genérico
    public PaymentClientAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
    }

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        log.info("Processing payment: {}", request.getId());

        return webClient
            .post()
            .uri(PROCESS_PATH)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
            .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
            .bodyToMono(PaymentResponse.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnSuccess(response -> log.info("Payment processed: {}", response.getId()))
            .doOnError(error -> log.error("Payment processing error: {}", error.getMessage()))
            .onErrorResume(this::handleRequestError);
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        return Mono.error(new BusinessException(DomainErrorCode.PAYMENT_SERVICE_ERROR));
    }

    private Mono<Throwable> handleServerError(ClientResponse response) {
        return Mono.error(new BusinessException(DomainErrorCode.PAYMENT_SERVICE_UNAVAILABLE));
    }

    private Mono<PaymentResponse> handleRequestError(Throwable error) {
        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(DomainErrorCode.PAYMENT_SERVICE_UNAVAILABLE));
        }
        return Mono.error(error);
    }
}
```

### Paso 3: Actualizar Configuración

#### 3.1 Actualizar `settings.gradle`

```gradle
// Agregar módulos
include ':webflux-client'
project(':webflux-client').projectDir = file('./infrastructure/driven-adapters/webflux-client')

include ':payment-client-adapter'
project(':payment-client-adapter').projectDir = file('./infrastructure/driven-adapters/payment-client-adapter')

// Repetir para otros adapters...
```

#### 3.2 Actualizar `app-service/build.gradle`

```gradle
dependencies {
    // ... otras dependencias
    implementation project(':webflux-client')
    implementation project(':payment-client-adapter')
    // Agregar otros adapters...
}
```

#### 3.3 Actualizar `application.yaml`

```yaml
external-services:
  services:
    payment:
      base-url: "http://payment-service:8080"
      connect-timeout: 3000
      response-timeout: 5

    user-management:
      base-url: "http://user-service:8081"
      connect-timeout: 5000
      response-timeout: 10

    # Agregar más servicios fácilmente...
```

## PATRONES Y CONVENCIONES

### Nombre de Servicios en YAML
- Use camelCase o kebab-case (pero sea consistente)
- Ejemplo: `payment`, `user-management`, `notification-service`

### Nombres de Adapters
- Patrón: `{ServiceName}ClientAdapter`
- Ejemplo: `PaymentClientAdapter`, `UserManagementClientAdapter`

### Paths de Adapters
- `infrastructure/driven-adapters/{service}-client-adapter/`
- Paquetes: `co.com.bancolombia.adapter.{service}`

### Timeouts
- `connectTimeout`: en **milisegundos** (ms)
- `responseTimeout`: en **segundos** (s)
- Defaults sugeridos:
  - APIs rápidas: 3s response timeout
  - APIs lentas: 10s response timeout

## TESTING

### Test del Factory

```java
@SpringBootTest
class WebClientFactoryTest {

    @Autowired
    private WebClientFactory factory;

    @Test
    void shouldCreateWebClientForConfiguredService() {
        WebClient client = factory.getWebClient("payment");
        assertNotNull(client);
    }

    @Test
    void shouldThrowExceptionForUnknownService() {
        assertThrows(IllegalArgumentException.class,
            () -> factory.getWebClient("unknown-service"));
    }
}
```

### Test del Adapter

```java
class PaymentClientAdapterTest {

    private MockWebServer mockWebServer;
    private PaymentClientAdapter adapter;
    private WebClientFactory factory;

    @Before
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        factory = createMockFactory(mockWebServer.url("/").toString());
        adapter = new PaymentClientAdapter(factory);
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"id\": \"123\", \"status\": \"SUCCESS\"}"));

        PaymentRequest request = new PaymentRequest("123", 100.0);

        StepVerifier.create(adapter.processPayment(request))
            .assertNext(response -> assertEquals("SUCCESS", response.getStatus()))
            .verifyComplete();
    }
}
```

## CHECKLIST DE IMPLEMENTACIÓN

- [ ] Crear módulo `webflux-client` con 4 clases
- [ ] Crear adapter específico que implemente tu gateway
- [ ] Actualizar `settings.gradle` con nuevos módulos
- [ ] Actualizar `app-service/build.gradle` con dependencias
- [ ] Actualizar `application.yaml` con `external-services`
- [ ] Crear tests para factory y adapter
- [ ] Ejecutar `./gradlew clean build`
- [ ] Verificar que no hay referencias al adaptador antiguo

## VENTAJAS DE ESTA ARQUITECTURA

✅ **Escalable**: Agregar nuevo servicio = agregar config YAML + crear adapter
✅ **Reutilizable**: El módulo `webflux-client` se puede copiar a otros proyectos
✅ **Mantenible**: Cambios de HTTP se hacen en un lugar
✅ **Clean**: Mantiene separación de capas y dependencias correctas
✅ **Testeable**: Cada componente aislado y mockeable

## ERRORES COMUNES A EVITAR

❌ No agregar `project(':model')` como dependencia de `webflux-client` (debe ser genérico)
❌ No usar el mismo nombre de servicio en YAML en múltiples microservicios
❌ No olvidar el `@Repository` en los adapters
❌ No cachear la response del servidor (factory cachea clientes, no respuestas)
❌ Usar valores de timeout inconsistentes entre servicios sin motivo

## EJEMPLO COMPLETO

Para ver un ejemplo real de esta arquitectura implementada, revisa:
- Módulo genérico: `infrastructure/driven-adapters/webflux-client/`
- Adapter específico: `infrastructure/driven-adapters/technology-client-adapter/`
- Configuración: `applications/app-service/src/main/resources/application.yaml`

---

**Fin del Prompt**

Copia todo lo anterior y úsalo como guía para implementar en tu microservicio.
