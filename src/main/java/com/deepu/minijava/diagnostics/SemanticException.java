package com.deepu.minijava.diagnostics;

/** Thrown by the SemanticAnalyzer when a program is grammatically correct but does not make sense. */
public class SemanticException extends CompileException {

    public SemanticException(String message, int line, int column) {
        super("Semantic", message, line, column);
    }
}