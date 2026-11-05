package com.deepu.minijava.parser.ast;

import java.util.List;

/** The whole program: a list of statements. */
public record Program(List<Stmt> statements) {

    // each index of statements will contain one full statement of code like "int x=10;" this whole thing will be stored.
    public Program {
        statements = List.copyOf(statements);   // keep our own copy, so nobody can change it later
    }
}