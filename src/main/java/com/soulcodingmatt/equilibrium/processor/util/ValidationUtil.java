package com.soulcodingmatt.equilibrium.processor.util;

import java.util.regex.Pattern;

/**
 * The ValidationUtil enforces the following technical requirements:
 * <p>
 * All patterns (package, postfix, and field names):
 * - No spaces allowed
 * - No special characters allowed (except dots, underscores, and hyphens in package names)
 * - No consecutive dots in package names
 * - No leading/trailing dots in package names
 * - No null or empty values allowed
 * </p>
 * <p>
 * Package names:
 * - Must start with a letter
 * - Can contain letters, numbers, dots, underscores, and hyphens
 * - Parts must be separated by dots
 * - Each part must start with a letter
 * </p>
 * <p>
 * Postfix and field names:
 * - Can contain letters and numbers
 * - No special characters allowed
 * </p>
 * <p>
 * Note: While this utility only enforces technical correctness,
 * the default values and generated names will follow Java naming conventions:
 * - Package names: lowercase with dots
 * - Class names (postfix): PascalCase
 * - Field names: camelCase
 * </p>
 */
public class ValidationUtil {
    // Package names must start with a letter, and each part after a dot must also start with a letter
    // Can contain letters, numbers, underscores, and hyphens
    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*+(?:\\.[a-zA-Z][a-zA-Z0-9_-]*+)*+$");

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private ValidationUtil(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates a package name for technical correctness.
     * @param pkg the package name to validate
     * @return {@code true} if the package name is technically valid, {@code false} otherwise
     */
    public static boolean isValidPackageName(String pkg) {
        return pkg != null && !pkg.isEmpty() && PACKAGE_PATTERN.matcher(pkg).matches();
    }

    /**
     * Validates a postfix for technical correctness.
     * @param postfix the postfix to validate
     * @return {@code true} if the postfix is technically valid, {@code false} otherwise
     */
    public static boolean isValidPostfix(String postfix) {
        return postfix != null && !postfix.isEmpty() && ALPHANUMERIC_PATTERN.matcher(postfix).matches();
    }

    /**
     * Validates a field name for technical correctness.
     * @param fieldName the field name to validate
     * @return {@code true} if the field name is technically valid, {@code false} otherwise
     */
    public static boolean isValidFieldName(String fieldName) {
        return fieldName != null && !fieldName.isEmpty() && ALPHANUMERIC_PATTERN.matcher(fieldName).matches();
    }

    /**
     * Gets the default package name based on project coordinates and class type.
     * This method follows Java naming conventions for the generated package name.
     * @param groupId the project's group ID
     * @param artifactId the project's artifact ID
     * @param classType the type of class being generated ("dto", "record", or "vo")
     * @return the default package name following Java conventions
     */
    public static String getDefaultPackageName(String groupId, String artifactId, String classType) {
        return groupId.toLowerCase() + "." + artifactId.toLowerCase() + "." + classType.toLowerCase();
    }

    /**
     * Gets the default postfix for a class type.
     * This method follows Java naming conventions for the generated postfix.
     * @param classType the type of class being generated ("DTO", "Record", or "VO")
     * @return the default postfix following Java conventions
     */
    public static String getDefaultPostfix(String classType) {
        return switch (classType) {
            case "DTO" -> "Dto";
            case "Record" -> "Record";
            case "VO" -> "Vo";
            default -> throw new IllegalArgumentException("Invalid class type: " + classType);
        };
    }
}
