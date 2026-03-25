package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.*;

/**
 * Generates a DTO class from the annotated source class. The generated DTO mirrors the field
 * structure, excluding any fields marked with {@code @IgnoreDto} or {@code @IgnoreAll}.
 *
 * <p>This annotation is repeatable — use it multiple times to generate several DTOs with different
 * configurations from the same source class.
 *
 * <p><b>Examples:</b>
 *
 * <p>Basic usage (package and name inferred from configuration):
 *
 * <pre>{@code
 * @GenerateDto
 * public class Person {
 *   private String name;
 *   private int age;
 * }
 * }</pre>
 *
 * <p>Explicit package and name:
 *
 * <pre>{@code
 * @GenerateDto(pkg = "com.example.api", name = "PersonResponse")
 * public class Person { ... }
 * }</pre>
 *
 * <p>Multiple DTOs with selective field exclusion:
 *
 * <pre>{@code
 * @GenerateDto(id = 1, name = "PersonSummary", ignore = {"address", "phone"})
 * @GenerateDto(id = 2, name = "PersonDetail")
 * public class Person { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GenerateDtos.class)
public @interface GenerateDto {
  /**
   * Optional ID for this DTO generation annotation. Used with @IgnoreDto(ids={...}) to selectively
   * ignore fields for specific DTOs. When multiple @GenerateDto annotations are used, each must
   * have a unique ID if specified.
   *
   * @return the ID for this DTO generation
   */
  int id() default -1;

  /**
   * The package where the DTO should be generated. If not specified, the global package
   * configuration from pom.xml will be used.
   *
   * @return the target package name
   */
  String pkg() default "";

  /**
   * The complete name for the generated DTO class. If specified, this exact name will be used
   * without any postfix. If not specified, the class name will be constructed using the original
   * class name plus a postfix (determined by compiler arguments or the default "Dto").
   *
   * @return the complete name for the generated class
   */
  String name() default "";

  /**
   * The fields of the base class to ignore. They will not be generated in the DTO. If this option
   * isn't enough, use @IgnoreDto to exclude further fields.
   *
   * @return the names of the fields to ignore
   */
  String[] ignore() default {};

  /**
   * Flag whether the generated DTO should have Lombok's @SuperBuilder annotation. Use builder =
   * true, if you want to use the Builder Pattern with the generated DTOs. Default is false.
   *
   * @return true if the generated DTO should include @SuperBuilder annotation
   */
  boolean builder() default false;
}
