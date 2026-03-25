package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import static io.github.soulcodingmatt.equilibrium.processor.orchestration.GenerationOrchestratorSupport.*;

import io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord;
import io.github.soulcodingmatt.equilibrium.processor.config.EquilibriumConfig;
import io.github.soulcodingmatt.equilibrium.processor.generation.record.RecordGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generation.record.RecordGeneratorTarget;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.RecordValidationEmitterImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

/** Handles all Record-related code generation for a single source class. */
class RecordGenerationOrchestrator {

  private static final String TYPE_LABEL = "Record";

  private final EquilibriumConfig config;
  private final DiagnosticReporter reporter;
  private final Filer filer;

  RecordGenerationOrchestrator(EquilibriumConfig config, DiagnosticReporter reporter, Filer filer) {
    this.config = config;
    this.reporter = reporter;
    this.filer = filer;
  }

  void processGenerateRecords(TypeElement classElement) {
    GenerateRecord[] annotations = classElement.getAnnotationsByType(GenerateRecord.class);
    if (annotations.length == 0) {
      return;
    }

    List<ResolvedTarget> targets = resolveTargets(classElement, annotations);

    if (!validateUniqueCombinations(
        classElement, reporter, TYPE_LABEL, "GenerateRecord", targets)) {
      return;
    }

    Set<Integer> validIds = collectExplicitIds(targets);

    validateIgnoreAnnotationIds(
        classElement,
        validIds,
        reporter,
        "IgnoreRecord",
        "GenerateRecord",
        field -> {
          IgnoreRecord ann = field.getAnnotation(IgnoreRecord.class);
          return ann != null ? ann.ids() : null;
        });

    for (GenerateRecord annotation : annotations) {
      processGenerateRecord(classElement, annotation);
    }
  }

  private void processGenerateRecord(TypeElement classElement, GenerateRecord annotation) {
    String packageName = config.validateAndGetPackage(annotation.pkg(), TYPE_LABEL);
    String className = resolveClassName(classElement, annotation.name(), config, TYPE_LABEL);
    String fullClassName = packageName + "." + className;
    Set<String> ignoredFields =
        collectIgnoredFields(annotation.ignore(), classElement, config, reporter);

    executeGeneration(
        classElement,
        reporter,
        TYPE_LABEL,
        fullClassName,
        () -> {
          RecordGenerator generator =
              new RecordGenerator(
                  new RecordGeneratorTarget(
                      classElement, packageName, className, ignoredFields, annotation.id()),
                  filer,
                  new RecordValidationEmitterImpl());
          generator.generate();
        });
  }

  private List<ResolvedTarget> resolveTargets(
      TypeElement classElement, GenerateRecord[] annotations) {
    return Arrays.stream(annotations)
        .map(
            a ->
                new ResolvedTarget(
                    config.validateAndGetPackage(a.pkg(), TYPE_LABEL),
                    resolveClassName(classElement, a.name(), config, TYPE_LABEL),
                    a.id()))
        .toList();
  }
}
