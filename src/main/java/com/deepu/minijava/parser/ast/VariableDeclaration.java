package com.deepu.minijava.parser.ast;

/** int name = initializer; Line and column are the position of the variable name. */
public record VariableDeclaration(String name, Expr initializer, int line, int column) implements Stmt {
}