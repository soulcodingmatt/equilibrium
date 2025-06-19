package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple @GenerateVo annotations.
 * This allows using @GenerateVo multiple times on the same class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateVos {
    GenerateVo[] value();
} 