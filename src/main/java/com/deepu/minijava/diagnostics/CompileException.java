package com.deepu.minijava.diagnostics;

/**
 * Base class for every error found while compiling a MiniJava program
 * (lexical, syntax and semantic errors).
 * It extends RuntimeException, so we do not need "throws" everywhere.
 */

public abstract class CompileException extends RuntimeException {
  // abstract because only child can access this and no object creation allowed
  private final String stage;
  private final int line;
  private final int column;



  protected CompileException(String stage, String msg, int line, int column){
    super(msg);
    this.stage=stage;
    this.line=line;
    this.column=column;
  }

  public String getStage() {
    return stage;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  /** Text shown to the user. Example: [Lexical Error] line 1, column 11: Unexpected character '@' */
  public String format() {
    return "[" + stage + " Error] line " + line + ", column " + column + ": " + getMessage();
  }


}
