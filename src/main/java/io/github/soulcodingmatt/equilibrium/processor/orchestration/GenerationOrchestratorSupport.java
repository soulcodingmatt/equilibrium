package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import io.github.soulcodingmatt.equilibrium.processor.config.EquilibriumConfig;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/** Shared utilities for the DTO, Record, and VO generation orchestrators. */
final class GenerationOrchestratorSupport {

  /** Sentinel value indicating no explicit ID was set on a generation annotation. */
  static final int NO_EXPLICIT_ID = -1;

  private static final String DUPLICATE_ID_PREFIX = "Duplicate ID ";
  private static final String DUPLICATES_WILL_BE_IGNORED = "' - duplicates will be ignored";
  private static final String INVALID_FIELD_NAME_PREFIX = "Invalid field name in ignore list: '";
  private static final String WILL_BE_SKIPPED = "' - will be skipped";

  private GenerationOrchestratorSupport() {}

  /** Resolves the class name from an annotation's name attribute, falling back to postfix. */
  static String resolveClassName(
      TypeElement classElement, String annotationName, EquilibriumConfig config, String typeLabel) {
    if (!annotationName.isEmpty()) {
      return annotationName;
    }
    String postfix = config.validateAndGetPostfix("", typeLabel);
    return classElement.getSimpleName() + postfix;
  }

  /** Validates that no two targets share the same fully qualified name or the same explicit ID. */
  static boolean validateUniqueCombinations(
      TypeElement classElement,
      DiagnosticReporter reporter,
      String typeLabel,
      String generateAnnotationName,
      List<ResolvedTarget> targets) {
    Set<String> uniqueCombinations = new HashSet<>();
    Set<Integer> usedIds = new HashSet<>();

    for (ResolvedTarget target : targets) {
      String combination = target.packageName() + "." + target.className();

      if (!uniqueCombinations.add(combination)) {
        reporter.error(
            classElement,
            "Duplicate "
                + typeLabel
                + " configuration would generate the same class: "
                + combination);
        return false;
      }

      if (target.id() != NO_EXPLICIT_ID && !usedIds.add(target.id())) {
        reporter.error(
            classElement,
            "Duplicate "
                + typeLabel
                + " ID: "
                + target.id()
                + ". Each @"
                + generateAnnotationName
                + " annotation must have a unique ID.");
        return false;
      }
    }
    return true;
  }

  /** Collects the explicit IDs from a list of resolved targets. */
  static Set<Integer> collectExplicitIds(List<ResolvedTarget> targets) {
    return targets.stream()
        .map(ResolvedTarget::id)
        .filter(id -> id != NO_EXPLICIT_ID)
        .collect(Collectors.toSet());
  }

  /** Validates field names in the ignore list, returning only the valid ones. */
  static Set<String> collectIgnoredFields(
      String[] ignoreList,
      TypeElement classElement,
      EquilibriumConfig config,
      DiagnosticReporter reporter) {
    Set<String> ignoredFields = new HashSet<>();
    for (String fieldName : ignoreList) {
      if (config.isValidFieldName(fieldName)) {
        ignoredFields.add(fieldName);
      } else if (!fieldName.isEmpty()) {
        reporter.warning(classElement, INVALID_FIELD_NAME_PREFIX + fieldName + WILL_BE_SKIPPED);
      }
    }
    return ignoredFields;
  }

  /**
   * Validates IDs in ignore annotations: warns about duplicates and IDs that don't match any
   * generation annotation.
   *
   * @param idsExtractor returns the {@code ids()} array from the ignore annotation on a field, or
   *     {@code null} if the annotation is absent
   */
  static void validateIgnoreAnnotationIds(
      TypeElement classElement,
      Set<Integer> validIds,
      DiagnosticReporter reporter,
      String ignoreAnnotationName,
      String generateAnnotationName,
      Function<VariableElement, int[]> idsExtractor) {
    classElement.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.FIELD)
        .map(VariableElement.class::cast)
        .forEach(
            field -> {
              int[] ids = idsExtractor.apply(field);
              if (ids == null) {
                return;
              }
              Set<Integer> uniqueIds = new HashSet<>();
              for (int id : ids) {
                if (!uniqueIds.add(id)) {
                  reporter.warning(
                      field,
                      DUPLICATE_ID_PREFIX
                          + id
                          + " in @"
                          + ignoreAnnotationName
                          + " annotation for field '"
                          + field.getSimpleName()
                          + DUPLICATES_WILL_BE_IGNORED);
                } else if (!validIds.isEmpty() && !validIds.contains(id)) {
                  reporter.warning(
                      field,
                      "ID "
                          + id
                          + " in @"
                          + ignoreAnnotationName
                          + " annotation for field '"
                          + field.getSimpleName()
                          + "' does not correspond to any @"
                          + generateAnnotationName
                          + " annotation ID");
                }
              }
            });
  }

  /**
   * Runs a generation action and emits a success note or an error diagnostic.
   *
   * <p>This narrows the catch from {@code Exception} to {@code IOException}, which is the only
   * checked exception thrown by generators.
   */
  static void executeGeneration(
      TypeElement classElement,
      DiagnosticReporter reporter,
      String displayName,
      String fullClassName,
      GenerationAction action) {
    try {
      action.execute();
      reporter.note(classElement, "Generated " + displayName + " class: " + fullClassName);
    } catch (IOException e) {
      reporter.error(
          classElement,
          "Failed to generate "
              + displayName
              + ": "
              + e.getMessage()
              + " ("
              + e.getClass().getSimpleName()
              + ")");
    }
  }

  /** A generation action that may throw {@link IOException}. */
  @FunctionalInterface
  interface GenerationAction {
    void execute() throws IOException;
  }

  /** A resolved annotation target with its package, class name, and optional ID. */
  record ResolvedTarget(String packageName, String className, int id) {}
}
