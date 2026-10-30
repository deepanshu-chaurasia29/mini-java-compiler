package com.deepu.minijava.lexer;

public enum TokenType {
  // Keywords
  INT,PRINT,IF,ELSE,WHILE,

  // Literals and reference var
  INTEGER,IDENTIFIER,

  // Operators
  PLUS,MINUS,STAR,SLASH,EQUAL,

  // Symbols
  LEFT_PAREN,RIGHT_PAREN,LEFT_BRACE,RIGHT_BRACE,SEMICOLON,

  // Special : marks the end of file
  EOF
}
