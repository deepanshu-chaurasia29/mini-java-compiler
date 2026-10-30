package com.deepu.minijava.lexer;

import com.deepu.minijava.diagnostics.LexerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.deepu.minijava.lexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    private List<Token> scan(String source) {
        return new Lexer(source).scanTokens();
    }

    private List<TokenType> types(String source) {
        return scan(source).stream().map(Token::type).toList();
    }

    @Test
    void emptySourceGivesOnlyEof() {
        List<Token> tokens = scan("");
        assertEquals(1, tokens.size());
        assertEquals(EOF, tokens.get(0).type());
    }

    @Test
    void variableDeclaration() {
        List<Token> tokens = scan("int age = 21;");
        assertEquals(List.of(INT, IDENTIFIER, EQUAL, INTEGER, SEMICOLON, EOF),
                tokens.stream().map(Token::type).toList());
        assertEquals("age", tokens.get(1).lexeme());
        assertEquals("21", tokens.get(3).lexeme());
    }

    @Test
    void keywordsAreNotIdentifiers() {
        assertEquals(List.of(INT, IDENTIFIER, PRINT, IDENTIFIER, IF, ELSE, WHILE, EOF),
                types("int integer print printer if else while"));
    }

    @Test
    void operatorsAndSymbols() {
        assertEquals(List.of(PLUS, MINUS, STAR, SLASH, EQUAL, LEFT_PAREN, RIGHT_PAREN,
                        LEFT_BRACE, RIGHT_BRACE, SEMICOLON, EOF),
                types("+ - * / = ( ) { } ;"));
    }

    @Test
    void lineCommentIsSkipped() {
        List<Token> tokens = scan("// hello\nint x;");
        assertEquals(List.of(INT, IDENTIFIER, SEMICOLON, EOF),
                tokens.stream().map(Token::type).toList());
        assertEquals(2, tokens.get(0).line());
    }

    @Test
    void commentAtEndOfFileWithoutNewline() {
        assertEquals(List.of(INT, IDENTIFIER, SEMICOLON, EOF), types("int x; // the end"));
    }

    @Test
    void singleSlashIsDivision() {
        assertEquals(List.of(INTEGER, SLASH, INTEGER, EOF), types("10 / 2"));
    }

    @Test
    void positionsOnOneLine() {
        List<Token> tokens = scan("int age = 21;");
        int[] expectedColumns = {1, 5, 9, 11, 13, 14};
        for (int i = 0; i < expectedColumns.length; i++) {
            assertEquals(1, tokens.get(i).line());
            assertEquals(expectedColumns[i], tokens.get(i).column());
        }
    }

    @Test
    void positionsAcrossLines() {
        List<Token> tokens = scan("int a;\n  int b;");
        Token secondInt = tokens.get(3);
        assertEquals(INT, secondInt.type());
        assertEquals(2, secondInt.line());
        assertEquals(3, secondInt.column());
        assertEquals(7, tokens.get(4).column());   // the name b
    }

    @Test
    void windowsLineEndings() {
        List<Token> tokens = scan("int a;\r\nint b;");
        assertEquals(2, tokens.get(3).line());
        assertEquals(1, tokens.get(3).column());
    }

    @Test
    void identifiersWithDigitsAndUnderscore() {
        List<Token> tokens = scan("total_1 _x x2");
        assertEquals(List.of(IDENTIFIER, IDENTIFIER, IDENTIFIER, EOF),
                tokens.stream().map(Token::type).toList());
        assertEquals("total_1", tokens.get(0).lexeme());
        assertEquals("_x", tokens.get(1).lexeme());
        assertEquals("x2", tokens.get(2).lexeme());
    }

    @Test
    void largestIntIsAccepted() {
        List<Token> tokens = scan("2147483647");
        assertEquals(INTEGER, tokens.get(0).type());
        assertEquals("2147483647", tokens.get(0).lexeme());
    }

    @Test
    void invalidCharacter() {
        LexerException e = assertThrows(LexerException.class, () -> scan("int x = 5 @ 3;"));
        assertEquals("Unexpected character '@'", e.getMessage());
        assertEquals(1, e.getLine());
        assertEquals(11, e.getColumn());
    }

    @Test
    void invalidCharacterOnSecondLine() {
        LexerException e = assertThrows(LexerException.class, () -> scan("int a;\n@"));
        assertEquals(2, e.getLine());
        assertEquals(1, e.getColumn());
    }

    @Test
    void numberFollowedByLettersIsAnError() {
        LexerException e = assertThrows(LexerException.class, () -> scan("12abc"));
        assertEquals("Invalid number '12abc'", e.getMessage());
        assertEquals(1, e.getColumn());
    }

    @Test
    void integerTooLargeIsAnError() {
        LexerException e = assertThrows(LexerException.class, () -> scan("99999999999"));
        assertTrue(e.getMessage().contains("too large"));
    }

    @Test
    void printStatement() {
        assertEquals(List.of(PRINT, LEFT_PAREN, INTEGER, PLUS, INTEGER, STAR, INTEGER,
                        RIGHT_PAREN, SEMICOLON, EOF),
                types("print(2 + 3 * 4);"));
    }
}