package com.deepu.minijava.parser.ast;

/** An expression used as a statement, like 5 + 3; Line and column are the position of its first token. */
public record ExpressionStatement(Expr expression, int line, int column) implements Stmt {
}