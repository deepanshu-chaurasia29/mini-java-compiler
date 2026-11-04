
package com.deepu.minijava.parser.ast;

/** Using a variable, like age in print(age). */
public record VariableExpr(String name, int line, int column) implements Expr {
}