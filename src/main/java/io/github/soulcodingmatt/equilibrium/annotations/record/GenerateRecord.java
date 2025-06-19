package io.github.soulcodingmatt.equilibrium.annotations.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate a Record class from the annotated class.
 * The generated class will mirror the structure of the annotated class,
 * excluding any fields marked with @IgnoreRecord or @IgnoreAll.
 * 
 * This annotation can be used multiple times on the same class to generate
 * multiple Records with different configurations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GenerateRecords.class)
public @interface GenerateRecord {
    /**
     * Optional ID for this Record generation annotation.
     * Used with @IgnoreRecord(ids={...}) to selectively ignore fields for specific Records.
     * When multiple @GenerateRecord annotations are used, each must have a unique ID if specified.
     * @return the ID for this Record generation
     */
    int id() default -1;

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

    /**
     * The fields of the base class to ignore. They will not be generated in the Record.
     * If this option isn't enough, use @IgnoreRecord to exclude further fields.
     * @return the names of the fields to ignore
     */
    String[] ignore() default {};
}
