package io.github.soulcodingmatt.equilibrium.processor.util;

import io.github.soulcodingmatt.equilibrium.annotations.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.annotations.dto.validation.*;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for validating @ValidateDto annotation combinations
 * to prevent invalid Jakarta Bean Validation configurations.
 */
public class ValidationConflictUtil {
    
    private final Types typeUtils;
    
    public ValidationConflictUtil(Types typeUtils) {
        this.typeUtils = typeUtils;
    }
    
    /**
     * Validates all ValidateDto annotations on a field for conflicts and type compatibility.
     * 
     * @param field the field being validated
     * @param validateDtoAnnotations array of ValidateDto annotations on the field
     * @return list of validation error messages, empty if no conflicts found
     */
    public List<String> validateField(VariableElement field, ValidateDto[] validateDtoAnnotations) {
        List<String> errors = new ArrayList<>();
        
        for (ValidateDto validateDto : validateDtoAnnotations) {
            errors.addAll(validateSingleAnnotation(field, validateDto));
        }
        
        return errors;
    }
    
    /**
     * Validates a single ValidateDto annotation for conflicts and type compatibility.
     */
    private List<String> validateSingleAnnotation(VariableElement field, ValidateDto validateDto) {
        List<String> errors = new ArrayList<>();
        TypeMirror fieldType = field.asType();
        String fieldName = field.getSimpleName().toString();
        
        // Collect all active validations
        List<ValidationInfo> activeValidations = collectActiveValidations(validateDto);
        
        // Check type compatibility for each validation
        for (ValidationInfo validation : activeValidations) {
            List<String> typeErrors = checkTypeCompatibility(fieldName, fieldType, validation);
            errors.addAll(typeErrors);
        }
        
        // Check logical conflicts between validations
        List<String> conflictErrors = checkLogicalConflicts(fieldName, activeValidations);
        errors.addAll(conflictErrors);
        
        return errors;
    }
    
    /**
     * Collects all active validations from a ValidateDto annotation.
     */
    private List<ValidationInfo> collectActiveValidations(ValidateDto validateDto) {
        List<ValidationInfo> validations = new ArrayList<>();
        
        // Check NotNull
        NotNull notNull = validateDto.notNull();
        if (!notNull.message().isEmpty()) {
            validations.add(new ValidationInfo("NotNull", notNull));
        }
        
        // Check NotBlank
        NotBlank notBlank = validateDto.notBlank();
        if (!notBlank.message().isEmpty()) {
            validations.add(new ValidationInfo("NotBlank", notBlank));
        }
        
        // Check NotEmpty
        NotEmpty notEmpty = validateDto.notEmpty();
        if (!notEmpty.message().isEmpty()) {
            validations.add(new ValidationInfo("NotEmpty", notEmpty));
        }
        
        // Check Size
        Size size = validateDto.size();
        if (size.min() != -1 || size.max() != -1) {
            // Validate Size parameters
            if (size.min() < 0) {
                // This will be caught in type compatibility check
            }
            if (size.max() < 0) {
                // This will be caught in type compatibility check  
            }
            if (size.min() > size.max()) {
                // This will be caught in logical conflicts check
            }
            validations.add(new ValidationInfo("Size", size));
        }
        
        // Check Min
        Min min = validateDto.min();
        if (min.value() != Long.MIN_VALUE) {
            validations.add(new ValidationInfo("Min", min));
        }
        
        // Check Max
        Max max = validateDto.max();
        if (max.value() != Long.MAX_VALUE) {
            validations.add(new ValidationInfo("Max", max));
        }
        
        // Check Email
        Email email = validateDto.email();
        if (!email.message().isEmpty()) {
            validations.add(new ValidationInfo("Email", email));
        }
        
        // Check Pattern
        Pattern pattern = validateDto.pattern();
        if (!pattern.regexp().isEmpty()) {
            validations.add(new ValidationInfo("Pattern", pattern));
        }
        
        // Check Positive
        Positive positive = validateDto.positive();
        if (!positive.message().isEmpty()) {
            validations.add(new ValidationInfo("Positive", positive));
        }
        
        // Check PositiveOrZero
        PositiveOrZero positiveOrZero = validateDto.positiveOrZero();
        if (!positiveOrZero.message().isEmpty()) {
            validations.add(new ValidationInfo("PositiveOrZero", positiveOrZero));
        }
        
        // Check Negative
        Negative negative = validateDto.negative();
        if (!negative.message().isEmpty()) {
            validations.add(new ValidationInfo("Negative", negative));
        }
        
        // Check NegativeOrZero
        NegativeOrZero negativeOrZero = validateDto.negativeOrZero();
        if (!negativeOrZero.message().isEmpty()) {
            validations.add(new ValidationInfo("NegativeOrZero", negativeOrZero));
        }
        
        // Check Digits
        Digits digits = validateDto.digits();
        if (digits.integer() != -1 || digits.fraction() != -1) {
            validations.add(new ValidationInfo("Digits", digits));
        }
        
        // Check Past
        Past past = validateDto.past();
        if (!past.message().isEmpty()) {
            validations.add(new ValidationInfo("Past", past));
        }
        
        // Check Future
        Future future = validateDto.future();
        if (!future.message().isEmpty()) {
            validations.add(new ValidationInfo("Future", future));
        }
        
        // Check PastOrPresent
        PastOrPresent pastOrPresent = validateDto.pastOrPresent();
        if (!pastOrPresent.message().isEmpty()) {
            validations.add(new ValidationInfo("PastOrPresent", pastOrPresent));
        }
        
        // Check FutureOrPresent
        FutureOrPresent futureOrPresent = validateDto.futureOrPresent();
        if (!futureOrPresent.message().isEmpty()) {
            validations.add(new ValidationInfo("FutureOrPresent", futureOrPresent));
        }
        
        return validations;
    }
    
    /**
     * Checks type compatibility between field type and validation annotation.
     */
    private List<String> checkTypeCompatibility(String fieldName, TypeMirror fieldType, ValidationInfo validation) {
        List<String> errors = new ArrayList<>();
        String typeName = fieldType.toString();
        String validationType = validation.type;
        
        switch (validationType) {
            case "NotNull":
                // NotNull can be applied to any reference type, but not primitives
                if (isPrimitiveType(typeName)) {
                    errors.add("@NotNull cannot be applied to primitive field '" + fieldName + "' of type " + typeName + ". Only applicable to reference types.");
                }
                break;
                
            case "NotBlank":
                if (!isStringType(typeName)) {
                    errors.add("@NotBlank can only be applied to String fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
                
            case "NotEmpty":
                if (!isStringType(typeName) && !isCollectionType(typeName) && !isMapType(typeName) && !isArrayType(typeName)) {
                    errors.add("@NotEmpty can only be applied to String, Collection, Map, or array fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
                
            case "Size":
                Size size = (Size) validation.annotation;
                if (!isStringType(typeName) && !isCollectionType(typeName) && !isMapType(typeName) && !isArrayType(typeName)) {
                    errors.add("@Size can only be applied to String, Collection, Map, or array fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                if (size.min() < 0) {
                    errors.add("@Size min value cannot be negative. Field '" + fieldName + "' has min=" + size.min() + ".");
                }
                if (size.max() < 0) {
                    errors.add("@Size max value cannot be negative. Field '" + fieldName + "' has max=" + size.max() + ".");
                }
                break;
                
            case "Min":
            case "Max":
            case "Positive":
            case "PositiveOrZero":
            case "Negative":
            case "NegativeOrZero":
                if (!isNumericType(typeName)) {
                    errors.add("@" + validationType + " can only be applied to numeric fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
                
            case "Email":
            case "Pattern":
                if (!isStringType(typeName)) {
                    errors.add("@" + validationType + " can only be applied to String fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
                
            case "Digits":
                if (!isNumericType(typeName) && !isStringType(typeName)) {
                    errors.add("@Digits can only be applied to numeric or String fields. Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
                
            case "Past":
            case "Future":
            case "PastOrPresent":
            case "FutureOrPresent":
                if (!isTemporalType(typeName)) {
                    errors.add("@" + validationType + " can only be applied to temporal fields (Date, Calendar, LocalDate, LocalDateTime, etc.). Field '" + fieldName + "' is of type " + typeName + ".");
                }
                break;
        }
        
        return errors;
    }
    
    /**
     * Checks for logical conflicts between validation annotations.
     */
    private List<String> checkLogicalConflicts(String fieldName, List<ValidationInfo> validations) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < validations.size(); i++) {
            for (int j = i + 1; j < validations.size(); j++) {
                ValidationInfo a = validations.get(i);
                ValidationInfo b = validations.get(j);
                
                String conflict = checkPairConflict(fieldName, a, b);
                if (conflict != null) {
                    errors.add(conflict);
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Checks for conflicts between two specific validation annotations.
     */
    private String checkPairConflict(String fieldName, ValidationInfo a, ValidationInfo b) {
        String typeA = a.type;
        String typeB = b.type;
        
        // Direct conflicts from the blacklist table
        if ((typeA.equals("Positive") && typeB.equals("Negative")) ||
            (typeA.equals("Negative") && typeB.equals("Positive"))) {
            return "Field '" + fieldName + "': @Positive and @Negative are contradictory. A value cannot be both > 0 and < 0.";
        }
        
        if ((typeA.equals("Positive") && typeB.equals("NegativeOrZero")) ||
            (typeA.equals("NegativeOrZero") && typeB.equals("Positive"))) {
            return "Field '" + fieldName + "': @Positive and @NegativeOrZero are contradictory. > 0 contradicts ≤ 0.";
        }
        
        if ((typeA.equals("PositiveOrZero") && typeB.equals("Negative")) ||
            (typeA.equals("Negative") && typeB.equals("PositiveOrZero"))) {
            return "Field '" + fieldName + "': @PositiveOrZero and @Negative are contradictory. ≥ 0 contradicts < 0.";
        }
        
        if ((typeA.equals("PositiveOrZero") && typeB.equals("NegativeOrZero")) ||
            (typeA.equals("NegativeOrZero") && typeB.equals("PositiveOrZero"))) {
            return "Field '" + fieldName + "': @PositiveOrZero and @NegativeOrZero are contradictory. ≥ 0 contradicts ≤ 0 (only 0 would be valid).";
        }
        
        if ((typeA.equals("Past") && typeB.equals("Future")) ||
            (typeA.equals("Future") && typeB.equals("Past"))) {
            return "Field '" + fieldName + "': @Past and @Future are contradictory. A date cannot be in the past and the future.";
        }
        
        if ((typeA.equals("Past") && typeB.equals("FutureOrPresent")) ||
            (typeA.equals("FutureOrPresent") && typeB.equals("Past"))) {
            return "Field '" + fieldName + "': @Past and @FutureOrPresent are contradictory. Past contradicts future or present.";
        }
        
        if ((typeA.equals("Future") && typeB.equals("PastOrPresent")) ||
            (typeA.equals("PastOrPresent") && typeB.equals("Future"))) {
            return "Field '" + fieldName + "': @Future and @PastOrPresent are contradictory. Future contradicts past or present.";
        }
        
        // Min/Max value conflicts
        if (typeA.equals("Min") && typeB.equals("Max")) {
            Min min = (Min) a.annotation;
            Max max = (Max) b.annotation;
            if (min.value() > max.value()) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") is greater than @Max(" + max.value() + "). Min value must be ≤ max value.";
            }
        } else if (typeA.equals("Max") && typeB.equals("Min")) {
            Max max = (Max) a.annotation;
            Min min = (Min) b.annotation;
            if (min.value() > max.value()) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") is greater than @Max(" + max.value() + "). Min value must be ≤ max value.";
            }
        }
        
        // Size min/max conflicts
        if (typeA.equals("Size") && typeB.equals("Size")) {
            // This shouldn't happen as we only collect one Size annotation per ValidateDto
            Size sizeA = (Size) a.annotation;
            Size sizeB = (Size) b.annotation;
            // Could check if they have conflicting ranges, but this is unlikely
        }
        
        // Min value with Positive/Negative conflicts
        if (typeA.equals("Min") && typeB.equals("Positive")) {
            Min min = (Min) a.annotation;
            if (min.value() <= 0) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") allows non-positive values, which contradicts @Positive (> 0).";
            }
        } else if (typeA.equals("Positive") && typeB.equals("Min")) {
            Min min = (Min) b.annotation;
            if (min.value() <= 0) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") allows non-positive values, which contradicts @Positive (> 0).";
            }
        }
        
        if (typeA.equals("Min") && typeB.equals("Negative")) {
            Min min = (Min) a.annotation;
            if (min.value() >= 0) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") requires non-negative values, which contradicts @Negative (< 0).";
            }
        } else if (typeA.equals("Negative") && typeB.equals("Min")) {
            Min min = (Min) b.annotation;
            if (min.value() >= 0) {
                return "Field '" + fieldName + "': @Min(" + min.value() + ") requires non-negative values, which contradicts @Negative (< 0).";
            }
        }
        
        // Max value with Positive/Negative conflicts
        if (typeA.equals("Max") && typeB.equals("Positive")) {
            Max max = (Max) a.annotation;
            if (max.value() <= 0) {
                return "Field '" + fieldName + "': @Max(" + max.value() + ") allows only non-positive values, which contradicts @Positive (> 0).";
            }
        } else if (typeA.equals("Positive") && typeB.equals("Max")) {
            Max max = (Max) b.annotation;
            if (max.value() <= 0) {
                return "Field '" + fieldName + "': @Max(" + max.value() + ") allows only non-positive values, which contradicts @Positive (> 0).";
            }
        }
        
        if (typeA.equals("Max") && typeB.equals("Negative")) {
            Max max = (Max) a.annotation;
            if (max.value() >= 0) {
                return "Field '" + fieldName + "': @Max(" + max.value() + ") allows non-negative values, which contradicts @Negative (< 0).";
            }
        } else if (typeA.equals("Negative") && typeB.equals("Max")) {
            Max max = (Max) b.annotation;
            if (max.value() >= 0) {
                return "Field '" + fieldName + "': @Max(" + max.value() + ") allows non-negative values, which contradicts @Negative (< 0).";
            }
        }
        
        // NotEmpty with Size max=0 conflict
        if ((typeA.equals("NotEmpty") && typeB.equals("Size")) ||
            (typeA.equals("Size") && typeB.equals("NotEmpty"))) {
            Size size = typeA.equals("Size") ? (Size) a.annotation : (Size) b.annotation;
            if (size.max() == 0) {
                return "Field '" + fieldName + "': @NotEmpty contradicts @Size(max=0). An element cannot be both not empty and have maximum size 0.";
            }
        }
        
        return null; // No conflict found
    }
    
    // Helper methods for type checking
    private boolean isPrimitiveType(String typeName) {
        return Arrays.asList("int", "long", "short", "byte", "float", "double", "boolean", "char").contains(typeName);
    }
    
    private boolean isStringType(String typeName) {
        return typeName.equals("java.lang.String") || typeName.equals("String");
    }
    
    private boolean isNumericType(String typeName) {
        return Arrays.asList(
            "int", "long", "short", "byte", "float", "double",
            "java.lang.Integer", "Integer",
            "java.lang.Long", "Long", 
            "java.lang.Short", "Short",
            "java.lang.Byte", "Byte",
            "java.lang.Float", "Float",
            "java.lang.Double", "Double",
            "java.math.BigDecimal", "BigDecimal",
            "java.math.BigInteger", "BigInteger"
        ).contains(typeName);
    }
    
    private boolean isCollectionType(String typeName) {
        return typeName.contains("java.util.List") || typeName.contains("List") ||
               typeName.contains("java.util.Set") || typeName.contains("Set") ||
               typeName.contains("java.util.Collection") || typeName.contains("Collection") ||
               typeName.contains("java.util.Queue") || typeName.contains("Queue") ||
               typeName.contains("java.util.Deque") || typeName.contains("Deque");
    }
    
    private boolean isMapType(String typeName) {
        return typeName.contains("java.util.Map") || typeName.contains("Map");
    }
    
    private boolean isArrayType(String typeName) {
        return typeName.contains("[]");
    }
    
    private boolean isTemporalType(String typeName) {
        return Arrays.asList(
            "java.util.Date", "Date",
            "java.util.Calendar", "Calendar",
            "java.time.LocalDate", "LocalDate",
            "java.time.LocalDateTime", "LocalDateTime",
            "java.time.LocalTime", "LocalTime",
            "java.time.ZonedDateTime", "ZonedDateTime",
            "java.time.OffsetDateTime", "OffsetDateTime",
            "java.time.Instant", "Instant"
        ).contains(typeName);
    }
    
    /**
     * Helper class to hold validation information.
     */
    private static class ValidationInfo {
        final String type;
        final Object annotation;
        
        ValidationInfo(String type, Object annotation) {
            this.type = type;
            this.annotation = annotation;
        }
    }
} 