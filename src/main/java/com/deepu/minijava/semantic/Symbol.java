package com.deepu.minijava.semantic;

/** One entry of the symbol table: a declared variable. Line and column are where its name was declared. */
public record Symbol(String name, Type type, int line, int column) {
}