package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be ignored when generating the corresponding Value Object class.
 * Fields annotated with this annotation will not be included in the generated VO.
 * 
 * When multiple @GenerateVo annotations are used, you can selectively ignore fields
 * for specific VOs by using the ids parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreVo {
    /**
     * Optional list of @GenerateVo IDs for which this field should be ignored.
     * If not specified, the field will be ignored for ALL generated VOs.
     * If specified, the field will only be ignored for VOs whose @GenerateVo annotation
     * has an ID that is present in this list.
     * 
     * @return array of VO generation IDs for which to ignore this field
     */
    int[] ids() default {};
}
