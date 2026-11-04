package com.deepu.minijava.parser.ast;

import java.util.List;

/** The whole program: a list of statements. */
public record Program(List<Stmt> statements) {

    public Program {
        statements = List.copyOf(statements);   // keep our own copy, so nobody can change it later
    }
}