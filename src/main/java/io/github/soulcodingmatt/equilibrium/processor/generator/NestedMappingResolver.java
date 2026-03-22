package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generator.dto.DtoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.util.CustomObjectDetector;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/** Resolves nested mapping DTO names and imports, and transforms types accordingly. */
public final class NestedMappingResolver {

  private final TypeElement sourceType;
  private final Messager messager;

  public NestedMappingResolver(TypeElement sourceType, Messager messager) {
    this.sourceType = sourceType;
    this.messager = messager;
  }

  public String findDtoImportFromSourceClass(NestedMapping mapping) {
    String dtoSimpleName = getDtoClassSimpleName(mapping);

    String registeredDto = DtoGenerator.lookupGeneratedDto(dtoSimpleName);
    if (registeredDto != null) {
      return registeredDto;
    }

    String sourcePackage = sourceType.getQualifiedName().toString();
    int lastDot = sourcePackage.lastIndexOf('.');
    if (lastDot > 0) {
      sourcePackage = sourcePackage.substring(0, lastDot);
    }

    String[] commonDtoPackages = {
      sourcePackage + ".dto",
      sourcePackage.replace(".domain", ".dto"),
      sourcePackage.replace(".entity", ".dto"),
      sourcePackage.replace(".model", ".dto"),
      sourcePackage
    };

    for (String pkgName : commonDtoPackages) {
      String fullName = pkgName + "." + dtoSimpleName;
      if (pkgName.contains("dto")) {
        return fullName;
      }
    }

    messager.printMessage(
        Diagnostic.Kind.WARNING, "Could not determine DTO import for: " + dtoSimpleName);
    return null;
  }

  public String getTransformedFieldType(VariableElement field) {
    TypeMirror fieldType = field.asType();
    String originalType = fieldType.toString();

    NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
    if (nestedMapping != null) {
      return transformTypeWithMappingSimpleName(fieldType, nestedMapping);
    }
    return originalType;
  }

  private String transformTypeWithMappingSimpleName(TypeMirror fieldType, NestedMapping mapping) {
    String dtoClassSimpleName = getDtoClassSimpleName(mapping);
    TypeMirror elementType = CustomObjectDetector.getCollectionElementType(fieldType);
    if (elementType != null) {
      String originalType = fieldType.toString();
      String originalElementType = elementType.toString();
      return originalType.replace(originalElementType, dtoClassSimpleName);
    } else {
      return dtoClassSimpleName;
    }
  }

  private String getDtoClassSimpleName(NestedMapping mapping) {
    try {
      return mapping.dtoClass().getSimpleName();
    } catch (MirroredTypeException mte) {
      TypeMirror typeMirror = mte.getTypeMirror();
      String typeMirrorString = typeMirror.toString();

      if (typeMirror.getKind() == TypeKind.ERROR || typeMirrorString.contains("<any?>")) {
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

      if (typeMirror.getKind() == TypeKind.DECLARED) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        return typeElement.getSimpleName().toString();
      }

      String fullName = typeMirror.toString();
      int lastDotIndex = fullName.lastIndexOf('.');
      return lastDotIndex > 0 ? fullName.substring(lastDotIndex + 1) : fullName;
    }
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
