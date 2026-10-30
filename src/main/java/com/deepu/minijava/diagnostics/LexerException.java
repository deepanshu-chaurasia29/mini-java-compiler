package com.deepu.minijava.diagnostics;

/** Thrown by the Lexer when the source text contains something that is not valid MiniJava. */
public class LexerException extends CompileException {
  public LexerException(String message,int line,int column){
    super("Lexical",message,line,column);
  }
}
