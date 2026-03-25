package io.github.soulcodingmatt.equilibrium.processor.model;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
    String dtoSimpleName = NestedMappingDtoClassNameResolver.resolve(mapping, messager);

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
    String dtoClassSimpleName = NestedMappingDtoClassNameResolver.resolve(mapping, messager);
    TypeMirror elementType = CustomObjectDetector.getCollectionElementType(fieldType);
    if (elementType != null) {
      String originalType = fieldType.toString();
      String originalElementType = elementType.toString();
      return originalType.replace(originalElementType, dtoClassSimpleName);
    } else {
      return dtoClassSimpleName;
    }
  }
}
