package io.github.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos;
import io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto;
import io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo;
import io.github.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generator.RecordGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generator.VoGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
        "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos",
        "io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
        "io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
        "io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
        "io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo",
        "io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo",
        "io.github.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
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
                .anyMatch(name -> name.startsWith("io.github.soulcodingmatt.equilibrium.annotations"));

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
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateDtos.class));
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

        // Process multiple DTO annotations
        processGenerateDtos(typeElement);

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

    private void processGenerateDtos(TypeElement classElement) {
        // Get all @GenerateDto annotations (handles both single and multiple annotations)
        GenerateDto[] dtoAnnotations = classElement.getAnnotationsByType(GenerateDto.class);
        
        if (dtoAnnotations.length == 0) {
            return; // No DTO annotations found
        }
        
        // Validate unique combinations of package and postfix
        if (!validateUniqueDtoCombinations(classElement, dtoAnnotations)) {
            return; // Validation failed, error already logged
        }
        
        // Process each DTO annotation
        for (GenerateDto annotation : dtoAnnotations) {
            processGenerateDto(classElement, annotation);
        }
    }

    private boolean validateUniqueDtoCombinations(TypeElement classElement, GenerateDto[] annotations) {
        Set<String> uniqueCombinations = new HashSet<>();
        Set<Integer> usedIds = new HashSet<>();
        
        for (GenerateDto annotation : annotations) {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "DTO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "DTO");
            String combination = packageName + "." + classElement.getSimpleName() + postfix;
            
            if (!uniqueCombinations.add(combination)) {
                error(classElement, "Duplicate DTO configuration would generate the same class: " + combination);
                return false;
            }
            
            // Validate unique IDs (only if ID is explicitly set)
            int id = annotation.id();
            if (id != -1) { // -1 is the default value meaning no ID specified
                if (!usedIds.add(id)) {
                    error(classElement, "Duplicate DTO ID: " + id + ". Each @GenerateDto annotation must have a unique ID.");
                    return false;
                }
            }
        }
        
        return true;
    }

    private void processGenerateDto(TypeElement classElement, GenerateDto annotation) {
        try {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "DTO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "DTO");
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        "Invalid field name in ignore list: '" + fieldName + "' - will be skipped", 
                        classElement);
                }
            }
            
            // Validate @IgnoreDto annotations for duplicate IDs
            validateIgnoreDtoAnnotations(classElement);
            
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            int dtoId = annotation.id();
            DtoGenerator generator = new DtoGenerator(classElement, packageName, postfix, ignoredFields, builder, dtoId, filer);
            generator.generate();

            note(classElement, "Generated DTO class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate DTO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateIgnoreDtoAnnotations(TypeElement classElement) {
        // Check all fields for @IgnoreDto annotations and validate their ids arrays
        classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .forEach(field -> {
                IgnoreDto ignoreDtoAnnotation = field.getAnnotation(IgnoreDto.class);
                if (ignoreDtoAnnotation != null) {
                    int[] ids = ignoreDtoAnnotation.ids();
                    Set<Integer> uniqueIds = new HashSet<>();
                    for (int id : ids) {
                        if (!uniqueIds.add(id)) {
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                "Duplicate ID " + id + " in @IgnoreDto annotation for field '" + 
                                field.getSimpleName() + "' - duplicates will be ignored", field);
                        }
                    }
                }
            });
    }

    private void processGenerateRecord(TypeElement classElement) {
        try {
            GenerateRecord annotation = classElement.getAnnotation(GenerateRecord.class);
            String packageName = config.validateAndGetPackage(annotation.pkg(), "Record");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "Record");
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        "Invalid field name in ignore list: '" + fieldName + "' - will be skipped", 
                        classElement);
                }
            }

            // Create and run the Record generator
            RecordGenerator generator = new RecordGenerator(classElement, packageName, postfix, ignoredFields, filer);
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
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        "Invalid field name in ignore list: '" + fieldName + "' - will be skipped", 
                        classElement);
                }
            }
            
            boolean generateSetter = annotation.setters();
            boolean overrides = annotation.overrides();

            // Create and run the Value Object generator
            VoGenerator generator = new VoGenerator(classElement, packageName, postfix, 
                                                  ignoredFields, generateSetter, overrides, filer);
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
