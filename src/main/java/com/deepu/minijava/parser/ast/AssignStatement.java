package com.deepu.minijava.parser.ast;

/** name = value; Line and column are the position of the variable name. */
public record AssignStatement(String name, Expr value, int line, int column) implements Stmt {
}