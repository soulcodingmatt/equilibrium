package io.github.soulcodingmatt.equilibrium.processor.config;

import io.github.soulcodingmatt.equilibrium.processor.validation.analysis.ValidationUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Configuration class for the Equilibrium annotation processor. Handles global settings from
 * pom.xml and provides default values.
 *
 * <p>Supported compiler options (read from ProcessingEnvironment.getOptions()):
 *
 * <ul>
 *   <li>{@code equilibrium.dto.package} - Default package for generated DTOs
 *   <li>{@code equilibrium.dto.postfix} - Default postfix for generated DTOs (default: "Dto")
 *   <li>{@code equilibrium.record.package} - Default package for generated Records
 *   <li>{@code equilibrium.record.postfix} - Default postfix for generated Records (default:
 *       "Record")
 *   <li>{@code equilibrium.vo.package} - Default package for generated Value Objects
 *   <li>{@code equilibrium.vo.postfix} - Default postfix for generated Value Objects (default:
 *       "Vo")
 *   <li>{@code equilibrium.groupId} - Optional project groupId (inferred from pom.xml if not set)
 *   <li>{@code equilibrium.artifactId} - Optional project artifactId (inferred from pom.xml if not
 *       set)
 * </ul>
 */
public class EquilibriumConfig {
  private static final String PREFIX = "equilibrium.";

  private static final String DTO_PACKAGE = PREFIX + "dto.package";
  private static final String DTO_POSTFIX = PREFIX + "dto.postfix";
  private static final String RECORD_PACKAGE = PREFIX + "record.package";
  private static final String RECORD_POSTFIX = PREFIX + "record.postfix";
  private static final String VALUE_OBJECT_PACKAGE = PREFIX + "vo.package";
  private static final String VALUE_OBJECT_POSTFIX = PREFIX + "vo.postfix";

  private static final String GROUP_ID = PREFIX + "groupId";
  private static final String ARTIFACT_ID = PREFIX + "artifactId";

  public static final String RECORD = "Record";
  public static final String DTO = "DTO";
  public static final String VO = "VO";

  /** Lazy holder for pom.xml coordinates — initialized at most once per JVM. */
  private static final class PomCoordinatesHolder {
    static final Map.Entry<String, String> INSTANCE = inferFromPom().orElse(null);

    private static Optional<Map.Entry<String, String>> inferFromPom() {
      try {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path pomFile = projectRoot.resolve("pom.xml");

        if (Files.exists(pomFile)) {
          String content = Files.readString(pomFile);

          // Try to find project coordinates that are NOT inside a <parent> tag
          Pattern projectPattern =
              Pattern.compile(
                  "(?:(?!</parent>)(?!<parent>).)++<groupId>\\s*(.*?)\\s*</groupId>"
                      + "(?:(?!</parent>)(?!<parent>).)*+<artifactId>\\s*(.*?)\\s*</artifactId>",
                  Pattern.DOTALL);
          Matcher projectMatcher = projectPattern.matcher(content);

          if (projectMatcher.find()) {
            return Optional.of(
                Map.entry(projectMatcher.group(1).trim(), projectMatcher.group(2).trim()));
          }

          // Remove the parent section entirely and search again
          String contentWithoutParent = content.replaceAll("(?s)<parent>.*?</parent>", "");
          Pattern simplePattern =
              Pattern.compile(
                  "<groupId>\\s*(.*?)\\s*</groupId>.*?<artifactId>\\s*(.*?)\\s*</artifactId>",
                  Pattern.DOTALL);
          Matcher simpleMatcher = simplePattern.matcher(contentWithoutParent);

          if (simpleMatcher.find()) {
            return Optional.of(
                Map.entry(simpleMatcher.group(1).trim(), simpleMatcher.group(2).trim()));
          }

          // Fallback: use the original pattern if nothing else works
          Pattern fallbackPattern =
              Pattern.compile(
                  "<groupId>\\s*(.*?)\\s*</groupId>\\s*<artifactId>\\s*(.*?)\\s*</artifactId>",
                  Pattern.DOTALL);
          Matcher fallbackMatcher = fallbackPattern.matcher(content);
          if (fallbackMatcher.find()) {
            return Optional.of(
                Map.entry(fallbackMatcher.group(1).trim(), fallbackMatcher.group(2).trim()));
          }
        }
      } catch (IOException e) {
        // If we can't read the file, return empty
      }

      return Optional.empty();
    }
  }

  private final Map<String, String> options;
  private final String groupId;
  private final String artifactId;

  public EquilibriumConfig(ProcessingEnvironment processingEnv) {
    this.options = processingEnv.getOptions();

    // First try to get from equilibrium options
    String configuredGroupId = options.get(GROUP_ID);
    String configuredArtifactId = options.get(ARTIFACT_ID);

    if (configuredGroupId != null && configuredArtifactId != null) {
      this.groupId = sanitizePackageName(configuredGroupId);
      this.artifactId = sanitizePackageName(configuredArtifactId);
      return;
    }

    // Try to infer from pom.xml (cached via lazy holder)
    Map.Entry<String, String> coordinates = PomCoordinatesHolder.INSTANCE;
    if (coordinates != null) {
      this.groupId = sanitizePackageName(coordinates.getKey());
      this.artifactId = sanitizePackageName(coordinates.getValue());
      return;
    }

    // Fall back to defaults
    this.groupId = "io.github.soulcodingmatt";
    this.artifactId = "equilibrium";
  }

  /**
   * Sanitizes a string to be used as a package name. - Converts to lowercase - Removes dashes and
   * underscores - Ensures valid package name format
   *
   * @param input the input string to sanitize
   * @return the sanitized package name
   */
  private String sanitizePackageName(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }

    // Convert to lowercase and remove dashes/underscores
    String sanitized =
        input
            .toLowerCase()
            .replaceAll("[-_]", "")
            // Remove any consecutive dots
            .replaceAll("\\.+", ".")
            // Remove leading/trailing dots
            .replaceAll("(^\\.)|(\\.$)", "");

    // Split by dots and ensure each part starts with a letter
    String[] parts = sanitized.split("\\.");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      // Remove any non-alphanumeric characters
      part = part.replaceAll("[^a-z0-9]", "");
      // Ensure part starts with a letter
      if (!part.isEmpty() && !Character.isLetter(part.charAt(0))) {
        part = "p" + part;
      }
      if (!part.isEmpty()) {
        if (i > 0) {
          result.append(".");
        }
        result.append(part);
      }
    }

    return result.toString();
  }

  /**
   * Gets the global package for DTOs from the configuration.
   *
   * @return Optional containing the package name, or empty if not configured
   */
  public Optional<String> getDtoPackage() {
    String pkg = options.get(DTO_PACKAGE);
    return ValidationUtil.isValidPackageName(pkg) ? Optional.of(pkg) : Optional.empty();
  }

  /**
   * Gets the global postfix for DTOs from the configuration.
   *
   * @return the configured postfix or "Dto" as default
   */
  public String getDtoPostfix() {
    String postfix = options.get(DTO_POSTFIX);
    return ValidationUtil.isValidPostfix(postfix)
        ? postfix
        : ValidationUtil.getDefaultPostfix("DTO");
  }

  /**
   * Gets the global package for Records from the configuration.
   *
   * @return Optional containing the package name, or empty if not configured
   */
  public Optional<String> getRecordPackage() {
    String pkg = options.get(RECORD_PACKAGE);
    return ValidationUtil.isValidPackageName(pkg) ? Optional.of(pkg) : Optional.empty();
  }

  /**
   * Gets the global postfix for Records from the configuration.
   *
   * @return the configured postfix or "Record" as default
   */
  public String getRecordPostfix() {
    String postfix = options.get(RECORD_POSTFIX);
    return ValidationUtil.isValidPostfix(postfix)
        ? postfix
        : ValidationUtil.getDefaultPostfix(RECORD);
  }

  /**
   * Gets the global package for Value Objects from the configuration.
   *
   * @return Optional containing the package name, or empty if not configured
   */
  public Optional<String> getVoPackage() {
    String pkg = options.get(VALUE_OBJECT_PACKAGE);
    return ValidationUtil.isValidPackageName(pkg) ? Optional.of(pkg) : Optional.empty();
  }

  /**
   * Gets the global postfix for Value Objects from the configuration.
   *
   * @return the configured postfix or "Vo" as default
   */
  public String getVoPostfix() {
    String postfix = options.get(VALUE_OBJECT_POSTFIX);
    return ValidationUtil.isValidPostfix(postfix)
        ? postfix
        : ValidationUtil.getDefaultPostfix("VO");
  }

  /**
   * Validates that either a global package is configured or a specific package is provided in the
   * annotation.
   *
   * @param annotationPackage the package specified in the annotation
   * @param classType the type of class being generated (DTO, Record, or VO)
   * @return the package to use, or throws an exception if none is available
   */
  public String validateAndGetPackage(String annotationPackage, String classType) {
    String resolvedPackage;

    // First check if the annotation package is valid and non-empty
    if (!annotationPackage.isEmpty() && ValidationUtil.isValidPackageName(annotationPackage)) {
      resolvedPackage = annotationPackage;
    } else {
      // If annotation package is invalid or empty, try global package configuration
      Optional<String> packageOpt =
          switch (classType) {
            case DTO -> getDtoPackage();
            case RECORD -> getRecordPackage();
            case VO -> getVoPackage();
            default -> Optional.empty();
          };

      // Finally, fall back to the default package based on project coordinates
      resolvedPackage =
          packageOpt.orElseGet(
              () -> ValidationUtil.getDefaultPackageName(groupId, artifactId, classType));
    }

    // Validate the final resolved package
    if (!ValidationUtil.isValidPackageName(resolvedPackage)) {
      String errorMessage = ValidationUtil.getPackageValidationError(resolvedPackage);
      throw new IllegalArgumentException(
          "Invalid package name in @Generate" + classType + " annotation: " + errorMessage);
    }

    return resolvedPackage;
  }

  /**
   * Validates a postfix and returns either the valid postfix or the default one.
   *
   * @param annotationPostfix the postfix specified in the annotation
   * @param classType the type of class being generated (DTO, Record, or VO)
   * @return the postfix to use
   */
  public String validateAndGetPostfix(String annotationPostfix, String classType) {
    if (ValidationUtil.isValidPostfix(annotationPostfix)) {
      return annotationPostfix;
    }

    return switch (classType) {
      case DTO -> getDtoPostfix();
      case RECORD -> getRecordPostfix();
      case VO -> getVoPostfix();
      default -> throw new IllegalArgumentException("Invalid class type: " + classType);
    };
  }

  /**
   * Validates a field name.
   *
   * @param fieldName the field name to validate
   * @return true if the field name is valid, false otherwise
   */
  public boolean isValidFieldName(String fieldName) {
    return ValidationUtil.isValidFieldName(fieldName);
  }
}
