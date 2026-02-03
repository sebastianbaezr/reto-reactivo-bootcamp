package co.com.bancolombia.model.capacity;

import co.com.bancolombia.model.common.AuditableModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Capacity extends AuditableModel {
    private Long id;
    private String name;
    private String description;
}
