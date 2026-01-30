package co.com.bancolombia.r2dbc.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import co.com.bancolombia.r2dbc.common.AuditableModelData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Table("technologies")
public class TechnologyData extends AuditableModelData {
    @Id
    private Long id;
    private String name;
    private String description;
}
