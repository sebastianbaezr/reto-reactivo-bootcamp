package co.com.bancolombia.usecase.getbootcampdetail;

import co.com.bancolombia.model.bootcamp.Bootcamp;
import co.com.bancolombia.model.bootcamp.BootcampDetail;
import co.com.bancolombia.model.bootcamp.gateways.BootcampRepository;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityDetail;
import co.com.bancolombia.model.capacity.gateways.CapacityDetailGateway;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetBootcampDetailUseCase.
 * Tests bootcamp detail retrieval with capacity and person information.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetBootcampDetailUseCaseTest {

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private PersonGateway personGateway;

    @Mock
    private CapacityDetailGateway capacityDetailGateway;

    private GetBootcampDetailUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetBootcampDetailUseCase(bootcampRepository, personGateway, capacityDetailGateway);
    }

    @Test
    @DisplayName("Should retrieve bootcamp detail with capacities and persons")
    void testGetBootcampDetailWithCapacitiesAndPersons() {
        Long bootcampId = 1L;

        Capacity capacity1 = createCapacity(10L, "Frontend");
        Capacity capacity2 = createCapacity(11L, "Backend");

        Bootcamp bootcamp = createBootcamp(bootcampId, "Java Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of(capacity1, capacity2));

        CapacityDetail capacityDetail1 = createCapacityDetail(10L, "Frontend", 5);
        CapacityDetail capacityDetail2 = createCapacityDetail(11L, "Backend", 3);

        Person person1 = createPerson(100L, "John Doe");
        Person person2 = createPerson(101L, "Jane Smith");

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId))
                .thenReturn(Flux.just(person1, person2));
        when(capacityDetailGateway.getCapacitiesByIds(List.of(10L, 11L)))
                .thenReturn(Flux.just(capacityDetail1, capacityDetail2));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getId().equals(bootcampId) &&
                        result.getName().equals("Java Bootcamp") &&
                        result.getPersons().size() == 2 &&
                        result.getCapacities().size() == 2
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail when bootcamp not found")
    void testBootcampNotFound() {
        Long bootcampId = 999L;

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Should retrieve bootcamp detail with no capacities")
    void testGetBootcampDetailWithNoCapacities() {
        Long bootcampId = 1L;

        Bootcamp bootcamp = createBootcamp(bootcampId, "Java Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of());

        Person person1 = createPerson(100L, "John Doe");

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId))
                .thenReturn(Flux.just(person1));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getId().equals(bootcampId) &&
                        result.getCapacities().isEmpty() &&
                        result.getPersons().size() == 1
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve bootcamp detail with no persons")
    void testGetBootcampDetailWithNoPersons() {
        Long bootcampId = 1L;

        Capacity capacity1 = createCapacity(10L, "Frontend");
        Bootcamp bootcamp = createBootcamp(bootcampId, "Java Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of(capacity1));

        CapacityDetail capacityDetail1 = createCapacityDetail(10L, "Frontend", 5);

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId)).thenReturn(Flux.empty());
        when(capacityDetailGateway.getCapacitiesByIds(List.of(10L)))
                .thenReturn(Flux.just(capacityDetail1));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getId().equals(bootcampId) &&
                        result.getPersons().isEmpty() &&
                        result.getCapacities().size() == 1
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve bootcamp detail with multiple capacities")
    void testGetBootcampDetailWithMultipleCapacities() {
        Long bootcampId = 1L;

        Capacity capacity1 = createCapacity(10L, "Frontend");
        Capacity capacity2 = createCapacity(11L, "Backend");
        Capacity capacity3 = createCapacity(12L, "DevOps");

        Bootcamp bootcamp = createBootcamp(bootcampId, "Full Stack Bootcamp",
                LocalDate.of(2024, 1, 1), 45, List.of(capacity1, capacity2, capacity3));

        CapacityDetail capacityDetail1 = createCapacityDetail(10L, "Frontend", 5);
        CapacityDetail capacityDetail2 = createCapacityDetail(11L, "Backend", 3);
        CapacityDetail capacityDetail3 = createCapacityDetail(12L, "DevOps", 2);

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId)).thenReturn(Flux.empty());
        when(capacityDetailGateway.getCapacitiesByIds(List.of(10L, 11L, 12L)))
                .thenReturn(Flux.just(capacityDetail1, capacityDetail2, capacityDetail3));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getCapacities().size() == 3 &&
                        result.getName().equals("Full Stack Bootcamp")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should preserve bootcamp metadata in detail")
    void testPreserveBootcampMetadata() {
        Long bootcampId = 1L;
        LocalDate releaseDate = LocalDate.of(2024, 3, 15);
        Integer duration = 60;
        String description = "Advanced Java Bootcamp";

        Bootcamp bootcamp = Bootcamp.builder()
                .id(bootcampId)
                .name("Java Advanced")
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .capacities(List.of())
                .build();

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getName().equals("Java Advanced") &&
                        result.getDescription().equals(description) &&
                        result.getReleaseDate().equals(releaseDate) &&
                        result.getDuration().equals(duration)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve bootcamp detail with many persons")
    void testGetBootcampDetailWithManyPersons() {
        Long bootcampId = 1L;

        Bootcamp bootcamp = createBootcamp(bootcampId, "Java Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of());

        Person person1 = createPerson(100L, "John Doe");
        Person person2 = createPerson(101L, "Jane Smith");
        Person person3 = createPerson(102L, "Bob Johnson");
        Person person4 = createPerson(103L, "Alice Williams");

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId))
                .thenReturn(Flux.just(person1, person2, person3, person4));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getPersons().size() == 4 &&
                        result.getCapacities().isEmpty()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle bootcamp with no capacities and no persons")
    void testBootcampWithNoCapacitiesAndNoPersons() {
        Long bootcampId = 1L;

        Bootcamp bootcamp = createBootcamp(bootcampId, "Solo Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of());

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getId().equals(bootcampId) &&
                        result.getPersons().isEmpty() &&
                        result.getCapacities().isEmpty()
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should combine data from multiple gateways correctly")
    void testCombineDataFromMultipleGateways() {
        Long bootcampId = 1L;

        Capacity capacity1 = createCapacity(10L, "Frontend");
        Bootcamp bootcamp = createBootcamp(bootcampId, "Test Bootcamp",
                LocalDate.of(2024, 1, 1), 30, List.of(capacity1));

        CapacityDetail capacityDetail1 = createCapacityDetail(10L, "Frontend", 5);

        Person person1 = createPerson(100L, "John");
        Person person2 = createPerson(101L, "Jane");

        when(bootcampRepository.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(personGateway.getPersonsByBootcampId(bootcampId))
                .thenReturn(Flux.just(person1, person2));
        when(capacityDetailGateway.getCapacitiesByIds(List.of(10L)))
                .thenReturn(Flux.just(capacityDetail1));

        StepVerifier.create(useCase.execute(bootcampId))
                .expectNextMatches(result ->
                        result.getId().equals(bootcampId) &&
                        result.getPersons().size() == 2 &&
                        result.getCapacities().size() == 1 &&
                        result.getName().equals("Test Bootcamp")
                )
                .verifyComplete();
    }

    // Helper methods to create test objects
    private Bootcamp createBootcamp(Long id, String name, LocalDate releaseDate,
                                    Integer duration, List<Capacity> capacities) {
        return Bootcamp.builder()
                .id(id)
                .name(name)
                .description("Test bootcamp: " + name)
                .releaseDate(releaseDate)
                .duration(duration)
                .capacities(capacities)
                .build();
    }

    private Capacity createCapacity(Long id, String name) {
        return Capacity.builder()
                .id(id)
                .name(name)
                .technologies(List.of())
                .build();
    }

    private CapacityDetail createCapacityDetail(Long id, String name, Integer technologyCount) {
        return CapacityDetail.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Person createPerson(Long id, String name) {
        return new Person(name, "LastName", "email@example.com");
    }
}
