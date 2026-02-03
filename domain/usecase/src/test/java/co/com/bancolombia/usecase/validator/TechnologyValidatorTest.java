package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TechnologyValidator Tests")
class TechnologyValidatorTest {

    // ============= Name Validation Tests =============

    @Test
    @DisplayName("Should pass validation for valid name")
    void testValidateName_ValidName() {
        // Arrange & Act & Assert - should not throw
        TechnologyValidator.validateName("Spring Boot");
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testValidateName_NullName() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateName(null)
        );
        assertEquals(DomainErrorCode.NAME_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void testValidateName_EmptyName() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateName("")
        );
        assertEquals(DomainErrorCode.NAME_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when name contains only whitespace")
    void testValidateName_BlankName() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateName("   ")
        );
        assertEquals(DomainErrorCode.NAME_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when name exceeds max length")
    void testValidateName_NameTooLong() {
        // Arrange
        String nameWithLength51 = "a".repeat(51);

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateName(nameWithLength51)
        );
        assertEquals(DomainErrorCode.INVALID_NAME_LENGTH.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should accept name with exactly 50 characters")
    void testValidateName_MaxLength() {
        // Arrange
        String nameWithLength50 = "a".repeat(50);

        // Act & Assert - should not throw
        TechnologyValidator.validateName(nameWithLength50);
    }

    @Test
    @DisplayName("Should accept name with 1 character")
    void testValidateName_MinLength() {
        // Act & Assert - should not throw
        TechnologyValidator.validateName("A");
    }

    @Test
    @DisplayName("Should accept single character name")
    void testValidateName_SingleCharacter() {
        // Act & Assert - should not throw
        TechnologyValidator.validateName("R");
    }

    @Test
    @DisplayName("Should accept name with special characters")
    void testValidateName_SpecialCharacters() {
        // Act & Assert - should not throw
        TechnologyValidator.validateName("C++");
    }

    @Test
    @DisplayName("Should accept name with spaces")
    void testValidateName_WithSpaces() {
        // Act & Assert - should not throw
        TechnologyValidator.validateName("Spring Boot");
    }

    @Test
    @DisplayName("Should accept name with dots")
    void testValidateName_WithDots() {
        // Act & Assert - should not throw
        TechnologyValidator.validateName(".NET Framework");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 25, 49, 50})
    @DisplayName("Should accept names within valid length range")
    void testValidateName_ValidLengths(int length) {
        // Arrange
        String name = "a".repeat(length);

        // Act & Assert - should not throw
        TechnologyValidator.validateName(name);
    }

    // ============= Description Validation Tests =============

    @Test
    @DisplayName("Should pass validation for valid description")
    void testValidateDescription_ValidDescription() {
        // Arrange & Act & Assert - should not throw
        TechnologyValidator.validateDescription("A powerful web framework");
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void testValidateDescription_NullDescription() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateDescription(null)
        );
        assertEquals(DomainErrorCode.DESCRIPTION_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when description is empty")
    void testValidateDescription_EmptyDescription() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateDescription("")
        );
        assertEquals(DomainErrorCode.DESCRIPTION_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when description contains only whitespace")
    void testValidateDescription_BlankDescription() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateDescription("   ")
        );
        assertEquals(DomainErrorCode.DESCRIPTION_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should throw exception when description exceeds max length")
    void testValidateDescription_DescriptionTooLong() {
        // Arrange
        String descriptionWithLength91 = "a".repeat(91);

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateDescription(descriptionWithLength91)
        );
        assertEquals(DomainErrorCode.INVALID_DESCRIPTION_LENGTH.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Should accept description with exactly 90 characters")
    void testValidateDescription_MaxLength() {
        // Arrange
        String descriptionWithLength90 = "a".repeat(90);

        // Act & Assert - should not throw
        TechnologyValidator.validateDescription(descriptionWithLength90);
    }

    @Test
    @DisplayName("Should accept description with 1 character")
    void testValidateDescription_MinLength() {
        // Act & Assert - should not throw
        TechnologyValidator.validateDescription("A");
    }

    @Test
    @DisplayName("Should accept description with special characters")
    void testValidateDescription_SpecialCharacters() {
        // Act & Assert - should not throw
        TechnologyValidator.validateDescription("Framework for building apps (fast & reliable)");
    }

    @Test
    @DisplayName("Should accept description with numbers")
    void testValidateDescription_WithNumbers() {
        // Act & Assert - should not throw
        TechnologyValidator.validateDescription("Version 2.0 released in 2024");
    }

    @Test
    @DisplayName("Should accept description with newlines")
    void testValidateDescription_WithNewlines() {
        // Act & Assert - should not throw
        TechnologyValidator.validateDescription("Line1\nLine2\nLine3");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 45, 89, 90})
    @DisplayName("Should accept descriptions within valid length range")
    void testValidateDescription_ValidLengths(int length) {
        // Arrange
        String description = "a".repeat(length);

        // Act & Assert - should not throw
        TechnologyValidator.validateDescription(description);
    }

    // ============= Boundary Tests =============

    @Test
    @DisplayName("Should accept both name and description at max length")
    void testValidate_BothAtMaxLength() {
        // Arrange
        String maxName = "a".repeat(50);
        String maxDescription = "a".repeat(90);

        // Act & Assert - should not throw
        TechnologyValidator.validateName(maxName);
        TechnologyValidator.validateDescription(maxDescription);
    }

    @Test
    @DisplayName("Should reject both name and description when too long")
    void testValidate_BothTooLong() {
        // Arrange
        String longName = "a".repeat(51);
        String longDescription = "a".repeat(91);

        // Act & Assert
        assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateName(longName)
        );
        assertThrows(
            BusinessException.class,
            () -> TechnologyValidator.validateDescription(longDescription)
        );
    }
}
