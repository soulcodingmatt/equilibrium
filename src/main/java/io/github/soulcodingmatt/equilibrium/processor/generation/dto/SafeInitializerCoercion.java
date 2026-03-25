package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import static io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports.TypeNames.*;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

/**
 * Classifies initializer expressions that are safe to copy onto generated DTO fields and records
 * any extra imports they require.
 */
final class SafeInitializerCoercion {

  private final Trees trees;
  private final Set<String> extraImportsForInheritedDefaults;

  SafeInitializerCoercion(Trees trees, Set<String> extraImportsForInheritedDefaults) {
    this.trees = trees;
    this.extraImportsForInheritedDefaults = extraImportsForInheritedDefaults;
  }

  String coerce(VariableElement field, String initializerText) {
    try {
      TreePath path = trees.getPath(field);
      if (path == null
          || !(path.getLeaf() instanceof VariableTree vt)
          || vt.getInitializer() == null) {
        return null;
      }
      ExpressionTree expr = vt.getInitializer();
      Tree.Kind kind = expr.getKind();

      if (isLiteralKind(kind)) {
        return initializerText;
      }

      String enumConstant = tryEnumConstant(field, kind, expr);
      if (enumConstant != null) {
        return enumConstant;
      }

      String fromNew = tryParameterlessNew(kind, expr, initializerText);
      if (fromNew != null) {
        return fromNew;
      }

      return tryParameterlessCall(kind, expr, initializerText);
    } catch (Exception e) {
      return null;
    }
  }

  private static boolean isLiteralKind(Tree.Kind kind) {
    return kind == Tree.Kind.INT_LITERAL
        || kind == Tree.Kind.LONG_LITERAL
        || kind == Tree.Kind.FLOAT_LITERAL
        || kind == Tree.Kind.DOUBLE_LITERAL
        || kind == Tree.Kind.BOOLEAN_LITERAL
        || kind == Tree.Kind.CHAR_LITERAL
        || kind == Tree.Kind.STRING_LITERAL
        || kind == Tree.Kind.NULL_LITERAL;
  }

  private String tryEnumConstant(VariableElement field, Tree.Kind kind, ExpressionTree expr) {
    if (!isEnumType(field)) {
      return null;
    }
    if (kind != Tree.Kind.MEMBER_SELECT && kind != Tree.Kind.IDENTIFIER) {
      return null;
    }
    String enumSimple = (((DeclaredType) field.asType()).asElement()).getSimpleName().toString();
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

  private String tryParameterlessNew(Tree.Kind kind, ExpressionTree expr, String initializerText) {
    if (kind != Tree.Kind.NEW_CLASS) {
      return null;
    }
    NewClassTree nct = (NewClassTree) expr;
    if (nct.getArguments() != null && !nct.getArguments().isEmpty()) {
      return null;
    }
    String typeName = AstTreeStringUtils.typeIdentifierToString(nct.getIdentifier());
    if (typeName == null) {
      return null;
    }
    if (typeName.endsWith("ArrayList")) {
      extraImportsForInheritedDefaults.add(JAVA_UTIL_LIST);
      extraImportsForInheritedDefaults.add("java.util.ArrayList");
      return initializerText;
    }
    if (typeName.endsWith("HashSet")) {
      extraImportsForInheritedDefaults.add(JAVA_UTIL_SET);
      extraImportsForInheritedDefaults.add("java.util.HashSet");
      return initializerText;
    }
    if (typeName.endsWith("HashMap")) {
      extraImportsForInheritedDefaults.add(JAVA_UTIL_MAP);
      extraImportsForInheritedDefaults.add("java.util.HashMap");
      return initializerText;
    }
    return null;
  }

  private String tryParameterlessCall(Tree.Kind kind, ExpressionTree expr, String initializerText) {
    if (kind != Tree.Kind.METHOD_INVOCATION) {
      return null;
    }
    MethodInvocationTree mit = (MethodInvocationTree) expr;
    if (mit.getArguments() != null && !mit.getArguments().isEmpty()) {
      return null;
    }
    String select = AstTreeStringUtils.methodSelectToString(mit.getMethodSelect());
    if (select == null) {
      return null;
    }
    if (select.endsWith("Optional.empty")) {
      extraImportsForInheritedDefaults.add(JAVA_UTIL_OPTIONAL);
      return initializerText;
    }
    if (select.endsWith("Collections.emptyList")
        || select.endsWith("Collections.emptySet")
        || select.endsWith("Collections.emptyMap")) {
      extraImportsForInheritedDefaults.add("java.util.Collections");
      return initializerText;
    }
    return null;
  }

  private static boolean isEnumType(VariableElement field) {
    return field.asType().getKind() == TypeKind.DECLARED
        && ((DeclaredType) field.asType()).asElement().getKind() == ElementKind.ENUM;
  }
}
