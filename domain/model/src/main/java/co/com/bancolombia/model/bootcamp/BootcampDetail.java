package co.com.bancolombia.model.bootcamp;

import co.com.bancolombia.model.capacity.CapacityDetail;
import co.com.bancolombia.model.person.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampDetail {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<Person> persons;
    private List<CapacityDetail> capacities;
}
