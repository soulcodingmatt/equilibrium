package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

/**
 * Extracts safe initializer expressions from {@code @Builder.Default} fields in the source class so
 * they can be replayed on generated DTO fields.
 */
public final class InheritedBuilderDefaultScanner {

  private InheritedBuilderDefaultScanner() {}

  public static InheritedBuilderDefaultScan scan(
      Trees trees, boolean builder, List<VariableElement> fields) {
    if (!builder || trees == null) {
      return InheritedBuilderDefaultScan.empty();
    }

    Map<VariableElement, String> inheritedDefaultInitializers = new HashMap<>();
    Set<String> extraImportsForInheritedDefaults = new HashSet<>();
    Context ctx = new Context(trees, extraImportsForInheritedDefaults);

    for (VariableElement field : fields) {
      DtoBuilderDefault override = field.getAnnotation(DtoBuilderDefault.class);
      boolean inheritable = (override == null) || override.inherit();

      if (inheritable && ctx.hasExistingBuilderDefault(field)) {
        String init = ctx.extractInitializerSource(field);
        if (init != null && !init.isEmpty()) {
          String safe = ctx.coerceSafeInitializer(field, init);
          if (safe != null && !safe.isEmpty()) {
            inheritedDefaultInitializers.put(field, safe);
          }
        }
      }
    }

    return InheritedBuilderDefaultScan.ofMutable(
        inheritedDefaultInitializers, extraImportsForInheritedDefaults);
  }

  private static final class Context {
    private final Trees trees;
    private final Set<String> extraImportsForInheritedDefaults;

    Context(Trees trees, Set<String> extraImportsForInheritedDefaults) {
      this.trees = trees;
      this.extraImportsForInheritedDefaults = extraImportsForInheritedDefaults;
    }

    boolean hasExistingBuilderDefault(VariableElement field) {
      return field.getAnnotationMirrors().stream()
          .anyMatch(
              mirror -> mirror.getAnnotationType().toString().equals("lombok.Builder.Default"));
    }

    String extractInitializerSource(VariableElement field) {
      try {
        TreePath path = trees.getPath(field);
        if (path == null) {
          return null;
        }
        if (!(path.getLeaf() instanceof VariableTree variableTree)) {
          return null;
        }
        com.sun.source.tree.ExpressionTree initializer = variableTree.getInitializer();
        if (initializer == null) {
          return null;
        }
        long start =
            trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), initializer);
        long end =
            trees.getSourcePositions().getEndPosition(path.getCompilationUnit(), initializer);
        if (start < 0 || end < 0) {
          return null;
        }
        CharSequence content = path.getCompilationUnit().getSourceFile().getCharContent(true);
        return content.subSequence((int) start, (int) end).toString().trim();
      } catch (Exception e) {
        return null;
      }
    }

    String coerceSafeInitializer(VariableElement field, String initializerText) {
      try {
        TreePath path = trees.getPath(field);
        if (path == null
            || !(path.getLeaf() instanceof VariableTree vt)
            || vt.getInitializer() == null) {
          return null;
        }
        com.sun.source.tree.ExpressionTree expr = vt.getInitializer();
        Tree.Kind kind = expr.getKind();

        if (kind == Tree.Kind.INT_LITERAL
            || kind == Tree.Kind.LONG_LITERAL
            || kind == Tree.Kind.FLOAT_LITERAL
            || kind == Tree.Kind.DOUBLE_LITERAL
            || kind == Tree.Kind.BOOLEAN_LITERAL
            || kind == Tree.Kind.CHAR_LITERAL
            || kind == Tree.Kind.STRING_LITERAL
            || kind == Tree.Kind.NULL_LITERAL) {
          return initializerText;
        }

        if (isEnumType(field)
            && (kind == Tree.Kind.MEMBER_SELECT || kind == Tree.Kind.IDENTIFIER)) {
          String enumSimple =
              ((TypeElement) ((DeclaredType) field.asType()).asElement())
                  .getSimpleName()
                  .toString();
          String constant;
          if (kind == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mst = (MemberSelectTree) expr;
            constant = mst.getIdentifier().toString();
          } else {
            IdentifierTree id = (IdentifierTree) expr;
            constant = id.getName().toString();
          }
          return enumSimple + "." + constant;
        }

        if (kind == Tree.Kind.NEW_CLASS) {
          NewClassTree nct = (NewClassTree) expr;
          if (nct.getArguments() != null && !nct.getArguments().isEmpty()) {
            return null;
          }
          String typeName = typeIdentifierToString(nct.getIdentifier());
          if (typeName == null) {
            return null;
          }
          if (typeName.endsWith("ArrayList")
              || typeName.endsWith("HashSet")
              || typeName.endsWith("HashMap")) {
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
            return initializerText + "";
          }
          if (select.endsWith("Collections.emptyList")
              || select.endsWith("Collections.emptySet")
              || select.endsWith("Collections.emptyMap")) {
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

    private static String typeIdentifierToString(Tree identifier) {
      if (identifier instanceof IdentifierTree id) {
        return id.getName().toString();
      }
      if (identifier instanceof MemberSelectTree mst) {
        return typeIdentifierToString(mst.getExpression()) + "." + mst.getIdentifier().toString();
      }
      return null;
    }

    private static String methodSelectToString(ExpressionTree select) {
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

    private static boolean isEnumType(VariableElement field) {
      return field.asType().getKind() == TypeKind.DECLARED
          && ((DeclaredType) field.asType()).asElement().getKind() == ElementKind.ENUM;
    }
  }
}
