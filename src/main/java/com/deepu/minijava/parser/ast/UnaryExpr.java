package com.deepu.minijava.parser.ast;

/** One value with an operator in front, like -5. Line and column are the position of the operator. */
public record UnaryExpr(UnaryOperator operator, Expr operand, int line, int column) implements Expr {
}