package co.com.bancolombia.model.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import co.com.bancolombia.model.common.AuditableModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Technology extends AuditableModel {
    private Long id;
    private String name;
    private String description;
}
