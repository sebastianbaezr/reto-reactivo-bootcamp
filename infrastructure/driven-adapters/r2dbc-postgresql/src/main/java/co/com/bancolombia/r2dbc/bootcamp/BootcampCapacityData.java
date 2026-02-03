package co.com.bancolombia.r2dbc.bootcamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("bootcamp_capacities")
public class BootcampCapacityData {
    @Id
    private Long id;
    private Long bootcampId;
    private Long capacityId;
}
