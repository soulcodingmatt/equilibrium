package org.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be ignored when generating the corresponding Record class.
 * Fields annotated with this annotation will not be included in the generated Record.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreVo {
}
