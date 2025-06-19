package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple @GenerateDto annotations.
 * This allows using @GenerateDto multiple times on the same class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateDtos {
    GenerateDto[] value();
} 