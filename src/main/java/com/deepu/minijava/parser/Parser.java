package com.deepu.minijava.parser;

import com.deepu.minijava.diagnostics.ParseException;
import com.deepu.minijava.lexer.Token;
import com.deepu.minijava.lexer.TokenType;
import com.deepu.minijava.parser.ast.*;

import java.util.ArrayList;
import java.util.List;

import static com.deepu.minijava.lexer.TokenType.*;

/**
 * Stage 2 of the compiler: turns a list of tokens into an AST.
 * It is a hand-written recursive-descent parser: one method for each grammar rule.
 * The token list must end with an EOF token (the Lexer always adds it).
 * The parser stops at the first error.
 */
public class Parser {

  private final List<Token> tokens;
  private int current = 0;   // index of the token we are looking at

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  /** program = { statement } EOF ; */
  public Program parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(statement());
    }
    return new Program(statements);
  }

  // ---------- Statements ----------

  /** statement = varDecl | assignStmt | printStmt | exprStmt ; */
  private Stmt statement() {
    if (check(INT)) {
      return varDeclaration();
    }
    if (check(PRINT)) {
      return printStatement();
    }
    if (check(IDENTIFIER) && checkNext(EQUAL)) {
      return assignmentStatement();
    }
    // These words and symbols are reserved for V2 (control flow and blocks).
    if (check(IF) || check(ELSE) || check(WHILE) || check(LEFT_BRACE) || check(RIGHT_BRACE)) {
      throw errorAtCurrent("Expected a statement");
    }
    return expressionStatement();
  }

  /** varDecl = "int" IDENTIFIER "=" expression ";" ; */
  private Stmt varDeclaration() {
    advance();   // the word int
    Token name = consume(IDENTIFIER, "Expected a variable name after 'int'");
    consume(EQUAL, "Expected '=' after the variable name");
    Expr initializer = expression();
    consume(SEMICOLON, "Expected ';' after the variable declaration");
    return new VariableDeclaration(name.lexeme(), initializer, name.line(), name.column());
  }

  /** assignStmt = IDENTIFIER "=" expression ";" ; */
  private Stmt assignmentStatement() {
    Token name = advance();   // the variable name
    advance();                // the = sign
    Expr value = expression();
    consume(SEMICOLON, "Expected ';' after the assignment");
    return new AssignStatement(name.lexeme(), value, name.line(), name.column());
  }

  /** printStmt = "print" "(" expression ")" ";" ; */
  private Stmt printStatement() {
    Token keyword = advance();   // the word print
    consume(LEFT_PAREN, "Expected '(' after 'print'");
    Expr value = expression();
    consume(RIGHT_PAREN, "Expected ')' after the value to print");
    consume(SEMICOLON, "Expected ';' after the print statement");
    return new PrintStatement(value, keyword.line(), keyword.column());
  }

  /** exprStmt = expression ";" ; */
  private Stmt expressionStatement() {
    Token first = peek();
    Expr expression = expression();
    consume(SEMICOLON, "Expected ';' after the expression");
    return new ExpressionStatement(expression, first.line(), first.column());
  }

  // ---------- Expressions (from the weakest to the strongest operator) ----------

  /** expression = term { ( "+" | "-" ) term } ; */
  private Expr expression() {
    Expr left = term();
    while (match(PLUS, MINUS)) {
      Token operator = previous();
      Expr right = term();
      BinaryOperator kind = operator.type() == PLUS ? BinaryOperator.ADD : BinaryOperator.SUBTRACT;
      left = new BinaryExpr(left, kind, right, operator.line(), operator.column());
    }
    return left;
  }

  /** term = unary { ( "*" | "/" ) unary } ; */
  private Expr term() {
    Expr left = unary();
    while (match(STAR, SLASH)) {
      Token operator = previous();
      Expr right = unary();
      BinaryOperator kind = operator.type() == STAR ? BinaryOperator.MULTIPLY : BinaryOperator.DIVIDE;
      left = new BinaryExpr(left, kind, right, operator.line(), operator.column());
    }
    return left;
  }

  /** unary = "-" unary | primary ; */
  private Expr unary() {
    if (match(MINUS)) {
      Token operator = previous();
      Expr operand = unary();   // calls itself, so - -5 works
      return new UnaryExpr(UnaryOperator.NEGATE, operand, operator.line(), operator.column());
    }
    return primary();
  }

  /** primary = INTEGER | IDENTIFIER | "(" expression ")" ; */
  private Expr primary() {
    if (match(INTEGER)) {
      Token number = previous();
      // The Lexer already checked that the number fits in an int.
      return new NumberExpr(Integer.parseInt(number.lexeme()), number.line(), number.column());
    }
    if (match(IDENTIFIER)) {
      Token name = previous();
      return new VariableExpr(name.lexeme(), name.line(), name.column());
    }
    if (match(LEFT_PAREN)) {
      Expr inside = expression();
      consume(RIGHT_PAREN, "Expected ')' after the expression");
      return inside;   // brackets need no node of their own: the tree shape shows the grouping
    }
    throw errorAtCurrent("Expected an expression");
  }

  // ---------- Helper methods ----------

  /** If the current token has one of these types, move forward and return true. */
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  /** The current token MUST have this type. If not, throw a syntax error. */
  private Token consume(TokenType type, String expected) {
    if (check(type)) {
      return advance();
    }
    throw errorAfterPrevious(expected);
  }

  /** Is the current token of this type? Does not move. */
  private boolean check(TokenType type) {
    return peek().type() == type;
  }

  /** Is the token after the current one of this type? Does not move. (One token of lookahead.) */
  private boolean checkNext(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return tokens.get(current + 1).type() == type;
  }

  /** Moves forward by one token and returns the token we just left. */
  private Token advance() {
    if (!isAtEnd()) {
      current++;
    }
    return previous();
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private boolean isAtEnd() {
    return peek().type() == EOF;
  }

  // ---------- Errors ----------

  /** The error points at the token we are looking at. */
  private ParseException errorAtCurrent(String expected) {
    Token found = peek();
    return new ParseException(expected + ", but found " + describe(found), found.line(), found.column());
  }

  /** The error points just after the previous token. A missing ; is then shown on the line where it is missing. */
  private ParseException errorAfterPrevious(String expected) {
    if (current == 0) {
      return errorAtCurrent(expected);
    }
    Token previous = previous();
    return new ParseException(expected + ", but found " + describe(peek()), previous.line(), previous.endColumn());
  }

  private static String describe(Token token) {
    return token.type() == EOF ? "end of file" : "'" + token.lexeme() + "'";
  }
}