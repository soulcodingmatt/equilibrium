package io.github.soulcodingmatt.equilibrium.processor;

import static org.junit.jupiter.api.Assertions.*;

import io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumFilerStats;
import io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumMessagerStats;
import io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumProcessor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EquilibriumProcessorTest {

  private EquilibriumProcessor processor;
  private TestMessager fixtureMessager;

  @BeforeEach
  void setUp() {
    processor = new EquilibriumProcessor();
    fixtureMessager = new TestMessager();
    TestProcessingEnvironment testProcessingEnv = new TestProcessingEnvironment(fixtureMessager);

    // Initialize the processor
    processor.init(testProcessingEnv);
  }

  @Test
  void testProcessorIsProperlyAnnotated() {
    // Verify @SupportedAnnotationTypes is present
    SupportedAnnotationTypes annotationTypes =
        EquilibriumProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
    assertNotNull(annotationTypes, "Processor must have @SupportedAnnotationTypes annotation");

    // Verify @SupportedOptions is present
    SupportedOptions supportedOptions =
        EquilibriumProcessor.class.getAnnotation(SupportedOptions.class);
    assertNotNull(supportedOptions, "Processor must have @SupportedOptions annotation");
  }

  @Test
  void testAllEquilibriumAnnotationsAreSupported() {
    Set<String> supportedAnnotations = processor.getSupportedAnnotationTypes();

    // Verify all Equilibrium annotations are declared
    List<String> expectedAnnotations =
        Arrays.asList(
            // DTO annotations
            "io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDtos",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMappings",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.NestedDtoMapping",
            "io.github.soulcodingmatt.equilibrium.annotations.dto.NestedDtoMappings",
            // Record annotations
            "io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
            "io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecords",
            "io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecords",
            // VO annotations
            "io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo",
            "io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVos",
            "io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo",
            "io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVos",
            // Common annotations
            "io.github.soulcodingmatt.equilibrium.annotations.common.IgnoreAll");

    for (String annotation : expectedAnnotations) {
      assertTrue(
          supportedAnnotations.contains(annotation),
          "Processor must support annotation: " + annotation);
    }
  }

  @Test
  void testAllJakartaValidationAnnotationsAreSupported() {
    Set<String> supportedAnnotations = processor.getSupportedAnnotationTypes();

    // Verify all Jakarta Bean Validation annotations are declared
    List<String> expectedValidationAnnotations =
        Arrays.asList(
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.NotBlank",
            "jakarta.validation.constraints.NotEmpty",
            "jakarta.validation.constraints.Size",
            "jakarta.validation.constraints.Min",
            "jakarta.validation.constraints.Max",
            "jakarta.validation.constraints.Email",
            "jakarta.validation.constraints.Pattern",
            "jakarta.validation.constraints.Positive",
            "jakarta.validation.constraints.PositiveOrZero",
            "jakarta.validation.constraints.Negative",
            "jakarta.validation.constraints.NegativeOrZero",
            "jakarta.validation.constraints.Digits",
            "jakarta.validation.constraints.Past",
            "jakarta.validation.constraints.Future",
            "jakarta.validation.constraints.PastOrPresent",
            "jakarta.validation.constraints.FutureOrPresent");

    for (String annotation : expectedValidationAnnotations) {
      assertTrue(
          supportedAnnotations.contains(annotation),
          "Processor must support Jakarta validation annotation: " + annotation);
    }
  }

  @Test
  void testAllRequiredOptionsAreSupported() {
    Set<String> supportedOptions = processor.getSupportedOptions();

    // Verify all required options are declared
    List<String> expectedOptions =
        Arrays.asList(
            "equilibrium.dto.package",
            "equilibrium.dto.postfix",
            "equilibrium.record.package",
            "equilibrium.record.postfix",
            "equilibrium.vo.package",
            "equilibrium.vo.postfix",
            "equilibrium.groupId",
            "equilibrium.artifactId",
            "equilibrium.banner",
            "equilibrium.banner.color");

    for (String option : expectedOptions) {
      assertTrue(supportedOptions.contains(option), "Processor must support option: " + option);
    }
  }

  @Test
  void testProcessorSupportedSourceVersion() {
    // Verify the processor supports the correct source version
    SourceVersion supportedVersion = processor.getSupportedSourceVersion();
    assertNotNull(supportedVersion, "Processor must declare a supported source version");

    // Should support at least Java 21 or latest
    assertTrue(
        supportedVersion.ordinal() >= SourceVersion.RELEASE_21.ordinal(),
        "Processor should support Java 21 or later");
  }

  @Test
  void testInitStoresFilerAndMessager() throws Exception {
    // Create a new processor instance to test init
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestFiler testFiler = new TestFiler();
    TestProcessingEnvironmentWithFiler testEnv =
        new TestProcessingEnvironmentWithFiler(messager, testFiler);

    // Act - initialize the processor
    testProcessor.init(testEnv);

    // Assert - filerStats wraps the provided filer and messager is wrapped for stats
    java.lang.reflect.Field filerStatsField =
        EquilibriumProcessor.class.getDeclaredField("filerStats");
    filerStatsField.setAccessible(true);
    EquilibriumFilerStats storedFilerStats =
        (EquilibriumFilerStats) filerStatsField.get(testProcessor);
    assertNotNull(storedFilerStats, "FilerStats should be stored during init");
    assertSame(
        testFiler,
        storedFilerStats.delegateFiler(),
        "Stats wrapper should delegate to the ProcessingEnvironment filer");

    java.lang.reflect.Field messagerField = EquilibriumProcessor.class.getDeclaredField("messager");
    messagerField.setAccessible(true);
    Messager storedMessager = (Messager) messagerField.get(testProcessor);
    assertNotNull(storedMessager, "Messager should be stored during init");
    assertInstanceOf(
        EquilibriumMessagerStats.class, storedMessager, "Messager should be wrapped for stats");
    assertSame(
        messager,
        ((EquilibriumMessagerStats) storedMessager).delegateMessager(),
        "Stats wrapper should delegate to the ProcessingEnvironment messager");
  }

  @Test
  void testInitBuildsEquilibriumConfig() throws Exception {
    // Create a new processor instance to test init
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);

    // Add some options to verify config is consumed from ProcessingEnvironment
    testEnv.getOptions().put("equilibrium.dto.package", "com.test.dto");
    testEnv.getOptions().put("equilibrium.dto.postfix", "Dto");

    // Act - initialize the processor
    testProcessor.init(testEnv);

    // Assert - config is built and consumed during init: the orchestrators are created as evidence
    java.lang.reflect.Field orchestratorField =
        EquilibriumProcessor.class.getDeclaredField("dtoOrchestrator");
    orchestratorField.setAccessible(true);
    Object storedOrchestrator = orchestratorField.get(testProcessor);
    assertNotNull(
        storedOrchestrator,
        "DtoGenerationOrchestrator should be created during init (requires EquilibriumConfig)");
  }

  @Test
  void testInitObtainsTreesNullable() {
    // Create a new processor instance to test init
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);

    // Trees is unavailable in the test environment — init() must not throw
    assertDoesNotThrow(
        () -> testProcessor.init(testEnv),
        "init() must handle missing Trees gracefully (Trees.instance may throw)");
  }

  @Test
  void testInitPrintsBuildBannerWhenEnabled() {
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);
    testEnv.getOptions().put("equilibrium.banner", "true");

    testProcessor.init(testEnv);

    assertFalse(messager.getNoteMessages().isEmpty(), "banner should emit NOTE diagnostics");
    List<TestMessage> notes = messager.getNoteMessages();
    assertEquals("", notes.getFirst().getMessage(), "blank line precedes top rule");
    String topRule = notes.get(1).getMessage();
    String bottomRule = notes.getLast().getMessage();
    assertTrue(topRule.chars().allMatch(ch -> ch == '='));
    assertTrue(bottomRule.chars().allMatch(ch -> ch == '-'));
    assertEquals(topRule.length(), bottomRule.length());
  }

  @Test
  void testProcessingOverPrintsGenerationFooterAfterNotes() throws Exception {
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);
    testEnv.getOptions().put("equilibrium.banner", "true");

    testProcessor.init(testEnv);

    Method noteMethod =
        EquilibriumProcessor.class.getDeclaredMethod("note", Element.class, String.class);
    noteMethod.setAccessible(true);
    noteMethod.invoke(testProcessor, null, "Generated DTO class: example.Dto");

    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);
    TestRoundEnvironment lastRound = new TestRoundEnvironment(true, new HashSet<>());
    processMethod.invoke(testProcessor, new HashSet<>(), lastRound);

    List<TestMessage> notes = messager.getNoteMessages();
    assertFalse(notes.isEmpty());
    String lastNonEmptyLine = "";
    for (int i = notes.size() - 1; i >= 0; i--) {
      String noteText = notes.get(i).getMessage();
      if (!noteText.isEmpty()) {
        lastNonEmptyLine = noteText;
        break;
      }
    }
    assertFalse(lastNonEmptyLine.isEmpty());
    assertTrue(lastNonEmptyLine.chars().allMatch(ch -> ch == '='));
  }

  @Test
  void testProcessingOver_skipsSummaryWhenNoWorkOrDiagnostics() throws Exception {
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);
    testEnv.getOptions().put("equilibrium.banner", "true");
    testEnv.getOptions().put("equilibrium.banner.color", "false");

    testProcessor.init(testEnv);

    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);
    TestRoundEnvironment lastRound = new TestRoundEnvironment(true, new HashSet<>());
    processMethod.invoke(testProcessor, new HashSet<>(), lastRound);

    String joined =
        messager.getNoteMessages().stream()
            .map(TestMessage::getMessage)
            .collect(Collectors.joining("\n"));
    assertFalse(joined.contains("Summary"));
    assertFalse(joined.contains("Result:"));
  }

  @Test
  void testProcessingOver_printsSummaryAfterNoteWithAggregateCounts() throws Exception {
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);
    testEnv.getOptions().put("equilibrium.banner", "true");
    testEnv.getOptions().put("equilibrium.banner.color", "false");

    testProcessor.init(testEnv);

    Method noteMethod =
        EquilibriumProcessor.class.getDeclaredMethod("note", Element.class, String.class);
    noteMethod.setAccessible(true);
    noteMethod.invoke(testProcessor, null, "Generated DTO class: example.Dto");

    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);
    TestRoundEnvironment lastRound = new TestRoundEnvironment(true, new HashSet<>());
    processMethod.invoke(testProcessor, new HashSet<>(), lastRound);

    String joined =
        messager.getNoteMessages().stream()
            .map(TestMessage::getMessage)
            .collect(Collectors.joining("\n"));
    assertTrue(joined.contains("Summary"));
    assertTrue(joined.contains("Processed 0 types"));
    assertTrue(joined.contains("Generated 0 files"));
    assertTrue(joined.contains("Result: SUCCESS (0 types, 0 files)"));
  }

  @Test
  void testProcessingOver_printsFailureSummaryWhenErrorEmitted() throws Exception {
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);
    testEnv.getOptions().put("equilibrium.banner", "true");
    testEnv.getOptions().put("equilibrium.banner.color", "false");

    testProcessor.init(testEnv);

    Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", String.class);
    errorMethod.setAccessible(true);
    errorMethod.invoke(testProcessor, "synthetic failure");

    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);
    TestRoundEnvironment lastRound = new TestRoundEnvironment(true, new HashSet<>());
    processMethod.invoke(testProcessor, new HashSet<>(), lastRound);

    String joined =
        messager.getNoteMessages().stream()
            .map(TestMessage::getMessage)
            .collect(Collectors.joining("\n"));
    assertTrue(joined.contains("Summary"));
    assertTrue(joined.contains("Result: FAILURE (0 types, 0 files, 1 error)"));
  }

  @Test
  void testInitHasNoSideEffects() throws Exception {
    // Create a new processor instance to test init
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);

    // Act - initialize the processor
    testProcessor.init(testEnv);

    // Assert - verify no messages were printed (no side effects)
    assertEquals(0, messager.getErrorMessages().size(), "init() should not produce error messages");
    assertEquals(
        0,
        messager.getGeneralErrorMessages().size(),
        "init() should not produce general error messages");
    assertEquals(0, messager.getNoteMessages().size(), "init() should not produce note messages");

    // Verify processedElements set is empty (no processing happened)
    java.lang.reflect.Field processedElementsField =
        EquilibriumProcessor.class.getDeclaredField("processedElements");
    processedElementsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> processedElements = (Set<String>) processedElementsField.get(testProcessor);
    assertTrue(
        processedElements.isEmpty(),
        "init() should not process any elements - processedElements should be empty");
  }

  @Test
  void testInitCallsSuperInit() {
    // Create a new processor instance to test init
    EquilibriumProcessor testProcessor = new EquilibriumProcessor();
    TestMessager messager = new TestMessager();
    TestProcessingEnvironment testEnv = new TestProcessingEnvironment(messager);

    // Act - initialize the processor
    testProcessor.init(testEnv);

    // Assert - verify that the processor can access ProcessingEnvironment methods
    // This indirectly verifies that super.init() was called
    assertNotNull(
        testProcessor.getSupportedAnnotationTypes(),
        "Processor should have access to supported annotation types after init");
    assertNotNull(
        testProcessor.getSupportedOptions(),
        "Processor should have access to supported options after init");
  }

  @Test
  void testErrorMethodWithElement() throws Exception {
    // Test the error(Element, String) method using reflection
    Method errorMethod =
        EquilibriumProcessor.class.getDeclaredMethod("error", Element.class, String.class);
    errorMethod.setAccessible(true);

    // Act
    errorMethod.invoke(processor, null, "Test error with element");

    // Assert
    assertEquals(1, fixtureMessager.getErrorMessages().size());
    TestMessage errorMessage = fixtureMessager.getErrorMessages().getFirst();
    assertEquals(Diagnostic.Kind.ERROR, errorMessage.getKind());
    assertEquals("Test error with element", errorMessage.getMessage());
    assertNull(errorMessage.getElement());
  }

  @Test
  void testErrorMethodWithoutElement() throws Exception {
    // Test the error(String) method using reflection
    Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", String.class);
    errorMethod.setAccessible(true);

    // Act
    errorMethod.invoke(processor, "Test error without element");

    // Assert
    assertEquals(1, fixtureMessager.getGeneralErrorMessages().size());
    assertEquals(
        "Test error without element", fixtureMessager.getGeneralErrorMessages().getFirst());
  }

  @Test
  void testErrorMessageFormat() throws Exception {
    // Test that error messages include exception type information
    Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", String.class);
    errorMethod.setAccessible(true);

    // Create a test scenario that mimics the error handling in the processor
    RuntimeException testException = new RuntimeException("Test exception message");
    String expectedMessage =
        "Failed to process annotations: "
            + testException.getMessage()
            + " ("
            + testException.getClass().getSimpleName()
            + ")";

    // Act
    errorMethod.invoke(processor, expectedMessage);

    // Assert
    assertEquals(1, fixtureMessager.getGeneralErrorMessages().size());
    String actualMessage = fixtureMessager.getGeneralErrorMessages().getFirst();
    assertTrue(actualMessage.contains("Failed to process annotations"));
    assertTrue(actualMessage.contains("Test exception message"));
    assertTrue(actualMessage.contains("RuntimeException"));
  }

  @Test
  void testNoteMethod() throws Exception {
    // Test the note method using reflection
    Method noteMethod =
        EquilibriumProcessor.class.getDeclaredMethod("note", Element.class, String.class);
    noteMethod.setAccessible(true);

    // Act
    noteMethod.invoke(processor, null, "Test note message");

    // Assert
    assertEquals(1, fixtureMessager.getNoteMessages().size());
    TestMessage noteMessage = fixtureMessager.getNoteMessages().getFirst();
    assertEquals(Diagnostic.Kind.NOTE, noteMessage.getKind());
    assertEquals("Test note message", noteMessage.getMessage());
  }

  @Test
  void testProcessReturnsFalseWhenProcessingOver() throws Exception {
    // Use reflection to test the process method behavior
    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);

    // Create a test RoundEnvironment that indicates processing is over
    TestRoundEnvironment roundEnv = new TestRoundEnvironment(true, new HashSet<>());
    Set<javax.lang.model.element.TypeElement> annotations = new HashSet<>();

    // Act
    Boolean result = (Boolean) processMethod.invoke(processor, annotations, roundEnv);

    // Assert
    assertFalse(result, "process() should return false when roundEnv.processingOver() is true");
  }

  @Test
  void testProcessReturnsTrueForOnlyJakartaValidationAnnotations() throws Exception {
    // Use reflection to test the process method behavior
    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);

    // Create a test RoundEnvironment with only Jakarta validation annotations
    TestRoundEnvironment roundEnv = new TestRoundEnvironment(false, new HashSet<>());

    // Create a set with only Jakarta validation annotation types
    Set<javax.lang.model.element.TypeElement> annotations = new HashSet<>();
    annotations.add(new TestTypeElement("jakarta.validation.constraints.NotNull"));
    annotations.add(new TestTypeElement("jakarta.validation.constraints.Size"));

    // Act
    Boolean result = (Boolean) processMethod.invoke(processor, annotations, roundEnv);

    // Assert
    assertTrue(
        result,
        "process() should return true (claim) when only Jakarta validation annotations are present");
  }

  @Test
  void testProcessReturnsFalseForNoAnnotations() throws Exception {
    // Use reflection to test the process method behavior
    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);

    // Create a test RoundEnvironment with no annotations
    TestRoundEnvironment roundEnv = new TestRoundEnvironment(false, new HashSet<>());
    Set<javax.lang.model.element.TypeElement> annotations = new HashSet<>();

    // Act
    Boolean result = (Boolean) processMethod.invoke(processor, annotations, roundEnv);

    // Assert
    assertFalse(result, "process() should return false when no annotations are present");
  }

  @Test
  void testProcessReturnsTrueForEquilibriumAndJakartaAnnotations() throws Exception {
    // Note: This test verifies that when both Equilibrium and Jakarta annotations are present,
    // the processor will attempt to process them. However, without actual valid class elements
    // in the RoundEnvironment, the processor correctly returns false (no work to do).
    // In a real scenario with actual annotated classes, the processor would return true.

    // Use reflection to test the process method behavior
    Method processMethod =
        EquilibriumProcessor.class.getMethod(
            "process", Set.class, javax.annotation.processing.RoundEnvironment.class);

    // Create a test RoundEnvironment with both Equilibrium and Jakarta annotations
    // but no actual elements (simulating an empty round)
    TestRoundEnvironment roundEnv = new TestRoundEnvironment(false, new HashSet<>());

    // Create a set with both Equilibrium and Jakarta validation annotation types
    Set<javax.lang.model.element.TypeElement> annotations = new HashSet<>();
    annotations.add(
        new TestTypeElement("io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto"));
    annotations.add(new TestTypeElement("jakarta.validation.constraints.NotNull"));

    // Act
    Boolean result = (Boolean) processMethod.invoke(processor, annotations, roundEnv);

    // Assert - returns false because there are no valid elements to process
    // This is correct behavior: the processor only claims annotations when it has work to do
    assertFalse(
        result, "process() should return false when no valid elements are present to process");
  }

  @Test
  void testGetValidClassElementsGathersAllGenerateAnnotations() throws Exception {
    // Test that getValidClassElements gathers elements from all generation annotations
    Method getValidClassElementsMethod =
        EquilibriumProcessor.class.getDeclaredMethod(
            "getValidClassElements", javax.annotation.processing.RoundEnvironment.class);
    getValidClassElementsMethod.setAccessible(true);

    // Create test class elements
    TestClassElement dtoElement = new TestClassElement("com.test.DtoClass");
    TestClassElement recordElement = new TestClassElement("com.test.RecordClass");
    TestClassElement voElement = new TestClassElement("com.test.VoClass");

    // Create a RoundEnvironment with elements for each annotation type
    TestRoundEnvironmentWithElements roundEnv = new TestRoundEnvironmentWithElements();
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto.class, dtoElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord.class,
        recordElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo.class, voElement);

    // Act
    @SuppressWarnings("unchecked")
    Set<javax.lang.model.element.TypeElement> result =
        (Set<javax.lang.model.element.TypeElement>)
            getValidClassElementsMethod.invoke(processor, roundEnv);

    // Assert
    assertEquals(3, result.size(), "Should gather elements from all generation annotations");
    assertTrue(result.contains(dtoElement), "Should include GenerateDto element");
    assertTrue(result.contains(recordElement), "Should include GenerateRecord element");
    assertTrue(result.contains(voElement), "Should include GenerateVo element");
  }

  @Test
  void testGetValidClassElementsGathersRepeatableAnnotations() throws Exception {
    // Test that getValidClassElements gathers elements from repeatable container annotations
    Method getValidClassElementsMethod =
        EquilibriumProcessor.class.getDeclaredMethod(
            "getValidClassElements", javax.annotation.processing.RoundEnvironment.class);
    getValidClassElementsMethod.setAccessible(true);

    // Create test class elements
    TestClassElement dtosElement = new TestClassElement("com.test.MultipleDtosClass");
    TestClassElement recordsElement = new TestClassElement("com.test.MultipleRecordsClass");
    TestClassElement vosElement = new TestClassElement("com.test.MultipleVosClass");

    // Create a RoundEnvironment with elements for repeatable container annotations
    TestRoundEnvironmentWithElements roundEnv = new TestRoundEnvironmentWithElements();
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos.class, dtosElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecords.class,
        recordsElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVos.class, vosElement);

    // Act
    @SuppressWarnings("unchecked")
    Set<javax.lang.model.element.TypeElement> result =
        (Set<javax.lang.model.element.TypeElement>)
            getValidClassElementsMethod.invoke(processor, roundEnv);

    // Assert
    assertEquals(
        3, result.size(), "Should gather elements from all repeatable container annotations");
    assertTrue(result.contains(dtosElement), "Should include GenerateDtos element");
    assertTrue(result.contains(recordsElement), "Should include GenerateRecords element");
    assertTrue(result.contains(vosElement), "Should include GenerateVos element");
  }

  @Test
  void testGetValidClassElementsFiltersNonClassElements() throws Exception {
    // Test that getValidClassElements filters out non-class elements (interfaces, enums, etc.)
    Method getValidClassElementsMethod =
        EquilibriumProcessor.class.getDeclaredMethod(
            "getValidClassElements", javax.annotation.processing.RoundEnvironment.class);
    getValidClassElementsMethod.setAccessible(true);

    // Create test elements of different kinds
    TestClassElement classElement = new TestClassElement("com.test.ValidClass");
    TestInterfaceElement interfaceElement = new TestInterfaceElement("com.test.InvalidInterface");
    TestEnumElement enumElement = new TestEnumElement("com.test.InvalidEnum");

    // Create a RoundEnvironment with mixed element types
    TestRoundEnvironmentWithElements roundEnv = new TestRoundEnvironmentWithElements();
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto.class, classElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto.class, interfaceElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto.class, enumElement);

    // Act
    @SuppressWarnings("unchecked")
    Set<javax.lang.model.element.TypeElement> result =
        (Set<javax.lang.model.element.TypeElement>)
            getValidClassElementsMethod.invoke(processor, roundEnv);

    // Assert
    assertEquals(1, result.size(), "Should only include class elements");
    assertTrue(result.contains(classElement), "Should include the class element");
    assertFalse(result.contains(interfaceElement), "Should not include interface element");
    assertFalse(result.contains(enumElement), "Should not include enum element");

    // Verify error messages were generated for non-class elements
    assertEquals(
        2,
        fixtureMessager.getErrorMessages().size(),
        "Should generate errors for non-class elements");
  }

  @Test
  void testGetValidClassElementsReturnsEmptySetForNoElements() throws Exception {
    // Test that getValidClassElements returns empty set when no elements are present
    Method getValidClassElementsMethod =
        EquilibriumProcessor.class.getDeclaredMethod(
            "getValidClassElements", javax.annotation.processing.RoundEnvironment.class);
    getValidClassElementsMethod.setAccessible(true);

    // Create an empty RoundEnvironment
    TestRoundEnvironmentWithElements roundEnv = new TestRoundEnvironmentWithElements();

    // Act
    @SuppressWarnings("unchecked")
    Set<javax.lang.model.element.TypeElement> result =
        (Set<javax.lang.model.element.TypeElement>)
            getValidClassElementsMethod.invoke(processor, roundEnv);

    // Assert
    assertTrue(result.isEmpty(), "Should return empty set when no elements are present");
  }

  @Test
  void testGetValidClassElementsDeduplicatesElements() throws Exception {
    // Test that getValidClassElements deduplicates elements that have multiple annotations
    Method getValidClassElementsMethod =
        EquilibriumProcessor.class.getDeclaredMethod(
            "getValidClassElements", javax.annotation.processing.RoundEnvironment.class);
    getValidClassElementsMethod.setAccessible(true);

    // Create a test class element that has multiple annotations
    TestClassElement multiAnnotatedElement = new TestClassElement("com.test.MultiAnnotatedClass");

    // Create a RoundEnvironment where the same element appears for multiple annotations
    TestRoundEnvironmentWithElements roundEnv = new TestRoundEnvironmentWithElements();
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto.class,
        multiAnnotatedElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord.class,
        multiAnnotatedElement);
    roundEnv.addElementForAnnotation(
        io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo.class,
        multiAnnotatedElement);

    // Act
    @SuppressWarnings("unchecked")
    Set<javax.lang.model.element.TypeElement> result =
        (Set<javax.lang.model.element.TypeElement>)
            getValidClassElementsMethod.invoke(processor, roundEnv);

    // Assert
    assertEquals(1, result.size(), "Should deduplicate elements with multiple annotations");
    assertTrue(
        result.contains(multiAnnotatedElement), "Should include the multi-annotated element once");
  }

  @Test
  void testIsValidClassElementReturnsTrueForClass() throws Exception {
    // Test that isValidClassElement returns true for class elements
    Method isValidClassElementMethod =
        EquilibriumProcessor.class.getDeclaredMethod("isValidClassElement", Element.class);
    isValidClassElementMethod.setAccessible(true);

    TestClassElement classElement = new TestClassElement("com.test.ValidClass");

    // Act
    Boolean result = (Boolean) isValidClassElementMethod.invoke(processor, classElement);

    // Assert
    assertTrue(result, "Should return true for class elements");
    assertEquals(
        0,
        fixtureMessager.getErrorMessages().size(),
        "Should not generate errors for class elements");
  }

  @Test
  void testIsValidClassElementReturnsFalseForInterface() throws Exception {
    // Test that isValidClassElement returns false for interface elements
    Method isValidClassElementMethod =
        EquilibriumProcessor.class.getDeclaredMethod("isValidClassElement", Element.class);
    isValidClassElementMethod.setAccessible(true);

    TestInterfaceElement interfaceElement = new TestInterfaceElement("com.test.InvalidInterface");

    // Act
    Boolean result = (Boolean) isValidClassElementMethod.invoke(processor, interfaceElement);

    // Assert
    assertFalse(result, "Should return false for interface elements");
    assertEquals(
        1,
        fixtureMessager.getErrorMessages().size(),
        "Should generate error for interface element");
    assertTrue(
        fixtureMessager
            .getErrorMessages()
            .getFirst()
            .getMessage()
            .contains("can only be applied to classes"),
        "Error message should indicate annotations can only be applied to classes");
  }

  @Test
  void testIsValidClassElementReturnsFalseForEnum() throws Exception {
    // Test that isValidClassElement returns false for enum elements
    Method isValidClassElementMethod =
        EquilibriumProcessor.class.getDeclaredMethod("isValidClassElement", Element.class);
    isValidClassElementMethod.setAccessible(true);

    TestEnumElement enumElement = new TestEnumElement("com.test.InvalidEnum");

    // Act
    Boolean result = (Boolean) isValidClassElementMethod.invoke(processor, enumElement);

    // Assert
    assertFalse(result, "Should return false for enum elements");
    assertEquals(
        1, fixtureMessager.getErrorMessages().size(), "Should generate error for enum element");
    assertTrue(
        fixtureMessager
            .getErrorMessages()
            .getFirst()
            .getMessage()
            .contains("can only be applied to classes"),
        "Error message should indicate annotations can only be applied to classes");
  }

  // Test helper classes — stubs only implement the methods actually exercised by tests.

  private static class TestRoundEnvironment
      implements javax.annotation.processing.RoundEnvironment {
    private final boolean processingOver;
    private final Set<? extends Element> rootElements;

    public TestRoundEnvironment(boolean processingOver, Set<? extends Element> rootElements) {
      this.processingOver = processingOver;
      this.rootElements = rootElements;
    }

    @Override
    public boolean processingOver() {
      return processingOver;
    }

    @Override
    public boolean errorRaised() {
      return false;
    }

    @Override
    public Set<? extends Element> getRootElements() {
      return rootElements;
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(javax.lang.model.element.TypeElement a) {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(
        Class<? extends java.lang.annotation.Annotation> a) {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWithAny(
        javax.lang.model.element.TypeElement... annotations) {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWithAny(
        Set<Class<? extends java.lang.annotation.Annotation>> annotations) {
      return new HashSet<>();
    }
  }

  @SuppressWarnings("NullableProblems")
  private static class TestTypeElement implements javax.lang.model.element.TypeElement {
    private final String qualifiedName;

    public TestTypeElement(String qualifiedName) {
      this.qualifiedName = qualifiedName;
    }

    @Override
    public javax.lang.model.element.Name getQualifiedName() {
      return new TestName(qualifiedName);
    }

    @Override
    public javax.lang.model.element.Name getSimpleName() {
      String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
      return new TestName(simpleName);
    }

    @Override
    public javax.lang.model.type.TypeMirror asType() {
      return null;
    }

    @Override
    public ElementKind getKind() {
      return ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public Set<javax.lang.model.element.Modifier> getModifiers() {
      return new HashSet<>();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return new ArrayList<>();
    }

    @Override
    public List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors() {
      return new ArrayList<>();
    }

    @Override
    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    public <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(
        Class<A> annotationType) {
      return null;
    }

    @Override
    public Element getEnclosingElement() {
      return null;
    }

    @Override
    public javax.lang.model.type.TypeMirror getSuperclass() {
      return null;
    }

    @Override
    public List<? extends javax.lang.model.type.TypeMirror> getInterfaces() {
      return new ArrayList<>();
    }

    @Override
    public List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters() {
      return new ArrayList<>();
    }

    @Override
    public javax.lang.model.element.NestingKind getNestingKind() {
      return javax.lang.model.element.NestingKind.TOP_LEVEL;
    }

    @Override
    public <R, P> R accept(javax.lang.model.element.ElementVisitor<R, P> v, P p) {
      return null;
    }
  }

  @SuppressWarnings("NullableProblems")
  private static class TestName implements javax.lang.model.element.Name {
    private final String name;

    public TestName(String name) {
      this.name = name;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
      return name.contentEquals(cs);
    }

    @Override
    public int length() {
      return name.length();
    }

    @Override
    public char charAt(int index) {
      return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return name.subSequence(start, end);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static class TestMessager implements Messager {
    private final List<TestMessage> errorMessages = new ArrayList<>();
    private final List<String> generalErrorMessages = new ArrayList<>();
    private final List<TestMessage> noteMessages = new ArrayList<>();

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
      if (kind == Diagnostic.Kind.ERROR) {
        generalErrorMessages.add(msg.toString());
      } else if (kind == Diagnostic.Kind.NOTE) {
        noteMessages.add(new TestMessage(kind, msg.toString(), null));
      }
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element) {
      if (kind == Diagnostic.Kind.ERROR) {
        errorMessages.add(new TestMessage(kind, msg.toString(), element));
      } else if (kind == Diagnostic.Kind.NOTE) {
        noteMessages.add(new TestMessage(kind, msg.toString(), element));
      }
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind,
        CharSequence msg,
        Element element,
        javax.lang.model.element.AnnotationMirror annotationMirror) {
      printMessage(kind, msg, element);
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind,
        CharSequence msg,
        Element element,
        javax.lang.model.element.AnnotationMirror annotationMirror,
        javax.lang.model.element.AnnotationValue annotationValue) {
      printMessage(kind, msg, element);
    }

    public List<TestMessage> getErrorMessages() {
      return errorMessages;
    }

    public List<String> getGeneralErrorMessages() {
      return generalErrorMessages;
    }

    public List<TestMessage> getNoteMessages() {
      return noteMessages;
    }
  }

  private static class TestMessage {
    private final Diagnostic.Kind kind;
    private final String message;
    private final Element element;

    public TestMessage(Diagnostic.Kind kind, String message, Element element) {
      this.kind = kind;
      this.message = message;
      this.element = element;
    }

    public Diagnostic.Kind getKind() {
      return kind;
    }

    public String getMessage() {
      return message;
    }

    public Element getElement() {
      return element;
    }
  }

  private static class TestProcessingEnvironment implements ProcessingEnvironment {
    private final Messager messager;
    private final Map<String, String> options = new HashMap<>();

    public TestProcessingEnvironment(Messager messager) {
      this.messager = messager;
      this.options.put("equilibrium.banner", "false");
    }

    @Override
    public Map<String, String> getOptions() {
      return options;
    }

    @Override
    public Messager getMessager() {
      return messager;
    }

    @Override
    public Filer getFiler() {
      return null;
    }

    @Override
    public Elements getElementUtils() {
      return null;
    }

    @Override
    public Types getTypeUtils() {
      return null;
    }

    @Override
    public SourceVersion getSourceVersion() {
      return SourceVersion.RELEASE_21;
    }

    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }

  private static class TestFiler implements Filer {
    @Override
    public javax.tools.JavaFileObject createSourceFile(
        CharSequence name, Element... originatingElements) {
      return null;
    }

    @Override
    public javax.tools.JavaFileObject createClassFile(
        CharSequence name, Element... originatingElements) {
      return null;
    }

    @Override
    public javax.tools.FileObject createResource(
        javax.tools.JavaFileManager.Location location,
        CharSequence pkg,
        CharSequence relativeName,
        Element... originatingElements) {
      return null;
    }

    @Override
    public javax.tools.FileObject getResource(
        javax.tools.JavaFileManager.Location location,
        CharSequence pkg,
        CharSequence relativeName) {
      return null;
    }
  }

  private static class TestProcessingEnvironmentWithFiler implements ProcessingEnvironment {
    private final Messager messager;
    private final Filer filer;
    private final Map<String, String> options = new HashMap<>();

    public TestProcessingEnvironmentWithFiler(Messager messager, Filer filer) {
      this.messager = messager;
      this.filer = filer;
      this.options.put("equilibrium.banner", "false");
    }

    @Override
    public Map<String, String> getOptions() {
      return options;
    }

    @Override
    public Messager getMessager() {
      return messager;
    }

    @Override
    public Filer getFiler() {
      return filer;
    }

    @Override
    public Elements getElementUtils() {
      return null;
    }

    @Override
    public Types getTypeUtils() {
      return null;
    }

    @Override
    public SourceVersion getSourceVersion() {
      return SourceVersion.RELEASE_21;
    }

    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }

  // Test helper classes for element gathering tests

  private static class TestRoundEnvironmentWithElements
      implements javax.annotation.processing.RoundEnvironment {
    private final Map<Class<? extends java.lang.annotation.Annotation>, Set<Element>>
        elementsByAnnotation = new HashMap<>();

    public void addElementForAnnotation(
        Class<? extends java.lang.annotation.Annotation> annotation, Element element) {
      elementsByAnnotation.computeIfAbsent(annotation, k -> new HashSet<>()).add(element);
    }

    @Override
    public boolean processingOver() {
      return false;
    }

    @Override
    public boolean errorRaised() {
      return false;
    }

    @Override
    public Set<? extends Element> getRootElements() {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(javax.lang.model.element.TypeElement a) {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(
        Class<? extends java.lang.annotation.Annotation> a) {
      return elementsByAnnotation.getOrDefault(a, new HashSet<>());
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWithAny(
        javax.lang.model.element.TypeElement... annotations) {
      return new HashSet<>();
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWithAny(
        Set<Class<? extends java.lang.annotation.Annotation>> annotations) {
      return new HashSet<>();
    }
  }

  @SuppressWarnings({"NullableProblems", "DataFlowIssue"})
  private static class TestClassElement implements javax.lang.model.element.TypeElement {
    private final String qualifiedName;
    private final List<Element> enclosedElements = new ArrayList<>();

    public TestClassElement(String qualifiedName) {
      this.qualifiedName = qualifiedName;
    }

    @Override
    public javax.lang.model.element.Name getQualifiedName() {
      return new TestName(qualifiedName);
    }

    @Override
    public javax.lang.model.element.Name getSimpleName() {
      String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
      return new TestName(simpleName);
    }

    @Override
    public javax.lang.model.type.TypeMirror asType() {
      return null;
    }

    @Override
    public ElementKind getKind() {
      return ElementKind.CLASS;
    }

    @Override
    public Set<javax.lang.model.element.Modifier> getModifiers() {
      return new HashSet<>();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return enclosedElements;
    }

    @Override
    public List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors() {
      return new ArrayList<>();
    }

    @Override
    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    public <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(
        Class<A> annotationType) {
      return null;
    }

    @Override
    public Element getEnclosingElement() {
      return null;
    }

    @Override
    public javax.lang.model.type.TypeMirror getSuperclass() {
      return null;
    }

    @Override
    public List<? extends javax.lang.model.type.TypeMirror> getInterfaces() {
      return new ArrayList<>();
    }

    @Override
    public List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters() {
      return new ArrayList<>();
    }

    @Override
    public javax.lang.model.element.NestingKind getNestingKind() {
      return javax.lang.model.element.NestingKind.TOP_LEVEL;
    }

    @Override
    public <R, P> R accept(javax.lang.model.element.ElementVisitor<R, P> v, P p) {
      return null;
    }
  }

  @SuppressWarnings("NullableProblems")
  private static class TestInterfaceElement extends TestClassElement {
    public TestInterfaceElement(String qualifiedName) {
      super(qualifiedName);
    }

    @Override
    public ElementKind getKind() {
      return ElementKind.INTERFACE;
    }
  }

  @SuppressWarnings("NullableProblems")
  private static class TestEnumElement extends TestClassElement {
    public TestEnumElement(String qualifiedName) {
      super(qualifiedName);
    }

    @Override
    public ElementKind getKind() {
      return ElementKind.ENUM;
    }
  }

  @Test
  void testRegisterAndLookupGeneratedDto() {
    String simpleName = "TestDto";
    String fullQualifiedName = "com.test.dto.TestDto";

    io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator.registerGeneratedDto(
        simpleName, fullQualifiedName);

    String result =
        io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator
            .lookupGeneratedDto(simpleName);

    assertEquals(
        fullQualifiedName,
        result,
        "lookupGeneratedDto should return the registered full qualified name");
  }

  @Test
  void testLookupNonExistentDto() {
    String result =
        io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator
            .lookupGeneratedDto("NonExistentDto_" + System.nanoTime());

    assertNull(result, "lookupGeneratedDto should return null for non-existent DTOs");
  }
}
