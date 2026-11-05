package com.deepu.minijava.parser.ast;

/** print(expression); Line and column are the position of the print keyword. */
public record PrintStatement(Expr expression, int line, int column) implements Stmt {
}