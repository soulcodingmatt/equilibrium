package io.github.soulcodingmatt.equilibrium.processor.generator.validationsupport;

import java.util.Set;

/** Merges imports derived from string-encoded validation annotations ({@code value} array). */
public final class ValidationStringAnnotationImports {

  private ValidationStringAnnotationImports() {}

  public static void mergeFromValueStrings(
      Set<String> validationImports, String[] validationStrings) {
    for (String validation : validationStrings) {
      if (!validation.trim().isEmpty()) {
        String annotationClass = ValidationCodegenStrings.extractAnnotationClass(validation);
        if (annotationClass != null) {
          validationImports.add(annotationClass);
        }
      }
    }
  }
}
