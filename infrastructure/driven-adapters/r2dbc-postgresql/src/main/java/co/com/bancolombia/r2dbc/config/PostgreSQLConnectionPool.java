package co.com.bancolombia.r2dbc.config;

// Disabled: Using Spring Boot's R2DBC auto-pooling configuration
// Spring Boot automatically manages the connection pool based on application.yaml settings:
// - spring.r2dbc.url: r2dbc:postgresql://host:port/database?sslmode=require
// - spring.r2dbc.username
// - spring.r2dbc.password
// - spring.r2dbc.pool.* properties

public class PostgreSQLConnectionPool {
    public static final int INITIAL_SIZE = 5;
    public static final int MAX_SIZE = 20;
    public static final int MAX_IDLE_TIME = 30;
}
