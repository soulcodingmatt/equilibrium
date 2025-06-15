package com.soulcodingmatt.equilibrium.processor.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(?>\\.[a-z][a-z0-9_]*)*$");
    private static final Pattern POSTFIX_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");

    private ValidationUtil(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates a package name.
     * @param pkg the package name to validate
     * @return {@code true} if the package name is valid, {@code false} otherwise
     */
    public static boolean isValidPackageName(String pkg) {
        return pkg != null && !pkg.isEmpty() && PACKAGE_PATTERN.matcher(pkg).matches();
    }

    /**
     * Validates a postfix for a generated class.
     * @param postfix the postfix to validate
     * @return {@code true} if the postfix is valid, {@code false} otherwise
     */
    public static boolean isValidPostfix(String postfix) {
        return postfix != null && !postfix.isEmpty() && POSTFIX_PATTERN.matcher(postfix).matches();
    }

    /**
     * Validates a field name.
     * @param fieldName the field name to validate
     * @return {@code true} if the field name is valid, {@code false} otherwise
     */
    public static boolean isValidFieldName(String fieldName) {
        return fieldName != null && !fieldName.isEmpty() && FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

    /**
     * Gets the default package name based on project coordinates and class type.
     * @param groupId the project's group ID
     * @param artifactId the project's artifact ID
     * @param classType the type of class being generated ("dto", "record", or "vo")
     * @return the default package name
     */
    public static String getDefaultPackageName(String groupId, String artifactId, String classType) {
        return groupId + "." + artifactId + "." + classType.toLowerCase();
    }

    /**
     * Gets the default postfix for a class type.
     * @param classType the type of class being generated ("DTO", "Record", or "VO")
     * @return the default postfix
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
