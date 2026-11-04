package com.deepu.minijava.parser.ast;

/** An expression: something that has a value, like 2 + 3. */
public sealed interface Expr permits NumberExpr, VariableExpr, BinaryExpr, UnaryExpr {
}