package co.com.bancolombia.webclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "external-services")
public record ExternalServicesProperties(
    Map<String, ServiceProperties> services
) {
    public ExternalServicesProperties {
        if (services == null) {
            services = Map.of();
        }
    }

    public ServiceProperties getService(String serviceName) {
        ServiceProperties props = services.get(serviceName);
        if (props == null) {
            throw new IllegalArgumentException(
                "Service configuration not found: " + serviceName);
        }
        return props;
    }
}
