package co.com.bancolombia.model.person.gateways;

import co.com.bancolombia.model.person.BootcampEnrollment;
import co.com.bancolombia.model.person.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonGateway {
    Flux<Person> getPersonsByBootcampId(Long bootcampId);

    Mono<BootcampEnrollment> getBootcampWithMostPeople();
}
