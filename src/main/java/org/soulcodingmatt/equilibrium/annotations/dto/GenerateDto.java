package org.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a DTO class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreDto or @IgnoreAll.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateDto {
    /**
     * The package where the DTO should be generated.
     * If not specified, the global package configuration from pom.xml will be used.
     * @return the target package name
     */
    String packageName() default "";

    /**
     * The postfix to be added to the generated class name.
     * Default is "Dto".
     * @return the postfix for the generated class
     */
    String postfix() default "Dto";

    /**
     * Flag whether the generated DTO should have Lombok's @SuperBuilder annotation.
     * Use builder = true, if you want to use the Builder Pattern with the generated DTOs.
     * Default is false.
     * @return
     */
    boolean builder() default false;
} 