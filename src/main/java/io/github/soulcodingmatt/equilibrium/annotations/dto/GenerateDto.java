package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a DTO class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreDto or @IgnoreAll.
 * 
 * This annotation can be used multiple times on the same class to generate
 * multiple DTOs with different configurations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GenerateDtos.class)
public @interface GenerateDto {
    /**
     * Optional ID for this DTO generation annotation.
     * Used with @IgnoreDto(ids={...}) to selectively ignore fields for specific DTOs.
     * When multiple @GenerateDto annotations are used, each must have a unique ID if specified.
     * @return the ID for this DTO generation
     */
    int id() default -1;

    /**
     * The package where the DTO should be generated.
     * If not specified, the global package configuration from pom.xml will be used.
     * @return the target package name
     */
    String pkg() default "";

    /**
     * The complete name for the generated DTO class.
     * If specified, this exact name will be used without any postfix.
     * If not specified, the class name will be constructed using the original class name 
     * plus a postfix (determined by compiler arguments or the default "Dto").
     * @return the complete name for the generated class
     */
    String name() default "";

    /**
     * The fields of the base class to ignore. They will not be generated in the DTO.
     * If this option isn't enough, use @IgnoreDto to exclude further fields.
     * @return the names of the fields to ignore
     */
    String[] ignore() default {};

    /**
     * Flag whether the generated DTO should have Lombok's @SuperBuilder annotation.
     * Use builder = true, if you want to use the Builder Pattern with the generated DTOs.
     * Default is false.
     * @return true if the generated DTO should include @SuperBuilder annotation
     */
    boolean builder() default false;
} 