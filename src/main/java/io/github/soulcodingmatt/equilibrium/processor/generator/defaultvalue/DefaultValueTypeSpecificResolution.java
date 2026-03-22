package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;

/** Maps type-specific {@link DtoBuilderDefault} attributes to Java initializer text. */
public final class DefaultValueTypeSpecificResolution {

  private DefaultValueTypeSpecificResolution() {}

  public static String getTypeSpecificValue(
      VariableElement field, DtoBuilderDefault builderDefault, Messager messager) {
    String fieldType = field.asType().toString();

    if (DefaultValueTypePredicates.isPrimitiveType(fieldType)
        || DefaultValueTypePredicates.isWrapperType(fieldType)) {
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

    if (DefaultValueTypePredicates.isStringType(fieldType)
        && !builderDefault.stringValue().isEmpty()) {
      return DefaultValueStringLiterals.processStringValue(builderDefault.stringValue());
    }

    if (DefaultValueTypePredicates.isEnumField(field) && !builderDefault.enumValue().isEmpty()) {
      return DefaultValueEnumReference.processEnumValue(
          field, builderDefault.enumValue(), messager);
    }

    return null;
  }
}
