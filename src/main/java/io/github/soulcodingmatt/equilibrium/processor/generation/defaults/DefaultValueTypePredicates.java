package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import static io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.processor.generation.emit.GeneratorUtility;
import java.util.Arrays;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

/** Classification of field types for {@code @DtoBuilderDefault} resolution. */
public final class DefaultValueTypePredicates {

  private DefaultValueTypePredicates() {}

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
    return Arrays.asList(
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
    return Arrays.asList(
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

  public static boolean isEnumField(VariableElement field) {
    return field.asType().getKind() == TypeKind.DECLARED
        && ((DeclaredType) field.asType()).asElement().getKind() == ElementKind.ENUM;
  }
}
