package com.deepu.minijava.lexer;

import com.deepu.minijava.diagnostics.LexerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stage 1 of the compiler: reads the source text character by character
 * and turns it into a list of tokens.
 * Use one Lexer object for one source text and call scanTokens() only once.
 */
public class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "int", TokenType.INT,
            "print", TokenType.PRINT,
            "if", TokenType.IF,
            "else", TokenType.ELSE,
            "while", TokenType.WHILE
    );

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;        // index where the current token started
    private int current = 0;      // index of the next character to read
    private int line = 1;         // line of the next character to read
    private int column = 1;       // column of the next character to read
    private int startLine = 1;    // line where the current token started
    private int startColumn = 1;  // column where the current token started

    public Lexer(String source) {
        this.source = source;
    }

    /** Reads the whole source text. The last token is always EOF. */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            startLine = line;
            startColumn = column;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /** Reads one token (or skips whitespace and comments). */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ', '\t', '\r', '\n' -> { }   // whitespace: ignore it
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);
            case '=' -> addToken(TokenType.EQUAL);
            case '/' -> {
                if (match('/')) {
                    // A comment: skip everything until the end of the line.
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isIdentifierStart(c)) {
                    identifier();
                } else {
                    throw new LexerException("Unexpected character '" + c + "'", startLine, startColumn);
                }
            }
        }
    }

    /** Reads a whole integer literal such as 21. */
    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        // A number must not touch letters or '_' (for example 12abc).
        if (isIdentifierStart(peek())) {
            while (isIdentifierPart(peek())) {
                advance();
            }
            throw new LexerException("Invalid number '" + source.substring(start, current) + "'", startLine, startColumn);
        }
        String text = source.substring(start, current);
        try {
            Integer.parseInt(text);   // only to check that it fits in a 32-bit int
        } catch (NumberFormatException e) {
            throw new LexerException("Integer literal '" + text + "' is too large (maximum is "
                    + Integer.MAX_VALUE + ")", startLine, startColumn);
        }
        addToken(TokenType.INTEGER);
    }

    /** Reads a whole word. It is a keyword (like int) or an identifier (like age). */
    private void identifier() {
        while (isIdentifierPart(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        addToken(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER));
    }

    private void addToken(TokenType type) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, startLine, startColumn));
    }

    /** Moves forward by one character and keeps line and column up to date. */
    private char advance() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    /** Looks at the next character without moving. Returns '\0' at the end. */
    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    /** Moves forward only if the next character is the expected one. */
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        advance();
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c);
    }
}