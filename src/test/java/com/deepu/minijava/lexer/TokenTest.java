package com.deepu.minijava.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenTest {

    @Test
    void endColumnIsJustAfterTheLastCharacter() {
        Token token = new Token(TokenType.IDENTIFIER, "age", 1, 5);
        assertEquals(8, token.endColumn());
    }

    @Test
    void toStringIsReadable() {
        Token token = new Token(TokenType.INTEGER, "21", 3, 11);
        assertEquals("INTEGER(\"21\") 3:11", token.toString());
    }
}