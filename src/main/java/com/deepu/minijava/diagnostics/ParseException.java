package com.deepu.minijava.diagnostics;

/** Thrown by the Parser when the tokens do not follow the grammar of MiniJava. */
public class ParseException extends CompileException {

    public ParseException(String message, int line, int column) {
        super("Syntax", message, line, column);
    }
}