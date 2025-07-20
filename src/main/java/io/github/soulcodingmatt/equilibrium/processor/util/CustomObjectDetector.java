package io.github.soulcodingmatt.equilibrium.processor.util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Set;

/**
 * Utility class for detecting custom objects that should be converted to DTOs
 * in nested object handling.
 */
public class CustomObjectDetector {
    
    // JDK and common library packages that should NOT be converted to DTOs
    private static final Set<String> EXCLUDED_PACKAGES = Set.of(
        "java.lang",
        "java.util",
        "java.time",
        "java.math",
        "java.io",
        "java.net",
        "java.nio",
        "javax.",
        "jakarta."
    );
    
    // Common types that should NOT be converted to DTOs
    private static final Set<String> EXCLUDED_TYPES = Set.of(
        "java.lang.String",
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Short", 
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Float",
        "java.lang.Double",
        "java.lang.Character",
        "java.math.BigDecimal",
        "java.math.BigInteger",
        "java.time.LocalDate",
        "java.time.LocalTime",
        "java.time.LocalDateTime",
        "java.time.ZonedDateTime",
        "java.time.OffsetDateTime",
        "java.time.Instant",
        "java.time.Duration",
        "java.time.Period",
        "java.util.UUID"
    );
    
    /**
     * Determines if a type represents a custom object that should be converted to a DTO.
     * 
     * @param type the type to check
     * @return true if this is a custom object that needs DTO conversion
     */
    public static boolean isCustomObject(TypeMirror type) {
        // Handle primitive types
        if (type.getKind().isPrimitive()) {
            return false;
        }
        
        // Handle declared types (classes, interfaces)
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            
            // Check if it's an excluded type
            if (EXCLUDED_TYPES.contains(qualifiedName)) {
                return false;
            }
            
            // Check if it's in an excluded package
            for (String excludedPackage : EXCLUDED_PACKAGES) {
                if (qualifiedName.startsWith(excludedPackage)) {
                    return false;
                }
            }
            
            // If we get here, it's likely a custom object
            return true;
        }
        
        // Other types (arrays, wildcards, etc.) are not custom objects
        return false;
    }
    
    /**
     * Extracts the element type from collection types like {@code List<CustomObject>}.
     * Returns the element type that should be checked for DTO conversion.
     * 
     * @param collectionType the collection type (List, Set, etc.)
     * @return the element type, or null if not a parameterized collection
     */
    public static TypeMirror getCollectionElementType(TypeMirror collectionType) {
        if (collectionType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) collectionType;
            
            // Check if it's a parameterized type with type arguments
            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                String qualifiedName = typeElement.getQualifiedName().toString();
                
                // Check if it's a known collection type
                if (isCollectionType(qualifiedName)) {
                    // Return the first type argument (element type)
                    return declaredType.getTypeArguments().get(0);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a qualified name represents a collection type.
     */
    private static boolean isCollectionType(String qualifiedName) {
        return qualifiedName.equals("java.util.List") ||
               qualifiedName.equals("java.util.Set") ||
               qualifiedName.equals("java.util.Collection") ||
               qualifiedName.equals("java.util.ArrayList") ||
               qualifiedName.equals("java.util.LinkedList") ||
               qualifiedName.equals("java.util.HashSet") ||
               qualifiedName.equals("java.util.LinkedHashSet") ||
               qualifiedName.equals("java.util.TreeSet");
    }
    
    /**
     * Determines if a type is a collection that can contain custom objects.
     * 
     * @param type the type to check
     * @return true if this is a collection of custom objects
     */
    public static boolean isCustomObjectCollection(TypeMirror type) {
        TypeMirror elementType = getCollectionElementType(type);
        return elementType != null && isCustomObject(elementType);
    }
} 
