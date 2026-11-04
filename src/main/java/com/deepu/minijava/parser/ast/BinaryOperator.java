package com.deepu.minijava.parser.ast;

/** The operators that take two values. Comparison operators are added in V2. */
public enum BinaryOperator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");

    private final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}