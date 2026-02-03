package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.TechnologyRequest;
import co.com.bancolombia.api.dto.response.TechnologyResponse;
import co.com.bancolombia.api.dto.response.TechnologySimpleResponse;
import co.com.bancolombia.api.dto.response.ValidateTechnologiesResponse;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.Technology;
import co.com.bancolombia.usecase.gettechnologiesbyids.GetTechnologiesByIdsUseCase;
import co.com.bancolombia.usecase.registertechnology.RegisterTechnologyUseCase;
import co.com.bancolombia.usecase.validatetechnologies.ValidateTechnologiesResult;
import co.com.bancolombia.usecase.validatetechnologies.ValidateTechnologiesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TechnologyHandler Tests")
class TechnologyHandlerTest {

    @Mock
    private RegisterTechnologyUseCase registerTechnologyUseCase;

    @Mock
    private ValidateTechnologiesUseCase validateTechnologiesUseCase;

    @Mock
    private GetTechnologiesByIdsUseCase getTechnologiesByIdsUseCase;

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

    @Test
    @DisplayName("Should validate technologies successfully with all ids existing")
    void testValidateTechnologies_AllExist() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of("1,2,3"));

        ValidateTechnologiesResult result = ValidateTechnologiesResult.builder()
            .existingIds(List.of(1L, 2L, 3L))
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        ValidateTechnologiesResponse response = ValidateTechnologiesResponse.builder()
            .existingIds(List.of(1L, 2L, 3L))
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        when(validateTechnologiesUseCase.execute(List.of(1L, 2L, 3L)))
            .thenReturn(Mono.just(result));
        when(technologyMapper.toValidateResponse(result))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.validateTechnologies(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate technologies with some ids missing")
    void testValidateTechnologies_SomeMissing() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of("1,2,3,4"));

        ValidateTechnologiesResult result = ValidateTechnologiesResult.builder()
            .existingIds(List.of(1L, 3L))
            .notFoundIds(List.of(2L, 4L))
            .allExist(false)
            .build();

        ValidateTechnologiesResponse response = ValidateTechnologiesResponse.builder()
            .existingIds(List.of(1L, 3L))
            .notFoundIds(List.of(2L, 4L))
            .allExist(false)
            .build();

        when(validateTechnologiesUseCase.execute(List.of(1L, 2L, 3L, 4L)))
            .thenReturn(Mono.just(result));
        when(technologyMapper.toValidateResponse(result))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.validateTechnologies(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate technologies with empty ids parameter")
    void testValidateTechnologies_EmptyIds() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of(""));

        ValidateTechnologiesResult result = ValidateTechnologiesResult.builder()
            .existingIds(List.of())
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        ValidateTechnologiesResponse response = ValidateTechnologiesResponse.builder()
            .existingIds(List.of())
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        when(validateTechnologiesUseCase.execute(List.of()))
            .thenReturn(Mono.just(result));
        when(technologyMapper.toValidateResponse(result))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.validateTechnologies(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate technologies with no ids parameter")
    void testValidateTechnologies_NoIdsParam() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.empty());

        ValidateTechnologiesResult result = ValidateTechnologiesResult.builder()
            .existingIds(List.of())
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        ValidateTechnologiesResponse response = ValidateTechnologiesResponse.builder()
            .existingIds(List.of())
            .notFoundIds(List.of())
            .allExist(true)
            .build();

        when(validateTechnologiesUseCase.execute(List.of()))
            .thenReturn(Mono.just(result));
        when(technologyMapper.toValidateResponse(result))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.validateTechnologies(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get technologies by ids successfully")
    void testGetTechnologiesByIds_Success() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of("1,2"));

        Technology tech1 = buildTechnology(1L, "Java", "Language");
        Technology tech2 = buildTechnology(2L, "Python", "Language");

        TechnologySimpleResponse response1 = TechnologySimpleResponse.builder()
            .id(1L)
            .name("Java")
            .build();

        TechnologySimpleResponse response2 = TechnologySimpleResponse.builder()
            .id(2L)
            .name("Python")
            .build();

        when(getTechnologiesByIdsUseCase.execute(List.of(1L, 2L)))
            .thenReturn(Flux.just(tech1, tech2));
        when(technologyMapper.toSimpleResponse(tech1))
            .thenReturn(response1);
        when(technologyMapper.toSimpleResponse(tech2))
            .thenReturn(response2);

        // Act & Assert
        StepVerifier.create(technologyHandler.getTechnologiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get technologies with empty ids")
    void testGetTechnologiesByIds_EmptyIds() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of(""));

        when(getTechnologiesByIdsUseCase.execute(List.of()))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(technologyHandler.getTechnologiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get technologies with no ids parameter")
    void testGetTechnologiesByIds_NoIdsParam() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.empty());

        when(getTechnologiesByIdsUseCase.execute(List.of()))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(technologyHandler.getTechnologiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get single technology by id")
    void testGetTechnologiesByIds_SingleId() {
        // Arrange
        when(serverRequest.queryParam("ids"))
            .thenReturn(Optional.of("1"));

        Technology tech = buildTechnology(1L, "Java", "Language");

        TechnologySimpleResponse response = TechnologySimpleResponse.builder()
            .id(1L)
            .name("Java")
            .build();

        when(getTechnologiesByIdsUseCase.execute(List.of(1L)))
            .thenReturn(Flux.just(tech));
        when(technologyMapper.toSimpleResponse(tech))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(technologyHandler.getTechnologiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
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
