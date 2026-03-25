package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.List;
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
 * Test-only processor that runs {@link InheritedBuilderDefaultScanner} on a named probe type so
 * {@link InheritedBuilderDefaultScanner} behavior can be asserted without going through the full
 * {@link io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumProcessor}
 * pipeline.
 */
@SupportedAnnotationTypes("*")
public class InheritedBuilderScanHarnessProcessor extends AbstractProcessor {

  static volatile InheritedBuilderDefaultScan lastScan;

  private Trees trees;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    trees = Trees.instance(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (trees == null || roundEnv.processingOver()) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (root instanceof TypeElement te
          && te.getQualifiedName()
              .toString()
              .equals("com.acme.probe.LombokInheritedDefaultsProbe")) {
        List<VariableElement> fields = new ArrayList<>();
        for (Element enclosed : te.getEnclosedElements()) {
          if (enclosed.getKind() == ElementKind.FIELD) {
            fields.add((VariableElement) enclosed);
          }
        }
        lastScan = InheritedBuilderDefaultScanner.scan(trees, true, fields);
      }
    }
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
