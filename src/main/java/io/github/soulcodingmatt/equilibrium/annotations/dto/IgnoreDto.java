package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be ignored when generating the corresponding DTO class.
 * Fields annotated with this annotation will not be included in the generated DTO.
 * 
 * When multiple @GenerateDto annotations are used, you can selectively ignore fields
 * for specific DTOs by using the ids parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreDto {
    /**
     * Optional list of @GenerateDto IDs for which this field should be ignored.
     * If not specified, the field will be ignored for ALL generated DTOs.
     * If specified, the field will only be ignored for DTOs whose @GenerateDto annotation
     * has an ID that is present in this list.
     * 
     * @return array of DTO generation IDs for which to ignore this field
     */
    int[] ids() default {};
} 