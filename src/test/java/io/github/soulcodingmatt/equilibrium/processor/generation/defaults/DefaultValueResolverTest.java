package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Locks in {@link DefaultValueResolver} behavior after splitting implementation into subtypes. */
class DefaultValueResolverTest {

  @BeforeEach
  void resetHarness() {
    DefaultValueResolverHarnessProcessor.reset();
  }

  @Nested
  class PureTypeClassification {

    @Test
    void collectionTypes_detectListSetMap_simpleAndQualified() {
      assertTrue(DefaultValueResolver.isCollectionType("java.util.List<String>"));
      assertTrue(DefaultValueResolver.isCollectionType("List<String>"));
      assertTrue(DefaultValueResolver.isCollectionType("java.util.Set<int[]>"));
      assertTrue(DefaultValueResolver.isCollectionType("java.util.Map<String, Object>"));
      assertFalse(DefaultValueResolver.isCollectionType("java.lang.String"));
    }

    @Test
    void optionalTypes_detectOptional() {
      assertTrue(DefaultValueResolver.isOptionalType("java.util.Optional<String>"));
      assertTrue(DefaultValueResolver.isOptionalType("Optional<String>"));
      assertFalse(DefaultValueResolver.isOptionalType("java.lang.String"));
    }

    @Test
    void stringTypes_detectString() {
      assertTrue(DefaultValueResolver.isStringType("java.lang.String"));
      assertTrue(DefaultValueResolver.isStringType("String"));
      assertFalse(DefaultValueResolver.isStringType("java.lang.CharSequence"));
    }

    @Test
    void primitiveTypes_matchExpectedSet() {
      assertTrue(DefaultValueResolver.isPrimitiveType("int"));
      assertTrue(DefaultValueResolver.isPrimitiveType("char"));
      assertTrue(DefaultValueResolver.isPrimitiveType("double"));
      assertFalse(DefaultValueResolver.isPrimitiveType("java.lang.Integer"));
    }

    @Test
    void wrapperTypes_matchExpectedSet() {
      assertTrue(DefaultValueResolver.isWrapperType("java.lang.Integer"));
      assertTrue(DefaultValueResolver.isWrapperType("Integer"));
      assertFalse(DefaultValueResolver.isWrapperType("int"));
    }

    @Test
    void collectionDefaults_matchKnownImplementations() {
      assertEquals(
          "new java.util.ArrayList<>()", DefaultValueResolver.getCollectionDefaultValue("List<X>"));
      assertEquals(
          "new java.util.HashSet<>()", DefaultValueResolver.getCollectionDefaultValue("Set<X>"));
      assertEquals(
          "new java.util.HashMap<>()", DefaultValueResolver.getCollectionDefaultValue("Map<A,B>"));
      assertNull(DefaultValueResolver.getCollectionDefaultValue("String"));
    }

    @Test
    void processStringValue_addsQuotesAndEscapes() {
      assertEquals("\"hello\"", DefaultValueResolver.processStringValue("hello"));
      assertEquals("\"hi\"", DefaultValueResolver.processStringValue("hi"));
      assertEquals("\"a \\\"b\\\" c\"", DefaultValueResolver.processStringValue("a \"b\" c"));
      assertEquals("\"x\"", DefaultValueResolver.processStringValue("\"x\""));
    }
  }

  @Nested
  class AnnotationProcessorHarness {

    @Test
    void getTypeSpecificValue_coversPrimitivesWrappersStringAndEnum() {
      compileProbe();

      assertEquals("42", DefaultValueResolverHarnessProcessor.typeSpecificInt);
      assertEquals("100L", DefaultValueResolverHarnessProcessor.typeSpecificLong);
      assertEquals("3", DefaultValueResolverHarnessProcessor.typeSpecificShort);
      assertEquals("2", DefaultValueResolverHarnessProcessor.typeSpecificByte);
      assertEquals("1.5f", DefaultValueResolverHarnessProcessor.typeSpecificFloat);
      assertEquals("2.5", DefaultValueResolverHarnessProcessor.typeSpecificDouble);
      assertEquals("true", DefaultValueResolverHarnessProcessor.typeSpecificBooleanTrue);
      assertNull(DefaultValueResolverHarnessProcessor.typeSpecificBooleanFalse);
      assertEquals("'Z'", DefaultValueResolverHarnessProcessor.typeSpecificChar);
      assertEquals("9", DefaultValueResolverHarnessProcessor.typeSpecificIntegerWrapper);
      assertEquals("\"hello\"", DefaultValueResolverHarnessProcessor.typeSpecificString);
      assertEquals("Hue.A", DefaultValueResolverHarnessProcessor.typeSpecificEnum);
    }

    @Test
    void processAnnotationValue_parsesLegacyValue_forPrimitiveAndWrapper() {
      compileProbe();

      assertEquals("99", DefaultValueResolverHarnessProcessor.legacyValuePrimitiveInt);
      assertEquals("7", DefaultValueResolverHarnessProcessor.legacyValueIntegerWrapper);
    }

    @Test
    void hasAnyOtherParameterSet_and_hasExplicitDtoDefault_followAnnotationModel() {
      compileProbe();

      assertFalse(DefaultValueResolverHarnessProcessor.hasOtherParamsEmptyDtoDefault);
      assertTrue(DefaultValueResolverHarnessProcessor.hasOtherParamsWithString);
      assertFalse(DefaultValueResolverHarnessProcessor.hasExplicitEmptyOnPlainInt);
      assertTrue(DefaultValueResolverHarnessProcessor.hasExplicitEmptyOnList);
      assertTrue(DefaultValueResolverHarnessProcessor.hasExplicitEmptyOnOptional);
    }
  }

  private static void compileProbe() {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new DefaultValueResolverHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.probe.DefaultValueProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
                    import java.util.List;
                    import java.util.Optional;

                    public class DefaultValueProbe {

                      enum Hue {
                        A,
                        B
                      }

                      @DtoBuilderDefault(intValue = 42)
                      int intVal;

                      @DtoBuilderDefault(longValue = 100L)
                      long longVal;

                      @DtoBuilderDefault(shortValue = 3)
                      short shortVal;

                      @DtoBuilderDefault(byteValue = 2)
                      byte byteVal;

                      @DtoBuilderDefault(floatValue = 1.5f)
                      float floatVal;

                      @DtoBuilderDefault(doubleValue = 2.5)
                      double doubleVal;

                      @DtoBuilderDefault(booleanValue = true)
                      boolean boolTrue;

                      @DtoBuilderDefault(booleanValue = false)
                      boolean boolFalse;

                      @DtoBuilderDefault(charValue = 'Z')
                      char charVal;

                      @DtoBuilderDefault(intValue = 9)
                      Integer integerWrapper;

                      @DtoBuilderDefault(stringValue = "hello")
                      String strVal;

                      @DtoBuilderDefault(enumValue = "A")
                      Hue hueVal;

                      @DtoBuilderDefault(value = "99")
                      int legacyPrimitiveInt;

                      @DtoBuilderDefault(value = "7")
                      Integer legacyInteger;

                      @DtoBuilderDefault
                      String emptyDtoDefault;

                      @DtoBuilderDefault
                      int plainInt;

                      @DtoBuilderDefault
                      List<String> emptyList;

                      @DtoBuilderDefault
                      Optional<String> emptyOpt;

                      @DtoBuilderDefault(stringValue = "x")
                      String withString;
                    }
                    """));
    assertThat(compilation).succeeded();
  }
}
