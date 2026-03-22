package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.VariableElement;

/** Reads the exact initializer source text for a field from the AST. */
final class VariableInitializerTextExtractor {

  private final Trees trees;

  VariableInitializerTextExtractor(Trees trees) {
    this.trees = trees;
  }

  String extractInitializerSubstring(VariableElement field) {
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
      long start =
          trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), initializer);
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
}
