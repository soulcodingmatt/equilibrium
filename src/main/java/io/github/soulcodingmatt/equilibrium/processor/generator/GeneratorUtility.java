package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.annotations.common.IgnoreAll;
import io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto;
import io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord;
import io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class containing common code generation methods shared across different generators.
 * This class helps eliminate code duplication between DtoGenerator, VoGenerator, and RecordGenerator.
 */
public class GeneratorUtility {
    
    // Common constants
    public static final String STRING_END = "    }\n\n";
    public static final String OVERRIDE = "    @Override\n";
    
    /**
     * Enum representing different generator types for conditional logic
     */
    public enum GeneratorType {
        DTO, VO, RECORD
    }
    
    /**
     * Configuration for field inclusion logic
     */
    public static class FieldInclusionConfig {
        private final GeneratorType generatorType;
        private final Set<String> ignoredFields;
        private final int entityId;
        
        public FieldInclusionConfig(GeneratorType generatorType, Set<String> ignoredFields, int entityId) {
            this.generatorType = generatorType;
            this.ignoredFields = ignoredFields != null ? ignoredFields : new HashSet<>();
            this.entityId = entityId;
        }
        
        public GeneratorType getGeneratorType() { return generatorType; }
        public Set<String> getIgnoredFields() { return ignoredFields; }
        public int getEntityId() { return entityId; }
    }
    
    /**
     * Configuration for accessor generation
     */
    public static class AccessorConfig {
        private final boolean generateSetters;
        private final Function<VariableElement, String> typeTransformer;
        
        public AccessorConfig(boolean generateSetters, Function<VariableElement, String> typeTransformer) {
            this.generateSetters = generateSetters;
            this.typeTransformer = typeTransformer != null ? typeTransformer : (field -> field.asType().toString());
        }
        
        public boolean shouldGenerateSetters() { return generateSetters; }
        public Function<VariableElement, String> getTypeTransformer() { return typeTransformer; }
    }
    
    /**
     * Configuration for constructor generation
     */
    public static class ConstructorConfig {
        private final boolean makeFinalFields;
        private final Function<VariableElement, String> typeTransformer;
        
        public ConstructorConfig(boolean makeFinalFields, Function<VariableElement, String> typeTransformer) {
            this.makeFinalFields = makeFinalFields;
            this.typeTransformer = typeTransformer != null ? typeTransformer : (field -> field.asType().toString());
        }
        
        public boolean shouldMakeFinalFields() { return makeFinalFields; }
        public Function<VariableElement, String> getTypeTransformer() { return typeTransformer; }
    }
    
    /**
     * Get all fields that should be included in the generated class
     */
    public static List<VariableElement> getIncludedFields(TypeElement classElement, FieldInclusionConfig config) {
        return collectFieldsFromHierarchy(classElement, config);
    }
    
    /**
     * Recursively collect fields from the class hierarchy
     */
    public static List<VariableElement> collectFieldsFromHierarchy(TypeElement element, FieldInclusionConfig config) {
        List<VariableElement> fields = element.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .filter(field -> shouldIncludeField(field, config))
            .toList();

        // Get parent class fields
        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE && !superclass.toString().equals("java.lang.Object")) {
            TypeElement superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();
            fields = Stream.concat(
                fields.stream(),
                collectFieldsFromHierarchy(superclassElement, config).stream()
            ).toList();
        }

        return fields;
    }
    
    /**
     * Determine if a field should be included based on annotations and configuration
     */
    public static boolean shouldIncludeField(VariableElement field, FieldInclusionConfig config) {
        // Exclude fields marked with @IgnoreAll
        if (field.getAnnotation(IgnoreAll.class) != null) {
            return false;
        }
        
        // Handle generator-specific ignore annotations
        switch (config.getGeneratorType()) {
            case DTO:
                if (!shouldIncludeFieldForDto(field, config.getEntityId())) {
                    return false;
                }
                break;
            case VO:
                if (!shouldIncludeFieldForVo(field, config.getEntityId())) {
                    return false;
                }
                break;
            case RECORD:
                if (!shouldIncludeFieldForRecord(field, config.getEntityId())) {
                    return false;
                }
                break;
        }
        
        // Exclude any fields specified in the ignore collection
        String fieldName = field.getSimpleName().toString();
        if (config.getIgnoredFields().contains(fieldName)) {
            return false;
        }
        
        // Exclude static and transient fields
        Set<Modifier> modifiers = field.getModifiers();
        return !modifiers.contains(Modifier.STATIC) && 
               !modifiers.contains(Modifier.TRANSIENT);
    }
    
    private static boolean shouldIncludeFieldForDto(VariableElement field, int dtoId) {
        IgnoreDto ignoreDtoAnnotation = field.getAnnotation(IgnoreDto.class);
        if (ignoreDtoAnnotation != null) {
            int[] ignoredIds = ignoreDtoAnnotation.ids();
            
            // If no IDs specified, ignore for all DTOs
            if (ignoredIds.length == 0) {
                return false;
            }
            
            // If IDs specified, only ignore if current DTO ID is in the list
            for (int ignoredId : ignoredIds) {
                if (ignoredId == dtoId) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean shouldIncludeFieldForVo(VariableElement field, int voId) {
        IgnoreVo ignoreVoAnnotation = field.getAnnotation(IgnoreVo.class);
        if (ignoreVoAnnotation != null) {
            int[] ignoredIds = ignoreVoAnnotation.ids();
            
            // If no IDs specified, ignore for all VOs
            if (ignoredIds.length == 0) {
                return false;
            }
            
            // If IDs specified, only ignore if current VO ID is in the list
            for (int ignoredId : ignoredIds) {
                if (ignoredId == voId) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean shouldIncludeFieldForRecord(VariableElement field, int recordId) {
        IgnoreRecord ignoreRecordAnnotation = field.getAnnotation(IgnoreRecord.class);
        if (ignoreRecordAnnotation != null) {
            int[] ignoredIds = ignoreRecordAnnotation.ids();
            
            // If no IDs specified, ignore for all Records
            if (ignoredIds.length == 0) {
                return false;
            }
            
            // If IDs specified, only ignore if current Record ID is in the list
            for (int ignoredId : ignoredIds) {
                if (ignoredId == recordId) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Write basic imports for the generated class
     */
    public static void writeBasicImports(Writer writer, List<VariableElement> fields) throws IOException {
        writer.write("import java.util.Objects;\n");
        
        Set<String> imports = fields.stream()
            .map(field -> field.asType().toString())
            .map(GeneratorUtility::extractBaseType)
            .filter(type -> type.contains("."))
            .collect(Collectors.toSet());
        
        for (String importType : imports) {
            writer.write("import " + importType + ";\n");
        }
        writer.write("\n");
    }
    
    /**
     * Extract base type from a full type string, removing generics
     */
    public static String extractBaseType(String fullType) {
        // Remove generic type parameters for import statements
        int genericStart = fullType.indexOf('<');
        if (genericStart > 0) {
            return fullType.substring(0, genericStart);
        }
        return fullType;
    }
    
    /**
     * Write constructor for the generated class
     */
    public static void writeConstructor(Writer writer, List<VariableElement> fields, String className, ConstructorConfig config) throws IOException {
        writer.write("    public " + className + "(");
        
        // Write constructor parameters
        boolean first = true;
        for (VariableElement field : fields) {
            if (!first) {
                writer.write(", ");
            }
            String type = config.getTypeTransformer().apply(field);
            String name = field.getSimpleName().toString();
            writer.write(type + " " + name);
            first = false;
        }
        writer.write(") {\n");
        
        // Write field assignments
        for (VariableElement field : fields) {
            String name = field.getSimpleName().toString();
            writer.write("        this." + name + " = " + name + ";\n");
        }
        writer.write(STRING_END);
    }
    
    /**
     * Write field declaration
     */
    public static void writeField(Writer writer, VariableElement field, ConstructorConfig config) throws IOException {
        String type = config.getTypeTransformer().apply(field);
        String name = field.getSimpleName().toString();
        writer.write("    private " + (config.shouldMakeFinalFields() ? "final " : "") + type + " " + name + ";\n\n");
    }
    
    /**
     * Write accessors (getters and optionally setters) for a field
     */
    public static void writeAccessors(Writer writer, VariableElement field, AccessorConfig config) throws IOException {
        String type = config.getTypeTransformer().apply(field);
        String name = field.getSimpleName().toString();
        String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        // Getter
        writer.write("    public " + type + " get" + capitalizedName + "() {\n");
        writer.write("        return " + name + ";\n");
        writer.write(STRING_END);
        
        // Setter (only if enabled)
        if (config.shouldGenerateSetters()) {
            writer.write("    public void set" + capitalizedName + "(" + type + " " + name + ") {\n");
            writer.write("        this." + name + " = " + name + ";\n");
            writer.write(STRING_END);
        }
    }
    
    /**
     * Write equals method
     */
    public static void writeEquals(Writer writer, List<VariableElement> fields, String className) throws IOException {
        writer.write(OVERRIDE);
        writer.write("    public boolean equals(Object o) {\n");
        writer.write("        if (this == o) return true;\n");
        writer.write("        if (o == null || getClass() != o.getClass()) return false;\n");
        writer.write("        " + className + " that = (" + className + ") o;\n");
        
        // Compare each field
        for (VariableElement field : fields) {
            String name = field.getSimpleName().toString();
            writer.write("        if (!Objects.equals(" + name + ", that." + name + ")) return false;\n");
        }
        
        writer.write("        return true;\n");
        writer.write(STRING_END);
    }
    
    /**
     * Write hashCode method
     */
    public static void writeHashCode(Writer writer, List<VariableElement> fields) throws IOException {
        writer.write(OVERRIDE);
        writer.write("    public int hashCode() {\n");
        writer.write("        return Objects.hash(");
        
        // Add all fields to hash
        boolean first = true;
        for (VariableElement field : fields) {
            if (!first) {
                writer.write(", ");
            }
            writer.write(field.getSimpleName().toString());
            first = false;
        }
        
        writer.write(");\n");
        writer.write(STRING_END);
    }
    
    /**
     * Write toString method
     */
    public static void writeToString(Writer writer, List<VariableElement> fields, String className) throws IOException {
        writer.write(OVERRIDE);
        writer.write("    public String toString() {\n");
        writer.write("        return \"" + className + "{\" +\n");
        
        // Add all fields to string representation
        boolean first = true;
        for (VariableElement field : fields) {
            String name = field.getSimpleName().toString();
            if (first) {
                writer.write("            \"" + name + "=\" + " + name);
                first = false;
            } else {
                writer.write(" +\n            \", " + name + "=\" + " + name);
            }
        }
        
        writer.write(" +\n            \"}\";\n");
        writer.write(STRING_END);
    }
    
    /**
     * Write record parameters for record declaration
     */
    public static void writeRecordParameters(Writer writer, List<VariableElement> fields, Function<VariableElement, String> typeTransformer) throws IOException {
        boolean first = true;
        for (VariableElement field : fields) {
            if (!first) {
                writer.write(", ");
            }
            String type = typeTransformer != null ? typeTransformer.apply(field) : field.asType().toString();
            String name = field.getSimpleName().toString();
            writer.write(type + " " + name);
            first = false;
        }
    }
} 