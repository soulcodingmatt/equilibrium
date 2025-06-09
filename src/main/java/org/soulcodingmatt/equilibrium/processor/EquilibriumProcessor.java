package org.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import org.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import org.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;
import org.soulcodingmatt.equilibrium.processor.generator.RecordGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
    "org.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
    "org.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
    "org.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
    "org.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedOptions({
    "equilibrium.dto.package",
    "equilibrium.dto.postfix",
    "equilibrium.record.package",
    "equilibrium.record.postfix"
})
public class EquilibriumProcessor extends AbstractProcessor {
    private final Set<String> processedElements = new HashSet<>();
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private Filer filer;
    private Messager messager;
    private EquilibriumConfig config;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        config = new EquilibriumConfig(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }

        try {
            // Process all elements that need generation
            Set<Element> elementsToProcess = new HashSet<>();
            elementsToProcess.addAll(roundEnv.getElementsAnnotatedWith(GenerateDto.class));
            elementsToProcess.addAll(roundEnv.getElementsAnnotatedWith(GenerateRecord.class));

            for (Element element : elementsToProcess) {
                if (element.getKind() != ElementKind.CLASS) {
                    error(element, "Annotations can only be applied to classes");
                    continue;
                }

                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();

                // Skip if we've already processed this element
                if (processedElements.contains(qualifiedName)) {
                    continue;
                }
                processedElements.add(qualifiedName);

                // Process DTO generation if needed
                GenerateDto dtoAnnotation = element.getAnnotation(GenerateDto.class);
                if (dtoAnnotation != null) {
                    processGenerateDto(typeElement);
                }

                // Process Record generation if needed
                GenerateRecord recordAnnotation = element.getAnnotation(GenerateRecord.class);
                if (recordAnnotation != null) {
                    processGenerateRecord(typeElement);
                }
            }
        } catch (Exception e) {
            error(null, "Failed to process annotations: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private void processGenerateDto(TypeElement classElement) {
        try {
            GenerateDto annotation = classElement.getAnnotation(GenerateDto.class);
            String packageName = config.validateAndGetPackage(annotation.packageName(), "DTO");
            String postfix = annotation.postfix().isEmpty() ? config.getDtoPostfix() : annotation.postfix();
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            DtoGenerator generator = new DtoGenerator(classElement, packageName, postfix, builder, filer, messager);
            generator.generate();

            note(classElement, "Generated DTO class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate DTO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processGenerateRecord(TypeElement classElement) {
        try {
            GenerateRecord annotation = classElement.getAnnotation(GenerateRecord.class);
            String packageName = config.validateAndGetPackage(annotation.packageName(), "Record");
            String postfix = annotation.postfix().isEmpty() ? config.getRecordPostfix() : annotation.postfix();

            // Create and run the Record generator
            RecordGenerator generator = new RecordGenerator(classElement, packageName, postfix, filer, messager);
            generator.generate();

            note(classElement, "Generated Record class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
