package co.com.bancolombia.model.person.gateways;

import co.com.bancolombia.model.person.Person;
import reactor.core.publisher.Flux;

public interface PersonGateway {
    Flux<Person> getPersonsByBootcampId(Long bootcampId);
}
