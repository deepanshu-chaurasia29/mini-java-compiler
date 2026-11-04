package com.deepu.minijava.parser.ast;

/** Two values with an operator between them. Line and column are the position of the operator. */
public record BinaryExpr(Expr left, BinaryOperator operator, Expr right, int line, int column) implements Expr {
}