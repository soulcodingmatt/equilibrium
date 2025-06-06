package org.soulcodingmatt.equilibrium.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be ignored in all generated classes.
 * Fields annotated with this annotation will not be included in any generated class
 * (DTO, Entity, Record, VO, etc.).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreAll {
} 