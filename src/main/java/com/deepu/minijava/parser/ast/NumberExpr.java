package com.deepu.minijava.parser.ast;

/** A whole number, like 21. */
public record NumberExpr(int value, int line, int column) implements Expr {
}