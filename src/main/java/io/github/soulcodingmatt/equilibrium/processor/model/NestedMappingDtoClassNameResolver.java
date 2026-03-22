package io.github.soulcodingmatt.equilibrium.processor.model;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Resolves the simple name of the DTO class referenced by {@link NestedMapping#dtoClass()},
 * including {@link MirroredTypeException} fallbacks.
 */
final class NestedMappingDtoClassNameResolver {

  private NestedMappingDtoClassNameResolver() {}

  static String resolve(NestedMapping mapping, Messager messager) {
    try {
      return mapping.dtoClass().getSimpleName();
    } catch (MirroredTypeException mte) {
      return resolveFromMirroredTypeException(mapping, mte, messager);
    }
  }

  private static String resolveFromMirroredTypeException(
      NestedMapping mapping, MirroredTypeException mte, Messager messager) {
    TypeMirror typeMirror = mte.getTypeMirror();
    String typeMirrorString = typeMirror.toString();

    if (typeMirror.getKind() == TypeKind.ERROR || typeMirrorString.contains("<any?>")) {
      return resolveErrorTypeMirror(mapping, typeMirrorString, messager);
    }

    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      TypeElement typeElement = (TypeElement) declaredType.asElement();
      return typeElement.getSimpleName().toString();
    }

    String fullName = typeMirror.toString();
    int lastDotIndex = fullName.lastIndexOf('.');
    return lastDotIndex > 0 ? fullName.substring(lastDotIndex + 1) : fullName;
  }

  private static String resolveErrorTypeMirror(
      NestedMapping mapping, String typeMirrorString, Messager messager) {
    if (typeMirrorString.contains(".")) {
      String simpleName = typeMirrorString.substring(typeMirrorString.lastIndexOf('.') + 1);
      if (!simpleName.contains("<") && !simpleName.contains(">") && !simpleName.isEmpty()) {
        return simpleName;
      }
    }
    String fromAnnotationString = tryParseSimpleNameFromNestedMappingToString(mapping);
    if (fromAnnotationString != null) {
      return fromAnnotationString;
    }

    messager.printMessage(
        Diagnostic.Kind.ERROR,
        "Cannot resolve DTO class in @NestedMapping annotation. "
            + "TypeMirror resolution failed: "
            + typeMirrorString
            + ". "
            + "Please ensure the referenced DTO class exists and is on the classpath.");
    throw new IllegalStateException(
        "Cannot resolve DTO class in @NestedMapping: " + typeMirrorString);
  }

  /**
   * Fallback when {@link MirroredTypeException} yields an error type: parse {@link
   * NestedMapping#toString()} for {@code dtoClass=SomeDto.class}. Uses index checks only (no
   * catch-all) so malformed strings fail the same way as "not found" and hit the error path below.
   */
  private static String tryParseSimpleNameFromNestedMappingToString(NestedMapping mapping) {
    String annotationString = mapping.toString();
    if (!annotationString.contains(DtoGenerator.DTO_CLASS)
        || !annotationString.contains(".class")) {
      return null;
    }
    int key = annotationString.indexOf(DtoGenerator.DTO_CLASS);
    if (key < 0) {
      return null;
    }
    int start = key + DtoGenerator.DTO_CLASS.length();
    int end = annotationString.indexOf(".class", start);
    if (end <= start) {
      return null;
    }
    String classReference = annotationString.substring(start, end);
    if (classReference.contains(".")) {
      classReference = classReference.substring(classReference.lastIndexOf('.') + 1);
    }
    return classReference.isEmpty() ? null : classReference;
  }
}
