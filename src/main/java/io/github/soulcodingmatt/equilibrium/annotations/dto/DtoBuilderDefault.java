package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.*;

/**
 * Annotation to specify default values for fields in generated DTOs when using the builder pattern.
 * This annotation only has effect when the @GenerateDto annotation has builder = true.
 * 
 * When applied to a field, the generated DTO will include a @Builder.Default annotation
 * with the specified default value.
 * 
 * String fields are handled automatically - quotes are added if missing:
 * - @DtoBuilderDefault("Hello") generates: private String name = "Hello";
 * 
 * Smart enum support with auto-detection and validation:
 * - @DtoBuilderDefault("ACTIVE") generates: private Status status = Status.ACTIVE;
 *   (enum type auto-detected from field, constant validated at compile-time)
 * 
 * Special handling for collections and Optional:
 * - List fields: defaults to new ArrayList&lt;&gt;()
 * - Set fields: defaults to new HashSet&lt;&gt;()
 * - Map fields: defaults to new HashMap&lt;&gt;()
 * - Optional fields: defaults to Optional.empty()
 * 
 * For these types, the annotation can be used without a value parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DtoBuilderDefault {
    /**
     * The default value for the field.
     * 
     * For primitive types and strings:
     * - @DtoBuilderDefault("42") for int fields
     * - @DtoBuilderDefault("true") for boolean fields  
     * - @DtoBuilderDefault("Hello") for String fields (quotes added automatically)
     * - @DtoBuilderDefault("1.5f") for float fields
     * 
     * For enum fields (auto-detected from field type):
     * - @DtoBuilderDefault("ACTIVE") for Status status field (enum type auto-detected)
     * - @DtoBuilderDefault("Status.ACTIVE") for qualified enum reference
     * 
     * The annotation processor will:
     * - Auto-detect if the field is an enum type
     * - Validate that the constant exists in the enum
     * - Generate the proper enum reference (Status.ACTIVE)
     * 
     * For collections (List, Set, Map) and Optional, this parameter is optional
     * and will use standard defaults if not specified.
     * 
     * @return the default value
     */
    String value() default "";
} 