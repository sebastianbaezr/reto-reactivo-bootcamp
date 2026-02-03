package co.com.bancolombia.model.common;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AuditableModel {

    protected LocalDateTime createdAt;

    protected LocalDateTime updatedAt;
}
