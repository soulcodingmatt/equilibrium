package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import static io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.processor.generation.emit.GeneratorUtility;

/** Default initializer expressions for collection-typed fields. */
public final class DefaultValueCollectionDefaults {

  private DefaultValueCollectionDefaults() {}

  public static String getCollectionDefaultValue(String fieldType) {
    String base = GeneratorUtility.extractBaseType(fieldType);
    return switch (base) {
      case JAVA_UTIL_LIST, "List" -> "new java.util.ArrayList<>()";
      case JAVA_UTIL_SET, "Set" -> "new java.util.HashSet<>()";
      case JAVA_UTIL_MAP, "Map" -> "new java.util.HashMap<>()";
      default -> null;
    };
  }
}
