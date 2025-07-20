package io.github.soulcodingmatt.equilibrium.processor.util;

import java.util.Set;
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
    // Pattern for validating individual package parts (no nested quantifiers)
    private static final Pattern PACKAGE_PART_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_\\-]*$");

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    
    // Java reserved keywords that cannot be used in package names
    // While technically allowed by JLS, they prevent class creation in those packages
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", 
        "class", "const", "continue", "default", "do", "double", "else", "enum", 
        "extends", "final", "finally", "float", "for", "goto", "if", "implements", 
        "import", "instanceof", "int", "interface", "long", "native", "new", 
        "package", "private", "protected", "public", "return", "short", "static", 
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", 
        "transient", "try", "void", "volatile", "while"
    );
    public static final String PACKAGE_NAME = "Package name '";

    private ValidationUtil(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates a package name for technical correctness.
     * @param pkg the package name to validate
     * @return {@code true} if the package name is technically valid, {@code false} otherwise
     */
    public static boolean isValidPackageName(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        
        // Check for leading/trailing dots or consecutive dots
        if (pkg.startsWith(".") || pkg.endsWith(".") || pkg.contains("..")) {
            return false;
        }
        
        // Split and validate each part
        String[] parts = pkg.split("\\.");
        for (String part : parts) {
            // Each part must match the pattern and not be a reserved keyword
            if (!PACKAGE_PART_PATTERN.matcher(part).matches() || 
                JAVA_KEYWORDS.contains(part.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Gets a detailed validation error message for an invalid package name.
     * @param pkg the package name to check
     * @return detailed error message, or null if package name is valid
     */
    public static String getPackageValidationError(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return "Package name cannot be null or empty";
        }
        
        // Check for leading/trailing dots or consecutive dots
        if (pkg.startsWith(".") || pkg.endsWith(".")) {
            return PACKAGE_NAME + pkg + "' cannot start or end with a dot";
        }
        
        if (pkg.contains("..")) {
            return PACKAGE_NAME + pkg + "' cannot contain consecutive dots";
        }
        
        // Split and validate each part
        String[] parts = pkg.split("\\.");
        for (String part : parts) {
            if (!PACKAGE_PART_PATTERN.matcher(part).matches()) {
                return PACKAGE_NAME + pkg + "' has invalid part '" + part + "'. Each part must start with a letter and contain only letters, numbers, underscores, and hyphens";
            }
            
            if (JAVA_KEYWORDS.contains(part.toLowerCase())) {
                return PACKAGE_NAME + pkg + "' contains Java reserved keyword '" + part + "'. Reserved keywords cannot be used in package names as they prevent class creation";
            }
        }
        
        return null; // Package name is valid
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
