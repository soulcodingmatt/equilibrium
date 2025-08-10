package io.github.soulcodingmatt.equilibrium.annotations.dto;

/**
 * Utility class providing static final variables for @DtoBuilderDefault annotation.
 * This class contains static final variables that can be used in annotation parameters
 * to provide type-safe default values for enums.
 * 
 * Usage:
 * <pre>
 * import static io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefaults.*;
 * 
 * &#64;DtoBuilderDefault(enumValue = STATUS_ACTIVE)
 * private Status status;
 * </pre>
 */
public final class DtoBuilderDefaults {
    
    private DtoBuilderDefaults() {
        // Utility class - prevent instantiation
    }
    
    // Example enum constants - users can create similar ones for their enums
    public static final String STATUS_ACTIVE = "Status.ACTIVE";
    public static final String STATUS_INACTIVE = "Status.INACTIVE";
    public static final String STATUS_PENDING = "Status.PENDING";
    
    /**
     * Helper method to generate static final variable declarations for your enums.
     * Copy the output and add it to your class or create a similar utility class.
     * 
     * @param enumClass the enum class to generate constants for
     * @return a string containing static final variable declarations
     */
    public static String generateEnumConstants(Class<? extends Enum<?>> enumClass) {
        StringBuilder sb = new StringBuilder();
        String className = enumClass.getSimpleName();
        
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            String constantName = className.toUpperCase() + "_" + constant.name();
            sb.append("public static final String ").append(constantName)
              .append(" = \"").append(className).append(".").append(constant.name()).append("\";\n");
        }
        
        return sb.toString();
    }
} 