package co.com.bancolombia.adapter.person;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonGateway;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@Slf4j
public class PersonAdapter implements PersonGateway {

    private static final String SERVICE_NAME = "person-service";
    private static final String GET_PERSONS_PATH = "/api/bootcamps/{bootcampId}/persons";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    public PersonAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
    }

    @Override
    public Flux<Person> getPersonsByBootcampId(Long bootcampId) {
        log.info("Fetching persons for bootcamp ID: {}", bootcampId);

        return webClient
            .get()
            .uri(GET_PERSONS_PATH, bootcampId)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                log.error("Error fetching persons. Status: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Error response body: {}", body))
                    .then(Mono.error(new BusinessException(
                        response.statusCode().is5xxServerError()
                            ? DomainErrorCode.PERSON_SERVICE_UNAVAILABLE
                            : DomainErrorCode.PERSON_SERVICE_ERROR
                    )));
            })
            .bodyToFlux(Person.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnNext(person -> log.debug("Fetched person: {} {}", person.name(), person.lastname()))
            .doOnError(error -> log.error("Error fetching persons: {}", error.getMessage(), error))
            .onErrorResume(error -> {
                log.error("Person fetch failed, returning empty", error);
                return Mono.error(new BusinessException(DomainErrorCode.PERSON_SERVICE_UNAVAILABLE));
            });
    }
}
