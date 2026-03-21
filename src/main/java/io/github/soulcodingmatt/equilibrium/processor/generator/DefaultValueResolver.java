package io.github.soulcodingmatt.equilibrium.processor.generator;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import javax.lang.model.element.VariableElement;

public final class DefaultValueResolver {

  private DefaultValueResolver() {
    throw new AssertionError("You should not be here!");
  }

  public static String getTypeSpecificValue(
      javax.lang.model.element.VariableElement field,
      DtoBuilderDefault builderDefault,
      javax.annotation.processing.Messager messager) {
    String fieldType = field.asType().toString();

    if (isPrimitiveType(fieldType) || isWrapperType(fieldType)) {
      boolean isIntegerType =
          fieldType.equals(INTEGER_STRING)
              || fieldType.equals(JAVA_LANG_INTEGER)
              || fieldType.equals(INTEGER);
      if (isIntegerType && builderDefault.intValue() != Integer.MIN_VALUE) {
        return String.valueOf(builderDefault.intValue());
      }

      boolean isLongType =
          fieldType.equals(LONG_STRING)
              || fieldType.equals(JAVA_LANG_LONG)
              || fieldType.equals(LONG);
      if (isLongType && builderDefault.longValue() != Long.MIN_VALUE) {
        return builderDefault.longValue() + "L";
      }

      boolean isShortType =
          fieldType.equals(SHORT_STRING)
              || fieldType.equals(JAVA_LANG_SHORT)
              || fieldType.equals(SHORT);
      if (isShortType && builderDefault.shortValue() != Short.MIN_VALUE) {
        return String.valueOf(builderDefault.shortValue());
      }

      boolean isByteType =
          fieldType.equals(BYTE_STRING)
              || fieldType.equals(JAVA_LANG_BYTE)
              || fieldType.equals(BYTE);
      if (isByteType && builderDefault.byteValue() != Byte.MIN_VALUE) {
        return String.valueOf(builderDefault.byteValue());
      }

      boolean isFloatType =
          fieldType.equals(FLOAT_STRING)
              || fieldType.equals(JAVA_LANG_FLOAT)
              || fieldType.equals(FLOAT);
      if (isFloatType && builderDefault.floatValue() != Float.MIN_VALUE) {
        return builderDefault.floatValue() + "f";
      }

      boolean isDoubleType =
          fieldType.equals(DOUBLE_STRING)
              || fieldType.equals(JAVA_LANG_DOUBLE)
              || fieldType.equals(DOUBLE);
      if (isDoubleType && builderDefault.doubleValue() != Double.MIN_VALUE) {
        return String.valueOf(builderDefault.doubleValue());
      }

      boolean isBooleanType =
          fieldType.equals(BOOLEAN_STRING)
              || fieldType.equals(JAVA_LANG_BOOLEAN)
              || fieldType.equals(BOOLEAN);
      if (isBooleanType) {
        if (builderDefault.booleanValue()) {
          return "true";
        }
        return null;
      }

      boolean isCharacterType =
          fieldType.equals("char")
              || fieldType.equals(JAVA_LANG_CHARACTER)
              || fieldType.equals(CHARACTER);
      if (isCharacterType && builderDefault.charValue() != '\0') {
        return "'" + builderDefault.charValue() + "'";
      }
    }

    if (isStringType(fieldType) && !builderDefault.stringValue().isEmpty()) {
      return processStringValue(builderDefault.stringValue());
    }

    if (isEnumType(field) && !builderDefault.enumValue().isEmpty()) {
      return processEnumValue(field, builderDefault.enumValue(), messager);
    }

    return null;
  }

  public static boolean hasAnyOtherParameterSet(DtoBuilderDefault builderDefault) {
    return !builderDefault.stringValue().isEmpty()
        || builderDefault.intValue() != Integer.MIN_VALUE
        || builderDefault.longValue() != Long.MIN_VALUE
        || builderDefault.shortValue() != Short.MIN_VALUE
        || builderDefault.byteValue() != Byte.MIN_VALUE
        || builderDefault.floatValue() != Float.MIN_VALUE
        || builderDefault.doubleValue() != Double.MIN_VALUE
        || builderDefault.booleanValue()
        || builderDefault.charValue() != '\0'
        || !builderDefault.enumValue().isEmpty()
        || !builderDefault.value().isEmpty();
  }

  public static String processAnnotationValue(
      javax.lang.model.element.VariableElement field,
      String fieldType,
      String annotationValue,
      javax.annotation.processing.Messager messager) {
    if (isEnumType(field)) {
      return processEnumValue(field, annotationValue, messager);
    }
    if (isStringType(fieldType)) {
      return processStringValue(annotationValue);
    }
    if (isPrimitiveType(fieldType)) {
      return processPrimitiveValue(field, fieldType, annotationValue, messager);
    }
    if (isWrapperType(fieldType)) {
      return processWrapperValue(field, fieldType, annotationValue, messager);
    }
    return annotationValue;
  }

  public static String processStringValue(String annotationValue) {
    if (annotationValue.startsWith("\"") && annotationValue.endsWith("\"")) {
      return annotationValue;
    }
    if (annotationValue.startsWith("\\\"") && annotationValue.endsWith("\\\"")) {
      return annotationValue;
    }
    return "\"" + escapeQuotes(annotationValue) + "\"";
  }

  public static String processPrimitiveValue(
      javax.lang.model.element.VariableElement field,
      String fieldType,
      String annotationValue,
      javax.annotation.processing.Messager messager) {
    String fieldName = field.getSimpleName().toString();
    try {
      switch (fieldType) {
        case INTEGER_STRING -> {
          Integer.parseInt(annotationValue.trim());
          return annotationValue.trim();
        }
        case LONG_STRING -> {
          Long.parseLong(annotationValue.trim());
          return annotationValue.trim();
        }
        case SHORT_STRING -> {
          Short.parseShort(annotationValue.trim());
          return annotationValue.trim();
        }
        case BYTE_STRING -> {
          Byte.parseByte(annotationValue.trim());
          return annotationValue.trim();
        }
        case FLOAT_STRING -> {
          Float.parseFloat(annotationValue.trim());
          return annotationValue.trim();
        }
        case DOUBLE_STRING -> {
          Double.parseDouble(annotationValue.trim());
          return annotationValue.trim();
        }
        case BOOLEAN_STRING -> {
          String trimmed = annotationValue.trim();
          if (!trimmed.equals("true") && !trimmed.equals("false")) {
            throw new NumberFormatException("Invalid boolean value");
          }
          return trimmed;
        }
        case "char" -> {
          if (annotationValue.length() == 1) {
            return "'" + annotationValue + "'";
          } else if (annotationValue.startsWith("'") && annotationValue.endsWith("'")) {
            return annotationValue;
          } else {
            throw new NumberFormatException("Invalid char value");
          }
        }
        default -> {
          messager.printMessage(
              javax.tools.Diagnostic.Kind.WARNING,
              "@DtoBuilderDefault: Unsupported primitive type '"
                  + fieldType
                  + "' for field: "
                  + fieldName
                  + ". Using value as-is.",
              field);
          return annotationValue;
        }
      }
    } catch (NumberFormatException e) {
      messager.printMessage(
          javax.tools.Diagnostic.Kind.ERROR,
          "@DtoBuilderDefault: Invalid value '"
              + annotationValue
              + "' for primitive type '"
              + fieldType
              + "' field: "
              + fieldName,
          field);
      return null;
    }
  }

  public static String processWrapperValue(
      javax.lang.model.element.VariableElement field,
      String fieldType,
      String annotationValue,
      javax.annotation.processing.Messager messager) {
    String fieldName = field.getSimpleName().toString();
    try {
      switch (fieldType) {
        case JAVA_LANG_INTEGER, INTEGER -> {
          Integer.parseInt(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_LONG, LONG -> {
          Long.parseLong(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_SHORT, SHORT -> {
          Short.parseShort(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_BYTE, BYTE -> {
          Byte.parseByte(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_FLOAT, FLOAT -> {
          Float.parseFloat(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_DOUBLE, DOUBLE -> {
          Double.parseDouble(annotationValue.trim());
          return annotationValue.trim();
        }
        case JAVA_LANG_BOOLEAN, BOOLEAN -> {
          String trimmed = annotationValue.trim();
          if (!trimmed.equals("true") && !trimmed.equals("false")) {
            throw new NumberFormatException("Invalid boolean value");
          }
          return trimmed;
        }
        case JAVA_LANG_CHARACTER, CHARACTER -> {
          if (annotationValue.length() == 1) {
            return "'" + annotationValue + "'";
          } else if (annotationValue.startsWith("'") && annotationValue.endsWith("'")) {
            return annotationValue;
          } else {
            throw new NumberFormatException("Invalid char value");
          }
        }
        default -> {
          return annotationValue;
        }
      }
    } catch (NumberFormatException e) {
      messager.printMessage(
          javax.tools.Diagnostic.Kind.ERROR,
          "@DtoBuilderDefault: Invalid value '"
              + annotationValue
              + "' for wrapper type '"
              + fieldType
              + "' field: "
              + fieldName,
          field);
      return null;
    }
  }

  public static String processEnumValue(
      javax.lang.model.element.VariableElement field,
      String annotationValue,
      javax.annotation.processing.Messager messager) {
    String fieldName = field.getSimpleName().toString();
    javax.lang.model.type.DeclaredType declaredType =
        (javax.lang.model.type.DeclaredType) field.asType();
    javax.lang.model.element.TypeElement enumElement =
        (javax.lang.model.element.TypeElement) declaredType.asElement();
    String enumClassName = enumElement.getQualifiedName().toString();
    String enumSimpleName = enumElement.getSimpleName().toString();

    String constantName;
    String specifiedEnumName = null;
    if (annotationValue.contains(".")) {
      String[] parts = annotationValue.split("\\.");
      if (parts.length != 2) {
        messager.printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
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
            javax.tools.Diagnostic.Kind.ERROR,
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
    for (javax.lang.model.element.Element enclosedElement : enumElement.getEnclosedElements()) {
      if (enclosedElement.getKind() == javax.lang.model.element.ElementKind.ENUM_CONSTANT
          && enclosedElement.getSimpleName().toString().equals(constantName)) {
        constantExists = true;
        break;
      }
    }
    if (!constantExists) {
      messager.printMessage(
          javax.tools.Diagnostic.Kind.ERROR,
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

  private static boolean isEnumType(javax.lang.model.element.VariableElement field) {
    return field.asType().getKind() == javax.lang.model.type.TypeKind.DECLARED
        && ((javax.lang.model.type.DeclaredType) field.asType()).asElement().getKind()
            == javax.lang.model.element.ElementKind.ENUM;
  }

  private static String escapeQuotes(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  public static boolean isCollectionType(String fieldType) {
    String base = GeneratorUtility.extractBaseType(fieldType);
    return base.equals(JAVA_UTIL_LIST)
        || base.equals(JAVA_UTIL_SET)
        || base.equals(JAVA_UTIL_MAP)
        || base.equals("List")
        || base.equals("Set")
        || base.equals("Map");
  }

  public static boolean isOptionalType(String fieldType) {
    String base = GeneratorUtility.extractBaseType(fieldType);
    return base.equals(JAVA_UTIL_OPTIONAL) || base.equals("Optional");
  }

  public static boolean isStringType(String fieldType) {
    String base = GeneratorUtility.extractBaseType(fieldType);
    return base.equals("java.lang.String") || base.equals("String");
  }

  public static boolean isPrimitiveType(String fieldType) {
    return java.util.Arrays.asList(
            INTEGER_STRING,
            LONG_STRING,
            SHORT_STRING,
            BYTE_STRING,
            FLOAT_STRING,
            DOUBLE_STRING,
            BOOLEAN_STRING,
            "char")
        .contains(fieldType);
  }

  public static boolean isWrapperType(String fieldType) {
    return java.util.Arrays.asList(
            JAVA_LANG_INTEGER, INTEGER,
            JAVA_LANG_LONG, LONG,
            JAVA_LANG_SHORT, SHORT,
            JAVA_LANG_BYTE, BYTE,
            JAVA_LANG_FLOAT, FLOAT,
            JAVA_LANG_DOUBLE, DOUBLE,
            JAVA_LANG_BOOLEAN, BOOLEAN,
            JAVA_LANG_CHARACTER, CHARACTER)
        .contains(fieldType);
  }

  public static String getCollectionDefaultValue(String fieldType) {
    String base = GeneratorUtility.extractBaseType(fieldType);
    return switch (base) {
      case JAVA_UTIL_LIST, "List" -> "new java.util.ArrayList<>()";
      case JAVA_UTIL_SET, "Set" -> "new java.util.HashSet<>()";
      case JAVA_UTIL_MAP, "Map" -> "new java.util.HashMap<>()";
      default -> null;
    };
  }

  public static boolean hasExplicitDtoDefault(
      VariableElement field, DtoBuilderDefault builderDefault) {
    String type = field.asType().toString();
    return !builderDefault.stringValue().isEmpty()
        || builderDefault.intValue() != Integer.MIN_VALUE
        || builderDefault.longValue() != Long.MIN_VALUE
        || builderDefault.shortValue() != Short.MIN_VALUE
        || builderDefault.byteValue() != Byte.MIN_VALUE
        || builderDefault.floatValue() != Float.MIN_VALUE
        || builderDefault.doubleValue() != Double.MIN_VALUE
        || builderDefault.booleanValue()
        || builderDefault.charValue() != '\0'
        || !builderDefault.enumValue().isEmpty()
        || !builderDefault.value().isEmpty()
        || isCollectionType(type)
        || isOptionalType(type);
  }
}
