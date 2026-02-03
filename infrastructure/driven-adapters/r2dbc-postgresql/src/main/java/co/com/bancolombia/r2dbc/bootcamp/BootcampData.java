package co.com.bancolombia.r2dbc.bootcamp;

import co.com.bancolombia.r2dbc.common.AuditableModelData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Table("bootcamps")
public class BootcampData extends AuditableModelData {
    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    @Transient
    private List<Long> capacityIds;
}
