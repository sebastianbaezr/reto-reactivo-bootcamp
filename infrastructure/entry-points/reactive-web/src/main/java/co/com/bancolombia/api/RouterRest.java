package co.com.bancolombia.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler, Optional<TechnologyHandler> technologyHandler) {
        var router = route(GET("/api/usecase/path"), handler::listenGETUseCase)
            .andRoute(GET("/api/otherusercase/path"), handler::listenGETOtherUseCase)
            .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase);

        if (technologyHandler.isPresent()) {
            router = router.andRoute(GET("/api/technologies"), technologyHandler.get()::getTechnologiesByIds)
                .andRoute(POST("/api/technologies"), technologyHandler.get()::registerTechnology)
                .andRoute(GET("/api/technologies/validate"), technologyHandler.get()::validateTechnologies)
                .andRoute(DELETE("/api/technologies/batch"), technologyHandler.get()::softDeleteTechnologies)
                .andRoute(POST("/api/technologies/restore-batch"), technologyHandler.get()::restoreTechnologies);
        }

        return router;
    }
}
