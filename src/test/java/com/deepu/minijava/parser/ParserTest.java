package com.deepu.minijava.parser;

import com.deepu.minijava.diagnostics.ParseException;
import com.deepu.minijava.lexer.Lexer;
import com.deepu.minijava.parser.ast.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Program parse(String source) {
        return new Parser(new Lexer(source).scanTokens()).parse();
    }

    /** Parses the source and returns the tree as short text, one line per statement. */
    private String ast(String source) {
        return AstPrinter.print(parse(source));
    }

    private void assertParseError(String source, String message, int line, int column) {
        ParseException e = assertThrows(ParseException.class, () -> parse(source));
        assertEquals(message, e.getMessage());
        assertEquals(line, e.getLine());
        assertEquals(column, e.getColumn());
    }

    // ---------- Valid programs ----------

    @Test
    void emptyProgram() {
        Program program = parse("");
        assertTrue(program.statements().isEmpty());
        assertEquals("", AstPrinter.print(program));
    }

    @Test
    void variableDeclaration() {
        assertEquals("(var age 21)", ast("int age = 21;"));
    }

    @Test
    void printStatement() {
        assertEquals("(print age)", ast("print(age);"));
    }

    @Test
    void assignment() {
        assertEquals("(assign x 10)", ast("x = 10;"));
    }

    @Test
    void expressionStatement() {
        assertEquals("(expr (+ 5 3))", ast("5 + 3;"));
    }

    @Test
    void multiplicationBeforeAddition() {
        assertEquals("(print (+ 2 (* 3 4)))", ast("print(2 + 3 * 4);"));
    }

    @Test
    void parenthesesChangePrecedence() {
        assertEquals("(print (* (+ 2 3) 4))", ast("print((2 + 3) * 4);"));
    }

    @Test
    void subtractionIsLeftToRight() {
        assertEquals("(print (- (- 10 4) 3))", ast("print(10 - 4 - 3);"));
    }

    @Test
    void divisionIsLeftToRight() {
        assertEquals("(print (/ (/ 100 10) 2))", ast("print(100 / 10 / 2);"));
    }

    @Test
    void unaryMinus() {
        assertEquals("(print (+ (neg 5) 8))", ast("print(-5 + 8);"));
    }

    @Test
    void doubleUnaryMinus() {
        assertEquals("(print (neg (neg 5)))", ast("print(- -5);"));
    }

    @Test
    void unaryBindsTighterThanMultiplication() {
        assertEquals("(print (* (neg 2) 3))", ast("print(-2 * 3);"));
    }

    @Test
    void variablesInExpression() {
        assertEquals("(var z (+ (* a b) c))", ast("int z = a * b + c;"));
    }

    @Test
    void multipleStatements() {
        String source = "int age = 21;\nprint(age);\nprint(10 + 20 * 3);";
        assertEquals("(var age 21)\n(print age)\n(print (+ 10 (* 20 3)))", ast(source));
    }

    @Test
    void nodePositions() {
        Program program = parse("int x = 1 + 2;");
        VariableDeclaration declaration = (VariableDeclaration) program.statements().get(0);
        assertEquals(1, declaration.line());
        assertEquals(5, declaration.column());          // position of the name x
        BinaryExpr sum = (BinaryExpr) declaration.initializer();
        assertEquals(11, sum.column());                 // position of the + operator
    }

    @Test
    void printStatementPosition() {
        Program program = parse("\nprint(1);");
        PrintStatement statement = (PrintStatement) program.statements().get(0);
        assertEquals(2, statement.line());
        assertEquals(1, statement.column());
    }

    // ---------- Syntax errors ----------

    @Test
    void missingSemicolonAfterDeclaration() {
        assertParseError("int x = 5",
                "Expected ';' after the variable declaration, but found end of file", 1, 10);
    }

    @Test
    void missingSemicolonIsReportedOnTheLineWhereItIsMissing() {
        assertParseError("int x = 5\nprint(x);",
                "Expected ';' after the variable declaration, but found 'print'", 1, 10);
    }

    @Test
    void unmatchedLeftParenthesis() {
        assertParseError("print((2 + 3);",
                "Expected ')' after the value to print, but found ';'", 1, 14);
    }

    @Test
    void unmatchedRightParenthesis() {
        assertParseError("print(2 + 3));",
                "Expected ';' after the print statement, but found ')'", 1, 13);
    }

    @Test
    void missingClosingParenthesisInGroup() {
        assertParseError("int x = (1 + 2;",
                "Expected ')' after the expression, but found ';'", 1, 15);
    }

    @Test
    void missingExpressionAfterEquals() {
        assertParseError("int x = ;", "Expected an expression, but found ';'", 1, 9);
    }

    @Test
    void incompleteExpression() {
        assertParseError("print(2 + );", "Expected an expression, but found ')'", 1, 11);
    }

    @Test
    void missingVariableName() {
        assertParseError("int = 5;", "Expected a variable name after 'int', but found '='", 1, 4);
    }

    @Test
    void keywordCannotBeVariableName() {
        assertParseError("int print = 5;",
                "Expected a variable name after 'int', but found 'print'", 1, 4);
    }

    @Test
    void missingEqualsInDeclaration() {
        assertParseError("int x;", "Expected '=' after the variable name, but found ';'", 1, 6);
    }

    @Test
    void missingParenAfterPrint() {
        assertParseError("print 5;", "Expected '(' after 'print', but found '5'", 1, 6);
    }

    @Test
    void missingSemicolonAfterAssignment() {
        assertParseError("x = 5",
                "Expected ';' after the assignment, but found end of file", 1, 6);
    }

    @Test
    void ifIsNotSupportedYet() {
        assertParseError("if (1) print(1);", "Expected a statement, but found 'if'", 1, 1);
    }

    @Test
    void braceIsNotSupportedYet() {
        assertParseError("{", "Expected a statement, but found '{'", 1, 1);
    }

    @Test
    void strayRightParenthesis() {
        assertParseError(")", "Expected an expression, but found ')'", 1, 1);
    }
}