package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * Parses and validates numeric, boolean, and character defaults from legacy {@code value()} text.
 */
public final class DefaultValueNumericParsing {

  private DefaultValueNumericParsing() {}

  public static String processPrimitiveValue(
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
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
              Diagnostic.Kind.WARNING,
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
          Diagnostic.Kind.ERROR,
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
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
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
          Diagnostic.Kind.ERROR,
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
}
