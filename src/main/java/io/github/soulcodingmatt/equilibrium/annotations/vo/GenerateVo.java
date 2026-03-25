package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.*;

/**
 * Generates an immutable Value Object class from the annotated source class. The generated class
 * has {@code private final} fields, a constructor, getters, and {@code equals}/{@code hashCode}/
 * {@code toString} — excluding any fields marked with {@code @IgnoreVo} or {@code @IgnoreAll}.
 *
 * <p>This annotation is repeatable — use it multiple times to generate several Value Objects with
 * different configurations from the same source class.
 *
 * <h3>Examples</h3>
 *
 * <p>Basic usage:
 *
 * <pre>{@code
 * @GenerateVo
 * public class Money {
 *   private String currency;
 *   private BigDecimal amount;
 * }
 * // generates: immutable MoneyVo with constructor, getters, equals, hashCode, toString
 * }</pre>
 *
 * <p>Excluding identity fields (typical for Value Objects):
 *
 * <pre>{@code
 * @GenerateVo(pkg = "com.example.domain", name = "Address", ignore = {"id"})
 * public class AddressEntity { ... }
 * }</pre>
 *
 * <p>With setters (opt-in, not the default for Value Objects):
 *
 * <pre>{@code
 * @GenerateVo(setters = true)
 * public class Coordinate { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GenerateVos.class)
public @interface GenerateVo {
  /**
   * Optional ID for this VO generation annotation. Used with @IgnoreVo(ids={...}) to selectively
   * ignore fields for specific VOs. When multiple @GenerateVo annotations are used, each must have
   * a unique ID if specified.
   *
   * @return the ID for this VO generation
   */
  int id() default -1;

  /**
   * The package where the Value Object should be generated. If not specified, the global package
   * configuration from pom.xml will be used.
   *
   * @return the target package name
   */
  String pkg() default "";

  /**
   * The complete name for the generated Value Object class. If specified, this exact name will be
   * used without any postfix. If not specified, the class name will be constructed using the
   * original class name plus a postfix (determined by compiler arguments or the default "Vo").
   *
   * @return the complete name for the generated class
   */
  String name() default "";

  /**
   * The fields of the base class to ignore. They will not be generated in the Value Object. Usually
   * Value Objects don't have an identity, so ID fields are often ignored. If this option isn't
   * enough, use @IgnoreVo to exclude further fields.
   *
   * @return the names of the fields to ignore
   */
  String[] ignore() default {};

  /**
   * Flags whether a Value Object should have setters or not. Usually Value Objects DO NOT have
   * setters. But in case you really need your Value Object to have setters, you can use this
   * option. Defaults to {@code false}.
   *
   * @return {@code true} if setters should be generated; {@code false} otherwise
   */
  boolean setters() default false;
}
