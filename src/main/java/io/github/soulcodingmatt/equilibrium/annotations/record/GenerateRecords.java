package io.github.soulcodingmatt.equilibrium.annotations.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple @GenerateRecord annotations.
 * This allows using @GenerateRecord multiple times on the same class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateRecords {
    GenerateRecord[] value();
} 