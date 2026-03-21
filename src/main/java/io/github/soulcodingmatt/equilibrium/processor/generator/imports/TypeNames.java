package io.github.soulcodingmatt.equilibrium.processor.generator.imports;

/**
 * Central place for commonly used type name constants to reduce clutter in generators. Only a
 * subset is introduced now; more can be moved progressively.
 */
public final class TypeNames {

  private TypeNames() {
    throw new AssertionError("You should not be here!");
  }

  public static final String JAVA_UTIL_OPTIONAL = "java.util.Optional";
  public static final String JAVA_UTIL_LIST = "java.util.List";
  public static final String JAVA_UTIL_SET = "java.util.Set";
  public static final String JAVA_UTIL_MAP = "java.util.Map";

  public static final String CHARACTER = "Character";
  public static final String JAVA_LANG_CHARACTER = "java.lang.Character";
  public static final String BOOLEAN = "Boolean";
  public static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
  public static final String BOOLEAN_STRING = "boolean";
  public static final String DOUBLE = "Double";
  public static final String JAVA_LANG_DOUBLE = "java.lang.Double";
  public static final String DOUBLE_STRING = "double";
  public static final String FLOAT = "Float";
  public static final String JAVA_LANG_FLOAT = "java.lang.Float";
  public static final String FLOAT_STRING = "float";
  public static final String BYTE = "Byte";
  public static final String JAVA_LANG_BYTE = "java.lang.Byte";
  public static final String BYTE_STRING = "byte";
  public static final String SHORT = "Short";
  public static final String JAVA_LANG_SHORT = "java.lang.Short";
  public static final String SHORT_STRING = "short";
  public static final String LONG = "Long";
  public static final String JAVA_LANG_LONG = "java.lang.Long";
  public static final String LONG_STRING = "long";
  public static final String INTEGER = "Integer";
  public static final String JAVA_LANG_INTEGER = "java.lang.Integer";
  public static final String INTEGER_STRING = "int";
}
