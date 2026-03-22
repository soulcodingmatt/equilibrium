package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

import static io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationConflictLabels.*;
import static io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationFieldPredicates.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.Size;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/** Checks whether a field type supports each Jakarta validation kind. */
public final class ValidationTypeCompatibilityChecker {

  private ValidationTypeCompatibilityChecker() {}

  public static List<String> checkTypeCompatibility(
      String fieldName, TypeMirror fieldType, ValidationInfo validation) {
    String typeName = fieldType.toString();
    String validationType = validation.type;

    if (validationType.equals(NOT_NULL)) {
      return checkNotNull(fieldName, typeName);
    }
    if (validationType.equals(NOT_BLANK)) {
      return checkNotBlank(fieldName, typeName);
    }
    if (validationType.equals(NOT_EMPTY)) {
      return checkNotEmpty(fieldName, typeName);
    }
    if (validationType.equals("Size")) {
      return checkSize(fieldName, typeName, (Size) validation.annotation);
    }
    if (validationType.equals("Min")
        || validationType.equals("Max")
        || validationType.equals(POSITIVE)
        || validationType.equals(POSITIVE_OR_ZERO)
        || validationType.equals(NEGATIVE)
        || validationType.equals(NEGATIVE_OR_ZERO)) {
      return checkNumericConstraint(fieldName, typeName, validationType);
    }
    if (validationType.equals(EMAIL) || validationType.equals(PATTERN)) {
      return checkStringOnlyConstraint(fieldName, typeName, validationType);
    }
    if (validationType.equals(DIGITS)) {
      return checkDigits(fieldName, typeName);
    }
    if (validationType.equals(PAST)
        || validationType.equals(FUTURE)
        || validationType.equals(PAST_OR_PRESENT)
        || validationType.equals(FUTURE_OR_PRESENT)) {
      return checkTemporal(fieldName, typeName, validationType);
    }
    throw new IllegalStateException("Unexpected value: " + validationType);
  }

  private static List<String> checkNotNull(String fieldName, String typeName) {
    List<String> errors = new ArrayList<>();
    if (isPrimitiveType(typeName)) {
      errors.add(
          "@NotNull cannot be applied to primitive field '"
              + fieldName
              + "' of type "
              + typeName
              + ". Only applicable to reference types.");
    }
    return errors;
  }

  private static List<String> checkNotBlank(String fieldName, String typeName) {
    List<String> errors = new ArrayList<>();
    if (!isStringType(typeName)) {
      errors.add(
          "@NotBlank can only be applied to String fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }

  private static List<String> checkNotEmpty(String fieldName, String typeName) {
    List<String> errors = new ArrayList<>();
    if (!isStringType(typeName)
        && !isCollectionType(typeName)
        && !isMapType(typeName)
        && !isArrayType(typeName)) {
      errors.add(
          "@NotEmpty can only be applied to String, Collection, Map, or array fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }

  private static List<String> checkSize(String fieldName, String typeName, Size size) {
    List<String> errors = new ArrayList<>();
    if (!isStringType(typeName)
        && !isCollectionType(typeName)
        && !isMapType(typeName)
        && !isArrayType(typeName)) {
      errors.add(
          "@Size can only be applied to String, Collection, Map, or array fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    if (size.min() < 0) {
      errors.add(
          "@Size min value cannot be negative. Field '"
              + fieldName
              + "' has min="
              + size.min()
              + ".");
    }
    if (size.max() < 0) {
      errors.add(
          "@Size max value cannot be negative. Field '"
              + fieldName
              + "' has max="
              + size.max()
              + ".");
    }
    return errors;
  }

  private static List<String> checkNumericConstraint(
      String fieldName, String typeName, String validationType) {
    List<String> errors = new ArrayList<>();
    if (!isNumericType(typeName)) {
      errors.add(
          "@"
              + validationType
              + " can only be applied to numeric fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }

  private static List<String> checkStringOnlyConstraint(
      String fieldName, String typeName, String validationType) {
    List<String> errors = new ArrayList<>();
    if (!isStringType(typeName)) {
      errors.add(
          "@"
              + validationType
              + " can only be applied to String fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }

  private static List<String> checkDigits(String fieldName, String typeName) {
    List<String> errors = new ArrayList<>();
    if (!isNumericType(typeName) && !isStringType(typeName)) {
      errors.add(
          "@Digits can only be applied to numeric or String fields. Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }

  private static List<String> checkTemporal(
      String fieldName, String typeName, String validationType) {
    List<String> errors = new ArrayList<>();
    if (!isTemporalType(typeName)) {
      errors.add(
          "@"
              + validationType
              + " can only be applied to temporal fields (Date, Calendar, LocalDate, LocalDateTime, etc.). Field '"
              + fieldName
              + IS_OF_TYPE
              + typeName
              + ".");
    }
    return errors;
  }
}
