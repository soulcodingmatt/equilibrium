package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.processor.util.CustomObjectDetector;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public final class CustomObjectWarning {
  private final Messager messager;
  private final TypeElement classElement;
  private final String dtoClassName;

  public CustomObjectWarning(Messager messager, TypeElement classElement, String dtoClassName) {
    this.messager = messager;
    this.classElement = classElement;
    this.dtoClassName = dtoClassName;
  }

  public void check(VariableElement field, TypeMirror fieldType) {
    String fieldName = field.getSimpleName().toString();

    if (CustomObjectDetector.isCustomObject(fieldType)) {
      String customTypeName = fieldType.toString();
      String suggestedDtoName = suggestDtoName(customTypeName);
      String message =
          String.format(
              "[%s] Field '%s' uses custom type '%s' without DTO mapping.%n  Consider: @NestedMapping(dtoClass = %s.class)%n  Location: %s.%s",
              dtoClassName,
              fieldName,
              customTypeName,
              suggestedDtoName,
              classElement.getQualifiedName(),
              fieldName);
      messager.printMessage(Diagnostic.Kind.WARNING, message, field);
    }

    if (CustomObjectDetector.isCustomObjectCollection(fieldType)) {
      TypeMirror elementType = CustomObjectDetector.getCollectionElementType(fieldType);
      if (elementType != null) {
        String customTypeName = elementType.toString();
        String suggestedDtoName = suggestDtoName(customTypeName);
        String message =
            String.format(
                "[%s] Field '%s' uses collection of custom type '%s' without DTO mapping.%n  Consider: @NestedMapping(dtoClass = %s.class)%n  Location: %s.%s",
                dtoClassName,
                fieldName,
                customTypeName,
                suggestedDtoName,
                classElement.getQualifiedName(),
                fieldName);
        messager.printMessage(Diagnostic.Kind.WARNING, message, field);
      }
    }
  }

  private String suggestDtoName(String customTypeName) {
    String simpleClassName = customTypeName;
    int lastDotIndex = customTypeName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      simpleClassName = customTypeName.substring(lastDotIndex + 1);
    }
    return simpleClassName + "Dto";
  }
}
