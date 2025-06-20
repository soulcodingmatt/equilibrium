package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a Value Object class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreVo or @IgnoreAll.
 * 
 * This annotation can be used multiple times on the same class to generate
 * multiple VOs with different configurations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GenerateVos.class)
public @interface GenerateVo {
    /**
     * Optional ID for this VO generation annotation.
     * Used with @IgnoreVo(ids={...}) to selectively ignore fields for specific VOs.
     * When multiple @GenerateVo annotations are used, each must have a unique ID if specified.
     * @return the ID for this VO generation
     */
    int id() default -1;

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
     * The fields of the base class to ignore. They will not be generated in the Value Object.
     * Usually Value Objects don't have an identity, so ID fields are often ignored.
     * If this option isn't enough, use @IgnoreVo to exclude further fields.
     * @return the names of the fields to ignore
     */
    String[] ignore() default {};

    /**
     * Flags whether a Value Object should have setters or not.
     * Usually Value Objects DO NOT have setters. But in case you really need your
     * Value Object to have setters, you can use this option.
     * Defaults to {@code false}.
     * @return {@code true} if setters should be generated; {@code false} otherwise
     */
    boolean setters() default false;
}
