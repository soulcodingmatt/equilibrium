package io.github.soulcodingmatt.equilibrium.annotations.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be ignored when generating the corresponding Record class.
 * Fields annotated with this annotation will not be included in the generated Record.
 * 
 * When multiple @GenerateRecord annotations are used, you can selectively ignore fields
 * for specific Records by using the ids parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreRecord {
    /**
     * Optional list of @GenerateRecord IDs for which this field should be ignored.
     * If not specified, the field will be ignored for ALL generated Records.
     * If specified, the field will only be ignored for Records whose @GenerateRecord annotation
     * has an ID that is present in this list.
     * 
     * @return array of Record generation IDs for which to ignore this field
     */
    int[] ids() default {};
}
