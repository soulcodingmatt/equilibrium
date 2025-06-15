package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a Value Object class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreRecord or @IgnoreAll.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateVo {
    /**
     * The package where the Value Object should be generated.
     * If not specified, the global package configuration from pom.xml will be used.
     * @return the target package name
     */
    String pkg() default "";

    /**
     * The postfix to be added to the generated class name.
     * Default is "Vo".
     * @return the postfix for the generated class
     */
    String postfix() default "Vo";

    /**
     * The ID field of the base class. It will not be generated in the Value Object.
     * Usually Value Objects don't have an identity, ID fields are not mirrored from
     * the base class. If this option isn't enough, use @IgnoreVo to exclude further fields.
     * @return the name of the ID field
     */
    String id()  default "";

    /**
     * Flags whether a Value Object should have a setter or not.
     * Usually Value Objects DO NOT have setters. But in case you really need your
     * Value Object to have a setter, you can use this option.
     * Defaults to {@code false}.
     * @return
     */
    boolean setter() default false;

    /**
     * Whether to generate standard method overrides for the value object,
     * including {@code equals()}, {@code hashCode()}, and {@code toString()}.
     * <p>
     * Defaults to {@code true}.
     * @return {@code true} to include standard overrides; {@code false} otherwise
     */
    boolean overrides() default true;
}
