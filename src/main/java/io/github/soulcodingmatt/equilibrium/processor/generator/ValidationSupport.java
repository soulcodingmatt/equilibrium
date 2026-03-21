package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.annotations.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.annotations.dto.validation.*;
import io.github.soulcodingmatt.equilibrium.annotations.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.annotations.vo.ValidateVo;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Utility methods for validation-related import collection and annotation emission. */
public final class ValidationSupport {

  private ValidationSupport() {
    throw new AssertionError("You should not be here!");
  }

  public static Set<String> collectValidationImports(List<VariableElement> fields, int dtoId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateDto[] validateAnnotations = field.getAnnotationsByType(ValidateDto.class);
      for (ValidateDto validateAnnotation : validateAnnotations) {
        if (shouldApplyValidation(validateAnnotation, dtoId)) {
          addTypeSafeValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  public static boolean shouldApplyValidation(ValidateDto validateAnnotation, int dtoId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == dtoId) {
        return true;
      }
    }
    return false;
  }

  public static void writeTypeSafeValidations(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write("    @NotNull");
      if (!notNull.message().equals("must not be null")) {
        writer.write("(message = \"" + escapeQuotes(notNull.message()) + "\")");
      }
      writer.write("\n");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write("    @NotBlank");
      if (!notBlank.message().equals("must not be blank")) {
        writer.write("(message = \"" + escapeQuotes(notBlank.message()) + "\")");
      }
      writer.write("\n");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      writer.write("    @Size");
      List<String> params = new ArrayList<>();
      if (size.min() != -1 && size.min() != 0) {
        params.add("min = " + size.min());
      }
      if (size.max() != -1 && size.max() != Integer.MAX_VALUE) {
        params.add("max = " + size.max());
      }
      if (!size.message().equals("")
          && !size.message().equals("size must be between {min} and {max}")) {
        params.add("message = \"" + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write("    @Min");
      List<String> params = new ArrayList<>();
      params.add("value = " + min.value());
      if (!min.message().equals("")
          && !min.message().equals("must be greater than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write("    @Max");
      List<String> params = new ArrayList<>();
      params.add("value = " + max.value());
      if (!max.message().equals("")
          && !max.message().equals("must be less than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write("    @Email");
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(".*")) {
        params.add("regexp = \"" + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("")
          && !email.message().equals("must be a well-formed email address")) {
        params.add("message = \"" + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write("    @Pattern");
      List<String> params = new ArrayList<>();
      params.add("regexp = \"" + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals("must match \"{regexp}\"")) {
        params.add("message = \"" + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write("    @NotEmpty");
      if (!notEmpty.message().equals("must not be empty")) {
        writer.write("(message = \"" + escapeQuotes(notEmpty.message()) + "\")");
      }
      writer.write("\n");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write("    @Positive");
      if (!positive.message().equals("must be greater than 0")) {
        writer.write("(message = \"" + escapeQuotes(positive.message()) + "\")");
      }
      writer.write("\n");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write("    @PositiveOrZero");
      if (!positiveOrZero.message().equals("must be greater than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(positiveOrZero.message()) + "\")");
      }
      writer.write("\n");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write("    @Negative");
      if (!negative.message().equals("must be less than 0")) {
        writer.write("(message = \"" + escapeQuotes(negative.message()) + "\")");
      }
      writer.write("\n");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write("    @NegativeOrZero");
      if (!negativeOrZero.message().equals("must be less than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(negativeOrZero.message()) + "\")");
      }
      writer.write("\n");
    }

    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      writer.write("    @Digits");
      List<String> params = new ArrayList<>();
      if (digits.integer() != -1) {
        params.add("integer = " + digits.integer());
      }
      if (digits.fraction() != -1) {
        params.add("fraction = " + digits.fraction());
      }
      if (!digits.message().equals("")
          && !digits
              .message()
              .equals(
                  "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)")) {
        params.add("message = \"" + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write("    @Past");
      if (!past.message().equals("must be a date in the past")) {
        writer.write("(message = \"" + escapeQuotes(past.message()) + "\")");
      }
      writer.write("\n");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write("    @Future");
      if (!future.message().equals("must be a date in the future")) {
        writer.write("(message = \"" + escapeQuotes(future.message()) + "\")");
      }
      writer.write("\n");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write("    @PastOrPresent");
      if (!pastOrPresent.message().equals("must be a date in the past or in the present")) {
        writer.write("(message = \"" + escapeQuotes(pastOrPresent.message()) + "\")");
      }
      writer.write("\n");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write("    @FutureOrPresent");
      if (!futureOrPresent.message().equals("must be a date in the present or in the future")) {
        writer.write("(message = \"" + escapeQuotes(futureOrPresent.message()) + "\")");
      }
      writer.write("\n");
    }
  }

  private static void addTypeSafeValidationImports(
      Set<String> validationImports, ValidateDto validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotNull");
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotBlank");
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotEmpty");
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Positive");
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PositiveOrZero");
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Negative");
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NegativeOrZero");
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Past");
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Future");
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PastOrPresent");
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.FutureOrPresent");
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Email");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add("jakarta.validation.constraints.Size");
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add("jakarta.validation.constraints.Min");
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add("jakarta.validation.constraints.Max");
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add("jakarta.validation.constraints.Pattern");
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add("jakarta.validation.constraints.Digits");
    }
  }

  private static String extractAnnotationClass(String annotationString) {
    String trimmed = annotationString.trim();
    if (!trimmed.startsWith("@")) {
      return null;
    }
    String withoutAt = trimmed.substring(1);
    int parenIndex = withoutAt.indexOf('(');
    String annotationName = parenIndex > 0 ? withoutAt.substring(0, parenIndex) : withoutAt;
    return getValidationAnnotationImport(annotationName);
  }

  private static String getValidationAnnotationImport(String annotationName) {
    return switch (annotationName) {
      case "NotNull" -> "jakarta.validation.constraints.NotNull";
      case "NotEmpty" -> "jakarta.validation.constraints.NotEmpty";
      case "NotBlank" -> "jakarta.validation.constraints.NotBlank";
      case "Size" -> "jakarta.validation.constraints.Size";
      case "Min" -> "jakarta.validation.constraints.Min";
      case "Max" -> "jakarta.validation.constraints.Max";
      case "DecimalMin" -> "jakarta.validation.constraints.DecimalMin";
      case "DecimalMax" -> "jakarta.validation.constraints.DecimalMax";
      case "Positive" -> "jakarta.validation.constraints.Positive";
      case "PositiveOrZero" -> "jakarta.validation.constraints.PositiveOrZero";
      case "Negative" -> "jakarta.validation.constraints.Negative";
      case "NegativeOrZero" -> "jakarta.validation.constraints.NegativeOrZero";
      case "Email" -> "jakarta.validation.constraints.Email";
      case "Pattern" -> "jakarta.validation.constraints.Pattern";
      case "Digits" -> "jakarta.validation.constraints.Digits";
      case "Future" -> "jakarta.validation.constraints.Future";
      case "FutureOrPresent" -> "jakarta.validation.constraints.FutureOrPresent";
      case "Past" -> "jakarta.validation.constraints.Past";
      case "PastOrPresent" -> "jakarta.validation.constraints.PastOrPresent";
      case "AssertTrue" -> "jakarta.validation.constraints.AssertTrue";
      case "AssertFalse" -> "jakarta.validation.constraints.AssertFalse";
      case "Valid" -> "jakarta.validation.Valid";
      default -> null;
    };
  }

  private static String escapeQuotes(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * Collects validation imports for Record components based on ValidateRecord annotations.
   *
   * @param fields the fields to check for ValidateRecord annotations
   * @param recordId the ID of the record being generated
   * @return set of validation import statements
   */
  public static Set<String> collectRecordValidationImports(
      List<VariableElement> fields, int recordId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateRecord[] validateAnnotations = field.getAnnotationsByType(ValidateRecord.class);
      for (ValidateRecord validateAnnotation : validateAnnotations) {
        if (shouldApplyRecordValidation(validateAnnotation, recordId)) {
          addTypeSafeRecordValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  /**
   * Checks if a ValidateRecord annotation should be applied for the given record ID.
   *
   * @param validateAnnotation the ValidateRecord annotation
   * @param recordId the ID of the record being generated
   * @return true if validation should be applied, false otherwise
   */
  public static boolean shouldApplyRecordValidation(
      ValidateRecord validateAnnotation, int recordId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == recordId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes type-safe validation annotations for a Record component.
   *
   * @param writer the writer to write to
   * @param validateAnnotation the ValidateRecord annotation
   * @throws IOException if writing fails
   */
  public static void writeTypeSafeRecordValidations(
      Writer writer, ValidateRecord validateAnnotation) throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write("    @NotNull");
      if (!notNull.message().equals("must not be null")) {
        writer.write("(message = \"" + escapeQuotes(notNull.message()) + "\")");
      }
      writer.write(" ");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write("@NotBlank");
      if (!notBlank.message().equals("must not be blank")) {
        writer.write("(message = \"" + escapeQuotes(notBlank.message()) + "\")");
      }
      writer.write(" ");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      writer.write("@Size");
      List<String> params = new ArrayList<>();
      if (size.min() != -1 && size.min() != 0) {
        params.add("min = " + size.min());
      }
      if (size.max() != -1 && size.max() != Integer.MAX_VALUE) {
        params.add("max = " + size.max());
      }
      if (!size.message().equals("")
          && !size.message().equals("size must be between {min} and {max}")) {
        params.add("message = \"" + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write("@Min");
      List<String> params = new ArrayList<>();
      params.add("value = " + min.value());
      if (!min.message().equals("")
          && !min.message().equals("must be greater than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write("@Max");
      List<String> params = new ArrayList<>();
      params.add("value = " + max.value());
      if (!max.message().equals("")
          && !max.message().equals("must be less than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write("@Email");
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(".*")) {
        params.add("regexp = \"" + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("")
          && !email.message().equals("must be a well-formed email address")) {
        params.add("message = \"" + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write("@Pattern");
      List<String> params = new ArrayList<>();
      params.add("regexp = \"" + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals("must match \"{regexp}\"")) {
        params.add("message = \"" + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write("@NotEmpty");
      if (!notEmpty.message().equals("must not be empty")) {
        writer.write("(message = \"" + escapeQuotes(notEmpty.message()) + "\")");
      }
      writer.write(" ");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write("@Positive");
      if (!positive.message().equals("must be greater than 0")) {
        writer.write("(message = \"" + escapeQuotes(positive.message()) + "\")");
      }
      writer.write(" ");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write("@PositiveOrZero");
      if (!positiveOrZero.message().equals("must be greater than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(positiveOrZero.message()) + "\")");
      }
      writer.write(" ");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write("@Negative");
      if (!negative.message().equals("must be less than 0")) {
        writer.write("(message = \"" + escapeQuotes(negative.message()) + "\")");
      }
      writer.write(" ");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write("@NegativeOrZero");
      if (!negativeOrZero.message().equals("must be less than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(negativeOrZero.message()) + "\")");
      }
      writer.write(" ");
    }

    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      writer.write("@Digits");
      List<String> params = new ArrayList<>();
      if (digits.integer() != -1) {
        params.add("integer = " + digits.integer());
      }
      if (digits.fraction() != -1) {
        params.add("fraction = " + digits.fraction());
      }
      if (!digits.message().equals("")
          && !digits
              .message()
              .equals(
                  "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)")) {
        params.add("message = \"" + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write("@Past");
      if (!past.message().equals("must be a date in the past")) {
        writer.write("(message = \"" + escapeQuotes(past.message()) + "\")");
      }
      writer.write(" ");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write("@Future");
      if (!future.message().equals("must be a date in the future")) {
        writer.write("(message = \"" + escapeQuotes(future.message()) + "\")");
      }
      writer.write(" ");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write("@PastOrPresent");
      if (!pastOrPresent.message().equals("must be a date in the past or in the present")) {
        writer.write("(message = \"" + escapeQuotes(pastOrPresent.message()) + "\")");
      }
      writer.write(" ");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write("@FutureOrPresent");
      if (!futureOrPresent.message().equals("must be a date in the present or in the future")) {
        writer.write("(message = \"" + escapeQuotes(futureOrPresent.message()) + "\")");
      }
      writer.write(" ");
    }
  }

  /** Adds type-safe validation imports for Record validations. */
  private static void addTypeSafeRecordValidationImports(
      Set<String> validationImports, ValidateRecord validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotNull");
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotBlank");
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotEmpty");
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Positive");
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PositiveOrZero");
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Negative");
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NegativeOrZero");
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Past");
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Future");
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PastOrPresent");
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.FutureOrPresent");
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Email");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add("jakarta.validation.constraints.Size");
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add("jakarta.validation.constraints.Min");
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add("jakarta.validation.constraints.Max");
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add("jakarta.validation.constraints.Pattern");
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add("jakarta.validation.constraints.Digits");
    }
  }

  /**
   * Collects validation imports for VO fields based on ValidateVo annotations.
   *
   * @param fields the fields to check for ValidateVo annotations
   * @param voId the ID of the VO being generated
   * @return set of validation import statements
   */
  public static Set<String> collectVoValidationImports(List<VariableElement> fields, int voId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateVo[] validateAnnotations = field.getAnnotationsByType(ValidateVo.class);
      for (ValidateVo validateAnnotation : validateAnnotations) {
        if (shouldApplyVoValidation(validateAnnotation, voId)) {
          addTypeSafeVoValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  /**
   * Checks if a ValidateVo annotation should be applied for the given VO ID.
   *
   * @param validateAnnotation the ValidateVo annotation
   * @param voId the ID of the VO being generated
   * @return true if validation should be applied, false otherwise
   */
  public static boolean shouldApplyVoValidation(ValidateVo validateAnnotation, int voId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == voId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes type-safe validation annotations for a VO field.
   *
   * @param writer the writer to write to
   * @param validateAnnotation the ValidateVo annotation
   * @throws IOException if writing fails
   */
  public static void writeTypeSafeVoValidations(Writer writer, ValidateVo validateAnnotation)
      throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write("    @NotNull");
      if (!notNull.message().equals("must not be null")) {
        writer.write("(message = \"" + escapeQuotes(notNull.message()) + "\")");
      }
      writer.write("\n");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write("    @NotBlank");
      if (!notBlank.message().equals("must not be blank")) {
        writer.write("(message = \"" + escapeQuotes(notBlank.message()) + "\")");
      }
      writer.write("\n");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      writer.write("    @Size");
      List<String> params = new ArrayList<>();
      if (size.min() != -1 && size.min() != 0) {
        params.add("min = " + size.min());
      }
      if (size.max() != -1 && size.max() != Integer.MAX_VALUE) {
        params.add("max = " + size.max());
      }
      if (!size.message().equals("")
          && !size.message().equals("size must be between {min} and {max}")) {
        params.add("message = \"" + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write("    @Min");
      List<String> params = new ArrayList<>();
      params.add("value = " + min.value());
      if (!min.message().equals("")
          && !min.message().equals("must be greater than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write("    @Max");
      List<String> params = new ArrayList<>();
      params.add("value = " + max.value());
      if (!max.message().equals("")
          && !max.message().equals("must be less than or equal to {value}")) {
        params.add("message = \"" + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write("    @Email");
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(".*")) {
        params.add("regexp = \"" + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("")
          && !email.message().equals("must be a well-formed email address")) {
        params.add("message = \"" + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write("    @Pattern");
      List<String> params = new ArrayList<>();
      params.add("regexp = \"" + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals("must match \"{regexp}\"")) {
        params.add("message = \"" + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write("    @NotEmpty");
      if (!notEmpty.message().equals("must not be empty")) {
        writer.write("(message = \"" + escapeQuotes(notEmpty.message()) + "\")");
      }
      writer.write("\n");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write("    @Positive");
      if (!positive.message().equals("must be greater than 0")) {
        writer.write("(message = \"" + escapeQuotes(positive.message()) + "\")");
      }
      writer.write("\n");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write("    @PositiveOrZero");
      if (!positiveOrZero.message().equals("must be greater than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(positiveOrZero.message()) + "\")");
      }
      writer.write("\n");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write("    @Negative");
      if (!negative.message().equals("must be less than 0")) {
        writer.write("(message = \"" + escapeQuotes(negative.message()) + "\")");
      }
      writer.write("\n");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write("    @NegativeOrZero");
      if (!negativeOrZero.message().equals("must be less than or equal to 0")) {
        writer.write("(message = \"" + escapeQuotes(negativeOrZero.message()) + "\")");
      }
      writer.write("\n");
    }

    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      writer.write("    @Digits");
      List<String> params = new ArrayList<>();
      if (digits.integer() != -1) {
        params.add("integer = " + digits.integer());
      }
      if (digits.fraction() != -1) {
        params.add("fraction = " + digits.fraction());
      }
      if (!digits.message().equals("")
          && !digits
              .message()
              .equals(
                  "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)")) {
        params.add("message = \"" + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write("    @Past");
      if (!past.message().equals("must be a date in the past")) {
        writer.write("(message = \"" + escapeQuotes(past.message()) + "\")");
      }
      writer.write("\n");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write("    @Future");
      if (!future.message().equals("must be a date in the future")) {
        writer.write("(message = \"" + escapeQuotes(future.message()) + "\")");
      }
      writer.write("\n");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write("    @PastOrPresent");
      if (!pastOrPresent.message().equals("must be a date in the past or in the present")) {
        writer.write("(message = \"" + escapeQuotes(pastOrPresent.message()) + "\")");
      }
      writer.write("\n");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write("    @FutureOrPresent");
      if (!futureOrPresent.message().equals("must be a date in the present or in the future")) {
        writer.write("(message = \"" + escapeQuotes(futureOrPresent.message()) + "\")");
      }
      writer.write("\n");
    }
  }

  /** Adds type-safe validation imports for VO validations. */
  private static void addTypeSafeVoValidationImports(
      Set<String> validationImports, ValidateVo validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotNull");
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotBlank");
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NotEmpty");
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Positive");
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PositiveOrZero");
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Negative");
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.NegativeOrZero");
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Past");
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Future");
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.PastOrPresent");
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.FutureOrPresent");
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add("jakarta.validation.constraints.Email");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add("jakarta.validation.constraints.Size");
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add("jakarta.validation.constraints.Min");
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add("jakarta.validation.constraints.Max");
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add("jakarta.validation.constraints.Pattern");
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add("jakarta.validation.constraints.Digits");
    }
  }
}
