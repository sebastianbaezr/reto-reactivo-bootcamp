package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.dto.response.TechnologyResponse;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.usecase.registertechnology.RegisterTechnologyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TechnologyHandler Tests")
class TechnologyHandlerTest {

    @Mock
    private RegisterTechnologyUseCase registerTechnologyUseCase;

    @Mock
    private TechnologyMapper technologyMapper;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private TechnologyHandler technologyHandler;

    @Test
    @DisplayName("Should register technology successfully and return 201 status")
    void testRegisterTechnology_Success() {
        // Arrange
        String name = "Spring Boot";
        String description = "A framework for building Java applications";
        TechnologyRequest request = buildValidRequest(name, description);
        Technology technology = buildTechnology(null, name, description);
        Technology savedTechnology = buildTechnology(1L, name, description);
        TechnologyResponse response = buildResponse(1L, name, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.just(savedTechnology));
        when(technologyMapper.toResponse(savedTechnology))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 201)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return 409 when technology name already exists")
    void testRegisterTechnology_DuplicateName() {
        // Arrange
        String name = "React";
        String description = "A JavaScript library";
        TechnologyRequest request = buildValidRequest(name, description);
        Technology technology = buildTechnology(null, name, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_NAME_ALREADY_EXISTS)));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return 400 when name is empty")
    void testRegisterTechnology_EmptyName() {
        // Arrange
        TechnologyRequest request = buildValidRequest("", "Valid description");
        Technology technology = buildTechnology(null, "", "Valid description");

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.NAME_REQUIRED)));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return 400 when name exceeds max length")
    void testRegisterTechnology_NameTooLong() {
        // Arrange
        String longName = "a".repeat(51);
        String description = "Valid description";
        TechnologyRequest request = buildValidRequest(longName, description);
        Technology technology = buildTechnology(null, longName, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.INVALID_NAME_LENGTH)));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return 400 when description is empty")
    void testRegisterTechnology_EmptyDescription() {
        // Arrange
        TechnologyRequest request = buildValidRequest("Java", "");
        Technology technology = buildTechnology(null, "Java", "");

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.DESCRIPTION_REQUIRED)));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return 400 when description exceeds max length")
    void testRegisterTechnology_DescriptionTooLong() {
        // Arrange
        String name = "Python";
        String longDescription = "a".repeat(91);
        TechnologyRequest request = buildValidRequest(name, longDescription);
        Technology technology = buildTechnology(null, name, longDescription);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.INVALID_DESCRIPTION_LENGTH)));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle name with exact maximum length")
    void testRegisterTechnology_NameExactMaxLength() {
        // Arrange
        String name = "a".repeat(50);
        String description = "Valid description";
        TechnologyRequest request = buildValidRequest(name, description);
        Technology technology = buildTechnology(null, name, description);
        Technology savedTechnology = buildTechnology(2L, name, description);
        TechnologyResponse response = buildResponse(2L, name, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.just(savedTechnology));
        when(technologyMapper.toResponse(savedTechnology))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 201)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle description with exact maximum length")
    void testRegisterTechnology_DescriptionExactMaxLength() {
        // Arrange
        String name = "NodeJS";
        String description = "a".repeat(90);
        TechnologyRequest request = buildValidRequest(name, description);
        Technology technology = buildTechnology(null, name, description);
        Technology savedTechnology = buildTechnology(3L, name, description);
        TechnologyResponse response = buildResponse(3L, name, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.just(savedTechnology));
        when(technologyMapper.toResponse(savedTechnology))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 201)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle generic exception from use case")
    void testRegisterTechnology_UnexpectedError() {
        // Arrange
        String name = "Go";
        String description = "A programming language";
        TechnologyRequest request = buildValidRequest(name, description);
        Technology technology = buildTechnology(null, name, description);

        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.just(request));
        when(technologyMapper.toEntity(request))
            .thenReturn(technology);
        when(registerTechnologyUseCase.execute(technology))
            .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle null body from request")
    void testRegisterTechnology_NullBody() {
        // Arrange
        when(serverRequest.bodyToMono(TechnologyRequest.class))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(technologyHandler.registerTechnology(serverRequest))
            .expectComplete()
            .verify();
    }

    // Helper methods
    private TechnologyRequest buildValidRequest(String name, String description) {
        return TechnologyRequest.builder()
            .name(name)
            .description(description)
            .build();
    }

    private Technology buildTechnology(Long id, String name, String description) {
        return Technology.builder()
            .id(id)
            .name(name)
            .description(description)
            .build();
    }

    private TechnologyResponse buildResponse(Long id, String name, String description) {
        return TechnologyResponse.builder()
            .id(id)
            .name(name)
            .description(description)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
