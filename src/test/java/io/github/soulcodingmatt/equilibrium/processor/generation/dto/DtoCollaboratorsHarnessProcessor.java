package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import io.github.soulcodingmatt.equilibrium.processor.generation.emit.CodeWriter;
import io.github.soulcodingmatt.equilibrium.processor.model.NestedMappingResolver;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Exercises {@link DtoBuilderDefaultSupport}, {@link DtoImportPlanner}, and {@link DtoFieldWriter}
 * on a compiled probe type for unit tests.
 */
@SupportedAnnotationTypes("*")
public class DtoCollaboratorsHarnessProcessor extends AbstractProcessor {

  private static final String PROBE = "com.acme.probe.DtoCollaboratorsProbe";

  static volatile String lastBuilderFieldDeclaration;
  static volatile String lastBuilderDefaultAnnotationBlock;
  static volatile String lastImportSection;
  static volatile String lastFieldWriterLabelBlock;

  private javax.annotation.processing.Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver() || messager == null) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (root instanceof TypeElement te && te.getQualifiedName().contentEquals(PROBE)) {
        runHarness(te);
      }
    }
    return false;
  }

  private void runHarness(TypeElement probe) {
    List<VariableElement> fields = fieldsOf(probe);
    VariableElement scoreField = findField(fields, "score");
    VariableElement labelField = findField(fields, "label");

    DtoBuilderDefaultSupport builderSupport =
        new DtoBuilderDefaultSupport(true, messager, Map.of());
    NestedMappingResolver nestedResolver = new NestedMappingResolver(probe, messager);

    String transformedScore = nestedResolver.getTransformedFieldType(scoreField);
    lastBuilderFieldDeclaration =
        builderSupport.getFieldDeclaration(scoreField, transformedScore, "score");
    try {
      StringWriter ann = new StringWriter();
      builderSupport.writeBuilderDefaultAnnotation(ann, scoreField);
      lastBuilderDefaultAnnotationBlock = ann.toString();
    } catch (IOException e) {
      throw new AssertionError(e);
    }

    try {
      StringWriter imports = new StringWriter();
      DtoImportPlanner.writeImports(
          imports, true, 1, fields, new HashSet<>(), builderSupport, nestedResolver);
      lastImportSection = imports.toString();
    } catch (IOException e) {
      throw new AssertionError(e);
    }

    DtoGeneratorTarget target =
        new DtoGeneratorTarget(probe, "com.acme.gen", "OutDto", Set.of(), true, 1);
    try {
      StringWriter fieldOutput = new StringWriter();
      CodeWriter code = new CodeWriter(fieldOutput);
      DtoFieldWriter fieldWriter =
          new DtoFieldWriter(target, messager, nestedResolver, builderSupport);
      fieldWriter.writeField(fieldOutput, code, labelField);
      lastFieldWriterLabelBlock = fieldOutput.toString();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static List<VariableElement> fieldsOf(TypeElement te) {
    List<VariableElement> out = new ArrayList<>();
    for (Element enclosed : te.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.FIELD) {
        out.add((VariableElement) enclosed);
      }
    }
    return out;
  }

  private static VariableElement findField(List<VariableElement> fields, String name) {
    for (VariableElement field : fields) {
      if (field.getSimpleName().contentEquals(name)) {
        return field;
      }
    }
    throw new IllegalStateException("No field: " + name);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
