package co.com.bancolombia.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "spring.r2dbc")
public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password) {
}
