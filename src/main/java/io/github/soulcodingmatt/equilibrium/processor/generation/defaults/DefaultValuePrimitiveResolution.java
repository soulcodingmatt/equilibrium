package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import static io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;

/**
 * Resolves {@link DtoBuilderDefault} numeric and boolean primitive/wrapper attributes to Java
 * initializer text.
 */
final class DefaultValuePrimitiveResolution {

  private DefaultValuePrimitiveResolution() {}

  static String tryResolve(DtoBuilderDefault builderDefault, String fieldType) {
    if (!DefaultValueTypePredicates.isPrimitiveType(fieldType)
        && !DefaultValueTypePredicates.isWrapperType(fieldType)) {
      return null;
    }
    String s = tryIntegral(builderDefault, fieldType);
    if (s != null) {
      return s;
    }
    s = tryFloating(builderDefault, fieldType);
    if (s != null) {
      return s;
    }
    s = tryBoolean(builderDefault, fieldType);
    if (s != null) {
      return s;
    }
    return tryCharacter(builderDefault, fieldType);
  }

  private static String tryIntegral(DtoBuilderDefault builderDefault, String fieldType) {
    if (matchesIntegerFamily(fieldType) && builderDefault.intValue() != Integer.MIN_VALUE) {
      return String.valueOf(builderDefault.intValue());
    }
    if (matchesLongFamily(fieldType) && builderDefault.longValue() != Long.MIN_VALUE) {
      return builderDefault.longValue() + "L";
    }
    if (matchesShortFamily(fieldType) && builderDefault.shortValue() != Short.MIN_VALUE) {
      return String.valueOf(builderDefault.shortValue());
    }
    if (matchesByteFamily(fieldType) && builderDefault.byteValue() != Byte.MIN_VALUE) {
      return String.valueOf(builderDefault.byteValue());
    }
    return null;
  }

  private static String tryFloating(DtoBuilderDefault builderDefault, String fieldType) {
    if (matchesFloatFamily(fieldType) && builderDefault.floatValue() != Float.MIN_VALUE) {
      return builderDefault.floatValue() + "f";
    }
    if (matchesDoubleFamily(fieldType) && builderDefault.doubleValue() != Double.MIN_VALUE) {
      return String.valueOf(builderDefault.doubleValue());
    }
    return null;
  }

  private static String tryBoolean(DtoBuilderDefault builderDefault, String fieldType) {
    if (!matchesBooleanFamily(fieldType)) {
      return null;
    }
    if (builderDefault.booleanValue()) {
      return "true";
    }
    return null;
  }

  private static String tryCharacter(DtoBuilderDefault builderDefault, String fieldType) {
    if (matchesCharacterFamily(fieldType) && builderDefault.charValue() != '\0') {
      return "'" + builderDefault.charValue() + "'";
    }
    return null;
  }

  private static boolean matchesIntegerFamily(String fieldType) {
    return fieldType.equals(INTEGER_STRING)
        || fieldType.equals(JAVA_LANG_INTEGER)
        || fieldType.equals(INTEGER);
  }

  private static boolean matchesLongFamily(String fieldType) {
    return fieldType.equals(LONG_STRING)
        || fieldType.equals(JAVA_LANG_LONG)
        || fieldType.equals(LONG);
  }

  private static boolean matchesShortFamily(String fieldType) {
    return fieldType.equals(SHORT_STRING)
        || fieldType.equals(JAVA_LANG_SHORT)
        || fieldType.equals(SHORT);
  }

  private static boolean matchesByteFamily(String fieldType) {
    return fieldType.equals(BYTE_STRING)
        || fieldType.equals(JAVA_LANG_BYTE)
        || fieldType.equals(BYTE);
  }

  private static boolean matchesFloatFamily(String fieldType) {
    return fieldType.equals(FLOAT_STRING)
        || fieldType.equals(JAVA_LANG_FLOAT)
        || fieldType.equals(FLOAT);
  }

  private static boolean matchesDoubleFamily(String fieldType) {
    return fieldType.equals(DOUBLE_STRING)
        || fieldType.equals(JAVA_LANG_DOUBLE)
        || fieldType.equals(DOUBLE);
  }

  private static boolean matchesBooleanFamily(String fieldType) {
    return fieldType.equals(BOOLEAN_STRING)
        || fieldType.equals(JAVA_LANG_BOOLEAN)
        || fieldType.equals(BOOLEAN);
  }

  private static boolean matchesCharacterFamily(String fieldType) {
    return fieldType.equals("char")
        || fieldType.equals(JAVA_LANG_CHARACTER)
        || fieldType.equals(CHARACTER);
  }
}
