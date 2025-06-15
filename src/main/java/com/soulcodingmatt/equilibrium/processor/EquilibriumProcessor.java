package com.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import com.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import com.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import com.soulcodingmatt.equilibrium.annotations.vo.GenerateVo;
import com.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;
import com.soulcodingmatt.equilibrium.processor.generator.RecordGenerator;
import com.soulcodingmatt.equilibrium.processor.generator.VoGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "com.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
        "com.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
        "com.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
        "com.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
        "com.soulcodingmatt.equilibrium.annotations.vo.GenerateVo",
        "com.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo",
        "com.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedOptions({
        "equilibrium.dto.package",
        "equilibrium.dto.postfix",
        "equilibrium.record.package",
        "equilibrium.record.postfix",
        "equilibrium.vo.package",
        "equilibrium.vo.postfix"
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
        // If processing is over, we haven't claimed any new annotations
        if (roundEnv.processingOver()) {
            return false;
        }

        // Check if any of our supported annotations are present
        boolean hasRelevantAnnotations = annotations.stream()
                .map(TypeElement::getQualifiedName)
                .map(Object::toString)
                .anyMatch(name -> name.startsWith("com.soulcodingmatt.equilibrium.annotations"));

        // If none of our annotations are present, don't claim them
        if (!hasRelevantAnnotations) {
            return false;
        }

        try {
            // Get valid class elements that need processing
            Set<TypeElement> validElements = getValidClassElements(roundEnv);

            // If no valid elements found, don't claim the annotations
            if (validElements.isEmpty()) {
                return false;
            }

            // Process each valid element
            for (TypeElement typeElement : validElements) {
                processElement(typeElement);
            }

            // We've processed our annotations, so claim them
            return true;
        } catch (Exception e) {
            error(null, "Failed to process annotations: " + e.getMessage());
            e.printStackTrace();
            // On error, don't claim the annotations so other processors might handle them
            return false;
        }
    }

    private Set<TypeElement> getValidClassElements(RoundEnvironment roundEnv) {
        Set<Element> elements = new HashSet<>();
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateDto.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateRecord.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateVo.class));

        return elements.stream()
                .filter(this::isValidClassElement)
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
    }

    private boolean isValidClassElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "Annotations can only be applied to classes");
            return false;
        }
        return true;
    }

    private void processElement(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();

        // Skip if already processed
        if (processedElements.contains(qualifiedName)) {
            return;
        }
        processedElements.add(qualifiedName);

        // Generate DTO if needed
        GenerateDto dtoAnnotation = typeElement.getAnnotation(GenerateDto.class);
        if (dtoAnnotation != null) {
            processGenerateDto(typeElement);
        }

        // Generate Record if needed
        GenerateRecord recordAnnotation = typeElement.getAnnotation(GenerateRecord.class);
        if (recordAnnotation != null) {
            processGenerateRecord(typeElement);
        }

        // Generate Value Object if needed
        GenerateVo voAnnotation = typeElement.getAnnotation(GenerateVo.class);
        if (voAnnotation != null) {
            processGenerateVo(typeElement);
        }
    }

    private void processGenerateDto(TypeElement classElement) {
        try {
            GenerateDto annotation = classElement.getAnnotation(GenerateDto.class);
            String packageName = config.validateAndGetPackage(annotation.pkg(), "DTO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "DTO");
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            DtoGenerator generator = new DtoGenerator(classElement, packageName, postfix, builder, filer);
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
            String packageName = config.validateAndGetPackage(annotation.pkg(), "Record");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "Record");

            // Create and run the Record generator
            RecordGenerator generator = new RecordGenerator(classElement, packageName, postfix, filer);
            generator.generate();

            note(classElement, "Generated Record class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processGenerateVo(TypeElement classElement) {
        try {
            GenerateVo annotation = classElement.getAnnotation(GenerateVo.class);
            String packageName = config.validateAndGetPackage(annotation.pkg(), "VO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "VO");
            String idField = config.isValidFieldName(annotation.id()) ? annotation.id() : "";
            boolean generateSetter = annotation.setter();
            boolean overrides = annotation.overrides();

            // Create and run the Value Object generator
            VoGenerator generator = new VoGenerator(classElement, packageName, postfix, 
                                                  idField, generateSetter, overrides, filer);
            generator.generate();

            note(classElement, "Generated Value Object class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Value Object: " + e.getMessage());
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
