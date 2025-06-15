package io.github.soulcodingmatt.equilibrium.annotations.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a Record class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreRecord or @IgnoreAll.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateRecord {
    /**
     * The package where the Record should be generated.
     * If not specified, the global package configuration from pom.xml will be used.
     * @return the target package name
     */
    String pkg() default "";

    /**
     * The postfix to be added to the generated class name.
     * Default is "Record".
     * @return the postfix for the generated class
     */
    String postfix() default "Record";
}
