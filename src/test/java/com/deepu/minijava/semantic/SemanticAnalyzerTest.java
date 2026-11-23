package com.deepu.minijava.semantic;

import com.deepu.minijava.diagnostics.SemanticException;
import com.deepu.minijava.lexer.Lexer;
import com.deepu.minijava.parser.Parser;
import com.deepu.minijava.parser.ast.Program;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {

    private void analyze(String source) {
        Program program = new Parser(new Lexer(source).scanTokens()).parse();
        new SemanticAnalyzer().analyze(program);
    }

    private void assertSemanticError(String source, String message, int line, int column) {
        SemanticException e = assertThrows(SemanticException.class, () -> analyze(source));
        assertEquals(message, e.getMessage());
        assertEquals(line, e.getLine());
        assertEquals(column, e.getColumn());
    }

    // ---------- Valid programs ----------

    @Test
    void validProgramPasses() {
        assertDoesNotThrow(() -> analyze(
                "int age = 21;\nint marks = 85;\nprint(age);\nprint(marks);\nprint(10 + 20 * 3);"));
    }

    @Test
    void assignmentToDeclaredVariable() {
        assertDoesNotThrow(() -> analyze("int x = 1;\nx = x + 2;"));
    }

    @Test
    void variableUsedInLaterStatement() {
        assertDoesNotThrow(() -> analyze("int a = 1;\nint b = a * 2;\nprint(b);"));
    }

    @Test
    void emptyProgram() {
        assertDoesNotThrow(() -> analyze(""));
    }

    @Test
    void divisionByZeroIsNotACompileError() {
        // It is a runtime error. MiniVM finds it in Sprint 4.
        assertDoesNotThrow(() -> analyze("print(1 / 0);"));
    }

    // ---------- Semantic errors ----------

    @Test
    void undeclaredVariableInPrint() {
        assertSemanticError("print(y);", "Variable 'y' is not declared", 1, 7);
    }

    @Test
    void undeclaredVariableInExpression() {
        assertSemanticError("int a = 1;\nprint(a + b);", "Variable 'b' is not declared", 2, 11);
    }

    @Test
    void variableIsNotVisibleInItsOwnInitializer() {
        assertSemanticError("int x = x + 1;", "Variable 'x' is not declared", 1, 9);
    }

    @Test
    void duplicateDeclaration() {
        assertSemanticError("int a = 1;\nint a = 2;", "Variable 'a' is already declared on line 1", 2, 5);
    }

    @Test
    void assignmentToUndeclaredVariable() {
        assertSemanticError("x = 5;", "Variable 'x' is not declared", 1, 1);
    }

    @Test
    void assignedValueIsChecked() {
        assertSemanticError("int a = 1;\na = b;", "Variable 'b' is not declared", 2, 5);
    }

    @Test
    void undeclaredVariableInUnaryExpression() {
        assertSemanticError("print(-y);", "Variable 'y' is not declared", 1, 8);
    }

    @Test
    void expressionStatementIsChecked() {
        assertSemanticError("y + 1;", "Variable 'y' is not declared", 1, 1);
    }

    @Test
    void stopsAtTheFirstError() {
        assertSemanticError("print(a);\nprint(b);", "Variable 'a' is not declared", 1, 7);
    }
}