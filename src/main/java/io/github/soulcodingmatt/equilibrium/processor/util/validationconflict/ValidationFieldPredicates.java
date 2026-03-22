package io.github.soulcodingmatt.equilibrium.processor.util.validationconflict;

import java.util.Arrays;

/** Type-shape predicates used when checking Jakarta validation applicability. */
public final class ValidationFieldPredicates {

  private ValidationFieldPredicates() {}

  public static boolean isPrimitiveType(String typeName) {
    return Arrays.asList("int", "long", "short", "byte", "float", "double", "boolean", "char")
        .contains(typeName);
  }

  public static boolean isStringType(String typeName) {
    return typeName.equals("java.lang.String") || typeName.equals("String");
  }

  public static boolean isNumericType(String typeName) {
    return Arrays.asList(
            "int",
            "long",
            "short",
            "byte",
            "float",
            "double",
            "java.lang.Integer",
            "Integer",
            "java.lang.Long",
            "Long",
            "java.lang.Short",
            "Short",
            "java.lang.Byte",
            "Byte",
            "java.lang.Float",
            "Float",
            "java.lang.Double",
            "Double",
            "java.math.BigDecimal",
            "BigDecimal",
            "java.math.BigInteger",
            "BigInteger")
        .contains(typeName);
  }

  public static boolean isCollectionType(String typeName) {
    return typeName.contains("java.util.List")
        || typeName.contains("List")
        || typeName.contains("java.util.Set")
        || typeName.contains("Set")
        || typeName.contains("java.util.Collection")
        || typeName.contains("Collection")
        || typeName.contains("java.util.Queue")
        || typeName.contains("Queue")
        || typeName.contains("java.util.Deque")
        || typeName.contains("Deque");
  }

  public static boolean isMapType(String typeName) {
    return typeName.contains("java.util.Map") || typeName.contains("Map");
  }

  public static boolean isArrayType(String typeName) {
    return typeName.contains("[]");
  }

  public static boolean isTemporalType(String typeName) {
    return Arrays.asList(
            "java.util.Date",
            "Date",
            "java.util.Calendar",
            "Calendar",
            "java.time.LocalDate",
            "LocalDate",
            "java.time.LocalDateTime",
            "LocalDateTime",
            "java.time.LocalTime",
            "LocalTime",
            "java.time.ZonedDateTime",
            "ZonedDateTime",
            "java.time.OffsetDateTime",
            "OffsetDateTime",
            "java.time.Instant",
            "Instant")
        .contains(typeName);
  }
}
