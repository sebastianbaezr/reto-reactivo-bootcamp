package co.com.bancolombia.webclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnClass(WebClient.class)
@EnableConfigurationProperties(ExternalServicesProperties.class)
public class WebClientAutoConfiguration {

    @Bean
    public WebClientFactory webClientFactory(ExternalServicesProperties properties) {
        return new WebClientFactory(properties);
    }
}
