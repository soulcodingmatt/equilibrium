package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

import static io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationConflictLabels.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import java.util.ArrayList;
import java.util.List;

/** Detects contradictory pairs of Jakarta validations on the same field. */
public final class ValidationPairConflictChecker {

  private ValidationPairConflictChecker() {}

  public static List<String> checkLogicalConflicts(
      String fieldName, List<ValidationInfo> validations) {
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

  public static String checkPairConflict(String fieldName, ValidationInfo a, ValidationInfo b) {
    String typeA = a.constraintKind;
    String typeB = b.constraintKind;

    String conflictMessage = checkSignContradictions(fieldName, typeA, typeB);
    if (conflictMessage != null) {
      return conflictMessage;
    }
    conflictMessage = checkTemporalContradictions(fieldName, typeA, typeB);
    if (conflictMessage != null) {
      return conflictMessage;
    }
    conflictMessage = checkMinMaxOrder(fieldName, typeA, typeB, a, b);
    if (conflictMessage != null) {
      return conflictMessage;
    }
    conflictMessage = checkMinVersusSign(fieldName, typeA, typeB, a, b);
    if (conflictMessage != null) {
      return conflictMessage;
    }
    conflictMessage = checkMaxVersusSign(fieldName, typeA, typeB, a, b);
    if (conflictMessage != null) {
      return conflictMessage;
    }
    return checkNotEmptyVersusSize(fieldName, typeA, typeB, a, b);
  }

  private static String symmetricMessage(
      String fieldName, String typeA, String typeB, String left, String right, String messageBody) {
    if ((typeA.equals(left) && typeB.equals(right))
        || (typeA.equals(right) && typeB.equals(left))) {
      return FIELD + fieldName + "': " + messageBody;
    }
    return null;
  }

  private static String checkSignContradictions(String fieldName, String typeA, String typeB) {
    String message =
        symmetricMessage(
            fieldName,
            typeA,
            typeB,
            POSITIVE,
            NEGATIVE,
            "@Positive and @Negative are contradictory. A value cannot be both > 0 and < 0.");
    if (message != null) {
      return message;
    }
    message =
        symmetricMessage(
            fieldName,
            typeA,
            typeB,
            POSITIVE,
            NEGATIVE_OR_ZERO,
            "@Positive and @NegativeOrZero are contradictory. > 0 contradicts ≤ 0.");
    if (message != null) {
      return message;
    }
    message =
        symmetricMessage(
            fieldName,
            typeA,
            typeB,
            POSITIVE_OR_ZERO,
            NEGATIVE,
            "@PositiveOrZero and @Negative are contradictory. ≥ 0 contradicts < 0.");
    if (message != null) {
      return message;
    }
    return symmetricMessage(
        fieldName,
        typeA,
        typeB,
        POSITIVE_OR_ZERO,
        NEGATIVE_OR_ZERO,
        "@PositiveOrZero and @NegativeOrZero are contradictory. ≥ 0 contradicts ≤ 0 (only 0 would be valid).");
  }

  private static String checkTemporalContradictions(String fieldName, String typeA, String typeB) {
    String message =
        symmetricMessage(
            fieldName,
            typeA,
            typeB,
            PAST,
            FUTURE,
            "@Past and @Future are contradictory. A date cannot be in the past and the future.");
    if (message != null) {
      return message;
    }
    message =
        symmetricMessage(
            fieldName,
            typeA,
            typeB,
            PAST,
            FUTURE_OR_PRESENT,
            "@Past and @FutureOrPresent are contradictory. Past contradicts future or present.");
    if (message != null) {
      return message;
    }
    return symmetricMessage(
        fieldName,
        typeA,
        typeB,
        FUTURE,
        PAST_OR_PRESENT,
        "@Future and @PastOrPresent are contradictory. Future contradicts past or present.");
  }

  private static String checkMinMaxOrder(
      String fieldName, String typeA, String typeB, ValidationInfo a, ValidationInfo b) {
    if (typeA.equals("Min") && typeB.equals("Max")) {
      return minGreaterThanMaxMessage(
          fieldName, (Min) a.constraintAnnotation, (Max) b.constraintAnnotation);
    }
    if (typeA.equals("Max") && typeB.equals("Min")) {
      return minGreaterThanMaxMessage(
          fieldName, (Min) b.constraintAnnotation, (Max) a.constraintAnnotation);
    }
    return null;
  }

  private static String minGreaterThanMaxMessage(String fieldName, Min min, Max max) {
    if (min.value() > max.value()) {
      return FIELD
          + fieldName
          + MIN
          + min.value()
          + ") is greater than @Max("
          + max.value()
          + "). Min value must be ≤ max value.";
    }
    return null;
  }

  private static String checkMinVersusSign(
      String fieldName, String typeA, String typeB, ValidationInfo a, ValidationInfo b) {
    if (typeA.equals("Min") && typeB.equals(POSITIVE)) {
      return minPositiveConflict(fieldName, (Min) a.constraintAnnotation);
    }
    if (typeA.equals(POSITIVE) && typeB.equals("Min")) {
      return minPositiveConflict(fieldName, (Min) b.constraintAnnotation);
    }
    if (typeA.equals("Min") && typeB.equals(NEGATIVE)) {
      return minNegativeConflict(fieldName, (Min) a.constraintAnnotation);
    }
    if (typeA.equals(NEGATIVE) && typeB.equals("Min")) {
      return minNegativeConflict(fieldName, (Min) b.constraintAnnotation);
    }
    return null;
  }

  private static String minPositiveConflict(String fieldName, Min min) {
    if (min.value() <= 0) {
      return FIELD
          + fieldName
          + MIN
          + min.value()
          + ") allows non-positive values, which contradicts @Positive (> 0).";
    }
    return null;
  }

  private static String minNegativeConflict(String fieldName, Min min) {
    if (min.value() >= 0) {
      return FIELD
          + fieldName
          + MIN
          + min.value()
          + ") requires non-negative values, which contradicts @Negative (< 0).";
    }
    return null;
  }

  private static String checkMaxVersusSign(
      String fieldName, String typeA, String typeB, ValidationInfo a, ValidationInfo b) {
    if (typeA.equals("Max") && typeB.equals(POSITIVE)) {
      return maxPositiveConflict(fieldName, (Max) a.constraintAnnotation);
    }
    if (typeA.equals(POSITIVE) && typeB.equals("Max")) {
      return maxPositiveConflict(fieldName, (Max) b.constraintAnnotation);
    }
    if (typeA.equals("Max") && typeB.equals(NEGATIVE)) {
      return maxNegativeConflict(fieldName, (Max) a.constraintAnnotation);
    }
    if (typeA.equals(NEGATIVE) && typeB.equals("Max")) {
      return maxNegativeConflict(fieldName, (Max) b.constraintAnnotation);
    }
    return null;
  }

  private static String maxPositiveConflict(String fieldName, Max max) {
    if (max.value() <= 0) {
      return FIELD
          + fieldName
          + MAX
          + max.value()
          + ") allows only non-positive values, which contradicts @Positive (> 0).";
    }
    return null;
  }

  private static String maxNegativeConflict(String fieldName, Max max) {
    if (max.value() >= 0) {
      return FIELD
          + fieldName
          + MAX
          + max.value()
          + ") allows non-negative values, which contradicts @Negative (< 0).";
    }
    return null;
  }

  private static String checkNotEmptyVersusSize(
      String fieldName, String typeA, String typeB, ValidationInfo a, ValidationInfo b) {
    if ((typeA.equals(NOT_EMPTY) && typeB.equals("Size"))
        || (typeA.equals("Size") && typeB.equals(NOT_EMPTY))) {
      Size size =
          typeA.equals("Size") ? (Size) a.constraintAnnotation : (Size) b.constraintAnnotation;
      if (size.max() == 0) {
        return FIELD
            + fieldName
            + "': @NotEmpty contradicts @Size(max=0). An element cannot be both not empty and have maximum size 0.";
      }
    }
    return null;
  }
}
