package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;

/** String forms of selected AST fragments (same behavior as the original inlined helpers). */
final class AstTreeStringUtils {

  private AstTreeStringUtils() {}

  static String typeIdentifierToString(Tree identifier) {
    if (identifier instanceof IdentifierTree id) {
      return id.getName().toString();
    }
    if (identifier instanceof MemberSelectTree mst) {
      return typeIdentifierToString(mst.getExpression()) + "." + mst.getIdentifier().toString();
    }
    return null;
  }

  static String methodSelectToString(ExpressionTree select) {
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
}
