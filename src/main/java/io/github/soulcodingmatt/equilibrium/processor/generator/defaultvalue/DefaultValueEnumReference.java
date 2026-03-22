package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

/** Resolves enum constant references for {@code @DtoBuilderDefault} (typed and legacy). */
public final class DefaultValueEnumReference {

  private DefaultValueEnumReference() {}

  public static String processEnumValue(
      VariableElement field, String annotationValue, Messager messager) {
    String fieldName = field.getSimpleName().toString();
    DeclaredType declaredType = (DeclaredType) field.asType();
    TypeElement enumElement = (TypeElement) declaredType.asElement();
    String enumClassName = enumElement.getQualifiedName().toString();
    String enumSimpleName = enumElement.getSimpleName().toString();

    String constantName;
    String specifiedEnumName = null;
    if (annotationValue.contains(".")) {
      String[] parts = annotationValue.split("\\.");
      if (parts.length != 2) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "@DtoBuilderDefault: Invalid enum reference format '"
                + annotationValue
                + "'. Expected 'EnumName.CONSTANT' or 'CONSTANT' for field: "
                + fieldName,
            field);
        return null;
      }
      specifiedEnumName = parts[0];
      constantName = parts[1];
      if (!specifiedEnumName.equals(enumSimpleName) && !specifiedEnumName.equals(enumClassName)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "@DtoBuilderDefault: Enum reference '"
                + specifiedEnumName
                + "' does not match field type '"
                + enumClassName
                + "' for field: "
                + fieldName,
            field);
        return null;
      }
    } else {
      constantName = annotationValue;
    }

    boolean constantExists = false;
    for (var enclosedElement : enumElement.getEnclosedElements()) {
      if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT
          && enclosedElement.getSimpleName().toString().equals(constantName)) {
        constantExists = true;
        break;
      }
    }
    if (!constantExists) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          "@DtoBuilderDefault: Enum constant '"
              + constantName
              + "' does not exist in enum "
              + enumClassName
              + " for field: "
              + fieldName,
          field);
      return null;
    }

    return enumSimpleName + "." + constantName;
  }
}
