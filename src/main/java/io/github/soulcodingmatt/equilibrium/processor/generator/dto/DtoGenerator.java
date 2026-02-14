package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.annotations.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.processor.generator.*;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility.FieldInclusionConfig;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility.GeneratorType;
import io.github.soulcodingmatt.equilibrium.processor.generator.imports.ImportManager;
import io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

public class DtoGenerator {

    // Registry to track generated DTOs: simpleName -> fullQualifiedName
    private static final Map<String, String> generatedDtoRegistry = new HashMap<>();
    public static final String DTO_CLASS = "dtoClass=";

    /**
     * Register a generated DTO for import resolution
     *
     * @param simpleName        Simple class name (e.g., "BodyDto")
     * @param fullQualifiedName Full qualified name (e.g., "com.soulcodingmatt.dto.BodyDto")
     */
    public static void registerGeneratedDto(String simpleName, String fullQualifiedName) {
        generatedDtoRegistry.put(simpleName, fullQualifiedName);
    }

    /**
     * Look up the full qualified name of a generated DTO
     *
     * @param simpleName Simple class name (e.g., "BodyDto")
     * @return Full qualified name if found, null otherwise
     */
    public static String lookupGeneratedDto(String simpleName) {
        return generatedDtoRegistry.get(simpleName);
    }

    private final TypeElement classElement;
    private final String packageName;
    private final String dtoClassName;
    private final Set<String> ignoredFields;
    private final boolean builder;
    private final Filer filer;
    private final int dtoId;
    private final Messager messager;
    private final Trees trees;
    private final Map<VariableElement, String> inheritedDefaultInitializers = new HashMap<>();
    private final Set<String> extraImportsForInheritedDefaults = new HashSet<>();

    public DtoGenerator(TypeElement classElement, String packageName, String dtoClassName,
                        Set<String> ignoredFields, boolean builder, int dtoId, Filer filer,
                        Messager messager, Trees trees) {
        this.classElement = classElement;
        this.packageName = packageName;
        this.dtoClassName = dtoClassName;
        this.ignoredFields = ignoredFields != null ? ignoredFields : new HashSet<>();
        this.filer = filer;
        this.builder = builder;
        this.dtoId = dtoId;
        this.messager = messager;
        this.trees = trees;
    }

    public void generate() throws IOException {

        // Get all fields that should be included in the DTO
        List<VariableElement> fields = getIncludedFields();
        // Pre-scan for safe inherited builder defaults to collect initializers and imports
        preScanInheritedBuilderDefaults(fields);

        // Create or update the DTO file
        JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + dtoClassName, classElement);

        try (Writer writer = sourceFile.openWriter()) {
            CodeWriter code = new CodeWriter(writer);
            DtoClassWriter classWriter = new DtoClassWriter();

            // File header and standard imports
            classWriter.writeFileHeader(writer, packageName, builder);
            writeImports(writer, fields);

            // Class header and open block
            classWriter.beginClass(code, dtoClassName, classElement, builder);

            // Write fields (annotations via legacy writer, declaration via DtoClassWriter)
            for (VariableElement field : fields) {
                writeField(writer, code, classWriter, field);
            }

            // Write constructor using CodeWriter
            classWriter.emitConstructor(code, dtoClassName, fields, this::getTransformedFieldType);

            // Write getters and setters using CodeWriter
            for (VariableElement field : fields) {
                String type = getTransformedFieldType(field);
                String name = field.getSimpleName().toString();
                classWriter.emitGetter(code, type, name);
                classWriter.emitSetter(code, type, name);
            }

            // Write standard method overrides (always generated)
            classWriter.emitEqualsHashToString(writer, fields, dtoClassName, dtoClassName);

            // Close class
            code.endBlock();
        }
    }

    private List<VariableElement> getIncludedFields() {
        FieldInclusionConfig fieldConfig = new FieldInclusionConfig(GeneratorType.DTO, ignoredFields, dtoId);
        return GeneratorUtility.getIncludedFields(classElement, fieldConfig);
    }

    private void writeImports(Writer writer, List<VariableElement> fields) throws IOException {
        Set<String> imports = new HashSet<>();
        Set<VariableElement> fieldsWithNestedMapping = new HashSet<>();

        NestedMappingResolver nestedResolver = new NestedMappingResolver(classElement, messager, dtoClassName);

        // First pass: collect fields with @NestedMapping and add their DTO imports
        for (VariableElement field : fields) {
            NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);

            if (nestedMapping != null) {
                fieldsWithNestedMapping.add(field);

                String dtoImport = nestedResolver.findDtoImportFromSourceClass(nestedMapping);
                if (dtoImport != null) {
                    imports.add(dtoImport);
                }
            }
        }

        // Second pass: add standard field imports, BUT skip fields that have @NestedMapping
        Set<String> fieldImports = fields.stream()
                .filter(field -> !fieldsWithNestedMapping.contains(field))  // Skip @NestedMapping fields
                .map(field -> field.asType().toString())  // Get original field types
                .map(GeneratorUtility::extractBaseType)  // Extract base type without generics
                .filter(type -> type.contains("."))
                .collect(Collectors.toSet());
        imports.addAll(fieldImports);

        // Add Jakarta Bean Validation imports if validation annotations are used
        Set<String> validationImports = ValidationSupport.collectValidationImports(fields, dtoId);
        imports.addAll(validationImports);

        // Add Builder.Default import if needed and builder is enabled
        if (builder && hasBuilderDefaults(fields)) {
            imports.add("lombok.Builder");
        }

        // Add additional imports needed for builder defaults
        Set<String> builderDefaultImports = getBuilderDefaultImports(fields);
        imports.addAll(builderDefaultImports);
        // Add imports needed due to inherited builder defaults we copied
        imports.addAll(extraImportsForInheritedDefaults);

        // Use ImportManager to filter and write imports
        ImportManager importManager = new ImportManager();
        importManager.addAll(imports);
        importManager.writeTo(writer);
    }

    // Import validation handled by ImportManager

    /**
     * Checks if any fields have @DtoBuilderDefault annotations or inherited builder defaults.
     */
    private boolean hasBuilderDefaults(List<VariableElement> fields) {
        for (VariableElement field : fields) {
            // Check for @DtoBuilderDefault annotation
            if (field.getAnnotation(DtoBuilderDefault.class) != null) {
                return true;
            }

            // Check for existing @Builder.Default annotation if builder is enabled
            if (builder && hasExistingBuilderDefault(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets additional imports needed for builder defaults.
     */
    private Set<String> getBuilderDefaultImports(List<VariableElement> fields) {
        Set<String> imports = new HashSet<>();

        if (!builder) {
            return imports; // No builder defaults if builder is disabled
        }

        for (VariableElement field : fields) {
            DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
            if (builderDefault != null) {
                // Add imports for collection types that use default constructors
                String fieldType = field.asType().toString();
                if (DefaultValueResolver.isCollectionType(fieldType)) {
                    addCollectionImports(imports, fieldType);
                } else if (DefaultValueResolver.isOptionalType(fieldType)) {
                    imports.add(TypeNames.JAVA_UTIL_OPTIONAL);
                }

                // Add enum imports if auto-detected enum types are used
                if (isEnumType(field)) {
                    addEnumImports(imports, field);
                }
            }
        }

        return imports;
    }

    /**
     * Checks if the field has an existing @Builder.Default annotation.
     */
    private boolean hasExistingBuilderDefault(VariableElement field) {
        // Check if field already has @Builder.Default annotation
        return field.getAnnotationMirrors().stream()
                .anyMatch(mirror -> mirror.getAnnotationType().toString().equals("lombok.Builder.Default"));
    }

    /**
     * Adds necessary imports for collection types.
     */
    private void addCollectionImports(Set<String> imports, String fieldType) {
        String baseType = GeneratorUtility.extractBaseType(fieldType);
        switch (baseType) {
            case TypeNames.JAVA_UTIL_LIST, "List" -> {
                imports.add(TypeNames.JAVA_UTIL_LIST);
                imports.add("java.util.ArrayList");
            }
            case TypeNames.JAVA_UTIL_SET, "Set" -> {
                imports.add(TypeNames.JAVA_UTIL_SET);
                imports.add("java.util.HashSet");
            }
            case TypeNames.JAVA_UTIL_MAP, "Map" -> {
                imports.add(TypeNames.JAVA_UTIL_MAP);
                imports.add("java.util.HashMap");
            }
        }
    }

    private String escapeQuotes(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Transforms the field type based on @NestedMapping annotations.
     * Returns simple names for use in field declarations, getters, setters, constructor.
     */
    private String getTransformedFieldType(VariableElement field) {
        NestedMappingResolver resolver = new NestedMappingResolver(classElement, messager, dtoClassName);
        return resolver.getTransformedFieldType(field);
    }

    /**
     * Writes @Builder.Default annotation if applicable.
     */
    private void writeBuilderDefaultAnnotation(Writer writer, VariableElement field) throws IOException {
        if (!builder) {
            return; // No builder defaults if builder is disabled
        }

        // Check for @DtoBuilderDefault annotation
        DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
        if (builderDefault != null) {
            // Validate that field is not final
            if (field.getModifiers().contains(javax.lang.model.element.Modifier.FINAL)) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@DtoBuilderDefault cannot be applied to final fields: " + field.getSimpleName(),
                        field);
                return;
            }
            // Emit @Builder.Default only if we'll have an initializer (explicit or inherited when allowed)
            boolean willInherit = builderDefault.inherit() && inheritedDefaultInitializers.containsKey(field);
            boolean hasExplicit = hasExplicitDtoDefault(field, builderDefault);
            if (hasExplicit || willInherit) {
                writer.write("    @Builder.Default\n");
            }
            return;
        }

        // Check for existing @Builder.Default annotation from base class
        // Only emit if we actually copied a safe initializer
        if (hasExistingBuilderDefault(field) && inheritedDefaultInitializers.containsKey(field)) {
            writer.write("    @Builder.Default\n");
        }
    }

    /**
     * Gets the field declaration including type, name, and optional default value.
     */
    private String getFieldDeclaration(VariableElement field, String transformedType, String name) {
        if (!builder) {
            return transformedType + " " + name; // No defaults if builder is disabled
        }

        // Check for @DtoBuilderDefault annotation
        DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
        if (builderDefault != null) {
            String defaultValue = getBuilderDefaultValue(field, builderDefault);
            if (defaultValue != null && !defaultValue.isEmpty()) {
                return transformedType + " " + name + " = " + defaultValue;
            }
        }

        // Check for existing builder default from base class (only if allowed)
        if ((builderDefault == null || builderDefault.inherit()) && hasExistingBuilderDefault(field)) {
            String existingDefault = getExistingBuilderDefaultValue(field);
            if (existingDefault != null && !existingDefault.isEmpty()) {
                return transformedType + " " + name + " = " + existingDefault;
            }
        }

        return transformedType + " " + name;
    }

    /**
     * Gets the default value for a field with @DtoBuilderDefault annotation.
     */
    private String getBuilderDefaultValue(VariableElement field, DtoBuilderDefault builderDefault) {
        String fieldType = field.asType().toString();

        // Check for type-specific parameters first (new approach)
        String typeSpecificValue = DefaultValueResolver.getTypeSpecificValue(field, builderDefault, messager);
        if (typeSpecificValue != null) {
            return typeSpecificValue;
        }

        // If inherit=false, do not fall back to inherited default
        if (!builderDefault.inherit()) {
            // No explicit value and inheritance disabled: do not provide a default
            return null;
        }

        // Fallback to legacy string value parameter
        String annotationValue = builderDefault.value();
        if (!annotationValue.isEmpty()) {
            return DefaultValueResolver.processAnnotationValue(field, fieldType, annotationValue, messager);
        }

        // Special handling for collections and Optional
        if (DefaultValueResolver.isCollectionType(fieldType)) {
            return DefaultValueResolver.getCollectionDefaultValue(fieldType);
        } else if (DefaultValueResolver.isOptionalType(fieldType)) {
            return "Optional.empty()";
        }

        // For other types, require explicit value
        messager.printMessage(Diagnostic.Kind.ERROR,
                "@DtoBuilderDefault requires a value for non-collection, non-Optional field: " + field.getSimpleName(),
                field);
        return null;
    }

    /**
     * Gets the default value from type-specific parameters in the annotation.
     */
    private String getTypeSpecificValue(VariableElement field, DtoBuilderDefault builderDefault) {
        String fieldType = field.asType().toString();

        // Check for primitive and wrapper types
        if (isPrimitiveType(fieldType) || isWrapperType(fieldType)) {
            // Integer types
            boolean isIntegerType = fieldType.equals(INTEGER_STRING) || fieldType.equals(JAVA_LANG_INTEGER) || fieldType.equals(INTEGER);
            if (isIntegerType && builderDefault.intValue() != Integer.MIN_VALUE) {
                return String.valueOf(builderDefault.intValue());
            }

            // Long types

            boolean isLongType = fieldType.equals(LONG_STRING) || fieldType.equals(JAVA_LANG_LONG) || fieldType.equals(LONG);
            if (isLongType && builderDefault.longValue() != Long.MIN_VALUE) {
                return builderDefault.longValue() + "L";
            }

            // Short types
            boolean isShortType = fieldType.equals(SHORT_STRING) || fieldType.equals(JAVA_LANG_SHORT) || fieldType.equals(SHORT);
            if (isShortType && builderDefault.shortValue() != Short.MIN_VALUE) {
                return String.valueOf(builderDefault.shortValue());
            }

            // Byte types
            boolean isByteType = fieldType.equals(BYTE_STRING) || fieldType.equals(JAVA_LANG_BYTE) || fieldType.equals(BYTE);
            if (isByteType && builderDefault.byteValue() != Byte.MIN_VALUE) {
                return String.valueOf(builderDefault.byteValue());
            }

            // Float types
            boolean isFloatType = fieldType.equals(FLOAT_STRING) || fieldType.equals(JAVA_LANG_FLOAT) || fieldType.equals(FLOAT);

            if (isFloatType && builderDefault.floatValue() != Float.MIN_VALUE) {
                return builderDefault.floatValue() + "f";
            }

            // Double types

            boolean isDoubleType = fieldType.equals(DOUBLE_STRING) || fieldType.equals(JAVA_LANG_DOUBLE) || fieldType.equals(DOUBLE);

            if (isDoubleType && builderDefault.doubleValue() != Double.MIN_VALUE) {
                return String.valueOf(builderDefault.doubleValue());
            }

            // Boolean types - we need to check if any boolean parameter is explicitly set
            boolean isBooleanType = fieldType.equals(BOOLEAN_STRING) || fieldType.equals(JAVA_LANG_BOOLEAN) || fieldType.equals(BOOLEAN);

            // Check if booleanValue is explicitly set (we can't distinguish default false from explicit false)
            // So we'll check if any other parameter is set to determine if booleanValue was intended
            if (isBooleanType && hasAnyOtherParameterSet(builderDefault)) {
                return String.valueOf(builderDefault.booleanValue());
            }

            // Character types
            boolean isCharacterType = fieldType.equals("char") || fieldType.equals(JAVA_LANG_CHARACTER) || fieldType.equals(CHARACTER);
            if (isCharacterType && builderDefault.charValue() != '\0') {
                return "'" + builderDefault.charValue() + "'";
            }
        }

        // String type
        if (DefaultValueResolver.isStringType(fieldType) && !builderDefault.stringValue().isEmpty()) {
            return DefaultValueResolver.processStringValue(builderDefault.stringValue());
        }

        // Enum type
        // Check for enumValue parameter (treat it like value parameter but with validation)
        if (isEnumType(field) && !builderDefault.enumValue().isEmpty()) {
            return DefaultValueResolver.processEnumValue(field, builderDefault.enumValue(), messager);
        }

        return null; // No type-specific value found
    }

    /**
     * Checks if any parameter other than booleanValue is set in the annotation.
     * This helps determine if booleanValue was explicitly set.
     */
    private boolean hasAnyOtherParameterSet(DtoBuilderDefault builderDefault) {
        return !builderDefault.stringValue().isEmpty() ||
                builderDefault.intValue() != Integer.MIN_VALUE ||
                builderDefault.longValue() != Long.MIN_VALUE ||
                builderDefault.shortValue() != Short.MIN_VALUE ||
                builderDefault.byteValue() != Byte.MIN_VALUE ||
                builderDefault.floatValue() != Float.MIN_VALUE ||
                builderDefault.doubleValue() != Double.MIN_VALUE ||
                builderDefault.charValue() != '\0' ||
                !builderDefault.enumValue().isEmpty() ||
                !builderDefault.value().isEmpty();
    }

    private boolean hasExplicitDtoDefault(VariableElement field, DtoBuilderDefault builderDefault) {
        // Returns true if any explicit parameter provides a value
        return DefaultValueResolver.hasExplicitDtoDefault(field, builderDefault);
    }

    /**
     * Checks if the given type is a primitive type.
     */
    private boolean isPrimitiveType(String fieldType) {
        return Arrays.asList(INTEGER_STRING, LONG_STRING, TypeNames.SHORT_STRING, TypeNames.BYTE_STRING, TypeNames.FLOAT_STRING, TypeNames.DOUBLE_STRING, TypeNames.BOOLEAN_STRING, "char")
                .contains(fieldType);
    }

    /**
     * Checks if the given type is a wrapper type.
     */
    private boolean isWrapperType(String fieldType) {
        return Arrays.asList(
                TypeNames.JAVA_LANG_INTEGER, TypeNames.INTEGER,
                TypeNames.JAVA_LANG_LONG, TypeNames.LONG,
                TypeNames.JAVA_LANG_SHORT, TypeNames.SHORT,
                TypeNames.JAVA_LANG_BYTE, TypeNames.BYTE,
                TypeNames.JAVA_LANG_FLOAT, TypeNames.FLOAT,
                TypeNames.JAVA_LANG_DOUBLE, TypeNames.DOUBLE,
                TypeNames.JAVA_LANG_BOOLEAN, TypeNames.BOOLEAN,
                TypeNames.JAVA_LANG_CHARACTER, TypeNames.CHARACTER
        ).contains(fieldType);
    }

    /**
     * Checks if a field is of enum type.
     */
    private boolean isEnumType(VariableElement field) {
        return field.asType().getKind() == javax.lang.model.type.TypeKind.DECLARED &&
                ((javax.lang.model.type.DeclaredType) field.asType()).asElement().getKind() == javax.lang.model.element.ElementKind.ENUM;
    }

    /**
     * Adds necessary imports for enum types used in builder defaults.
     */
    private void addEnumImports(Set<String> imports, VariableElement field) {
        if (isEnumType(field)) {
            javax.lang.model.type.DeclaredType declaredType = (javax.lang.model.type.DeclaredType) field.asType();
            javax.lang.model.element.TypeElement enumElement = (javax.lang.model.element.TypeElement) declaredType.asElement();
            String enumClassName = enumElement.getQualifiedName().toString();

            // Add import if it's not in java.lang package
            if (enumClassName.contains(".") && !enumClassName.startsWith("java.lang.")) {
                imports.add(enumClassName);
            }
        }
    }

    /**
     * Gets the existing default value from a field that already has @Builder.Default.
     * Uses a pre-scanned map populated from the AST when possible.
     */
    private String getExistingBuilderDefaultValue(VariableElement field) {
        return inheritedDefaultInitializers.get(field);
    }

    private void preScanInheritedBuilderDefaults(List<VariableElement> fields) {
        inheritedDefaultInitializers.clear();
        extraImportsForInheritedDefaults.clear();
        if (!builder || trees == null) {
            return;
        }

        for (VariableElement field : fields) {
            DtoBuilderDefault override = field.getAnnotation(DtoBuilderDefault.class);
            boolean inheritable = (override == null) || override.inherit();

            if (inheritable && hasExistingBuilderDefault(field)) {
                String init = extractInitializerSource(field);
                if (init != null && !init.isEmpty()) {
                    String safe = coerceSafeInitializer(field, init);
                    if (safe != null && !safe.isEmpty()) {
                        inheritedDefaultInitializers.put(field, safe);
                    }
                }
            }
        }
    }


    private String extractInitializerSource(VariableElement field) {
        try {
            TreePath path = trees.getPath(field);
            if (path == null) {
                return null;
            }
            if (!(path.getLeaf() instanceof VariableTree variableTree)) {
                return null;
            }
            ExpressionTree initializer = variableTree.getInitializer();
            if (initializer == null) {
                return null;
            }
            long start = trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), initializer);
            long end = trees.getSourcePositions().getEndPosition(path.getCompilationUnit(), initializer);
            if (start < 0 || end < 0) {
                return null;
            }
            CharSequence content = path.getCompilationUnit().getSourceFile().getCharContent(true);
            return content.subSequence((int) start, (int) end).toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String coerceSafeInitializer(VariableElement field, String initializerText) {
        // Use AST again to classify safely
        try {
            TreePath path = trees.getPath(field);
            if (path == null || !(path.getLeaf() instanceof VariableTree vt) || vt.getInitializer() == null) {
                return null;
            }
            ExpressionTree expr = vt.getInitializer();
            Tree.Kind kind = expr.getKind();

            // 1) Literal kinds
            if (kind == Tree.Kind.INT_LITERAL || kind == Tree.Kind.LONG_LITERAL ||
                    kind == Tree.Kind.FLOAT_LITERAL || kind == Tree.Kind.DOUBLE_LITERAL ||
                    kind == Tree.Kind.BOOLEAN_LITERAL || kind == Tree.Kind.CHAR_LITERAL ||
                    kind == Tree.Kind.STRING_LITERAL || kind == Tree.Kind.NULL_LITERAL) {
                return initializerText;
            }

            // 2) Enum constants: convert to EnumSimpleName.CONSTANT
            if (isEnumType(field) && (kind == Tree.Kind.MEMBER_SELECT || kind == Tree.Kind.IDENTIFIER)) {
                String enumSimple = ((TypeElement) ((DeclaredType) field.asType()).asElement()).getSimpleName().toString();
                String constant;
                if (kind == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree mst = (MemberSelectTree) expr;
                    constant = mst.getIdentifier().toString();
                } else {
                    IdentifierTree id = (IdentifierTree) expr;
                    constant = id.getName().toString();
                }
                // ensure we import enum type via normal field import logic
                return enumSimple + "." + constant;
            }

            // 3) new ArrayList<>() / new HashSet<>() / new HashMap<>() with no args
            if (kind == Tree.Kind.NEW_CLASS) {
                NewClassTree nct = (NewClassTree) expr;
                if (nct.getArguments() != null && !nct.getArguments().isEmpty()) {
                    return null;
                }
                String typeName = typeIdentifierToString(nct.getIdentifier());
                if (typeName == null) {
                    return null;
                }
                if (typeName.endsWith("ArrayList") || typeName.endsWith("HashSet") || typeName.endsWith("HashMap")) {
                    // add necessary imports for impl classes
                    if (typeName.endsWith("ArrayList")) {
                        extraImportsForInheritedDefaults.add(JAVA_UTIL_LIST);
                        extraImportsForInheritedDefaults.add("java.util.ArrayList");
                    } else if (typeName.endsWith("HashSet")) {
                        extraImportsForInheritedDefaults.add(JAVA_UTIL_SET);
                        extraImportsForInheritedDefaults.add("java.util.HashSet");
                    } else if (typeName.endsWith("HashMap")) {
                        extraImportsForInheritedDefaults.add(JAVA_UTIL_MAP);
                        extraImportsForInheritedDefaults.add("java.util.HashMap");
                    }
                    return initializerText;
                }
                return null;
            }

            // 4) Optional.empty() and Collections.emptyXxx() with no args
            if (kind == Tree.Kind.METHOD_INVOCATION) {
                MethodInvocationTree mit = (MethodInvocationTree) expr;
                if (mit.getArguments() != null && !mit.getArguments().isEmpty()) {
                    return null;
                }
                String select = methodSelectToString(mit.getMethodSelect());
                if (select == null) {
                    return null;
                }
                if (select.endsWith("Optional.empty")) {
                    extraImportsForInheritedDefaults.add(JAVA_UTIL_OPTIONAL);
                    return initializerText + ""; // as-is
                }
                if (select.endsWith("Collections.emptyList") || select.endsWith("Collections.emptySet") || select.endsWith("Collections.emptyMap")) {
                    extraImportsForInheritedDefaults.add("java.util.Collections");
                    return initializerText + "";
                }
                return null;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String typeIdentifierToString(Tree identifier) {
        if (identifier instanceof IdentifierTree id) {
            return id.getName().toString();
        }
        if (identifier instanceof MemberSelectTree mst) {
            // build fully qualified or dotted string
            return typeIdentifierToString(mst.getExpression()) + "." + mst.getIdentifier().toString();
        }
        return null;
    }

    private String methodSelectToString(ExpressionTree select) {
        if (select instanceof IdentifierTree id) {
            return id.getName().toString();
        }
        if (select instanceof MemberSelectTree mst) {
            String left = methodSelectToString(mst.getExpression());
            if (left == null) {
                return null;
            }
            return left + "." + mst.getIdentifier().toString();
        }
        return null;
    }

    private void writeField(Writer writer, CodeWriter code, DtoClassWriter classWriter, VariableElement field) throws IOException {
        // Check for validation annotations (handles both single and multiple ValidateDto annotations)
        ValidateDto[] validateAnnotations = field.getAnnotationsByType(ValidateDto.class);

        for (ValidateDto validateAnnotation : validateAnnotations) {
            if (ValidationSupport.shouldApplyValidation(validateAnnotation, dtoId)) {
                // Write type-safe validation annotations
                ValidationSupport.writeTypeSafeValidations(writer, validateAnnotation);

                // Write legacy string-based validation annotations (for backward compatibility)
                for (String validation : validateAnnotation.value()) {
                    if (!validation.trim().isEmpty()) {
                        writer.write("    " + validation + "\n");
                    }
                }
            }
        }

        // Write builder default annotations if builder is enabled
        writeBuilderDefaultAnnotation(writer, field);

        // Transform field type based on @NestedMapping annotations
        String transformedType = getTransformedFieldType(field);
        String name = field.getSimpleName().toString();

        // Check for unmapped custom objects and warn (only during field declaration)
        TypeMirror fieldType = field.asType();
        NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
        if (nestedMapping == null) {
            new CustomObjectWarning(messager, classElement, dtoClassName).check(field, fieldType);
        }

        // Write field declaration with optional default value using DtoClassWriter
        String fieldDeclaration = getFieldDeclaration(field, transformedType, name);
        // fieldDeclaration already includes "type name" and possibly "= default"; split to type/name for emitter
        // We still output as a single line via CodeWriter to keep identical behavior
        code.writeLine("private " + fieldDeclaration + ";");
        code.blankLine();
    }
}
