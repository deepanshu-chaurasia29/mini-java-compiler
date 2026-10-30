package com.deepu.minijava.lexer;
/**
 * One small meaningful piece of the source code.
 *
 * @param type   what kind of token it is
 * @param lexeme the exact text from the file, for example "age" or "21"
 * @param line   line number where the token starts (first line is 1)
 * @param column column number where the token starts (first column is 1)
 */
public record Token(TokenType type,String lexeme,int line, int column) {
  /** Column just after the last character of this token. Used for error positions. */
  public int endColumn(){
    return column + lexeme.length();
  }

  @Override
  public String toString() {
    return type + "(\"" + lexeme + "\") " + line + ":" + column;
  }
}
