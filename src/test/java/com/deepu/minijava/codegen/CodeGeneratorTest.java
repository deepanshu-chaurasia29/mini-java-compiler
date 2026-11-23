package com.deepu.minijava.codegen;

import com.deepu.minijava.lexer.Lexer;
import com.deepu.minijava.parser.Parser;
import com.deepu.minijava.parser.ast.Program;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorTest {

    private Program parse(String source) {
        return new Parser(new Lexer(source).scanTokens()).parse();
    }

    /** Generates code and returns each instruction as text, for example "PUSH 2". */
    private List<String> generate(String source) {
        return new CodeGenerator().generate(parse(source)).stream()
                .map(Instruction::toString)
                .toList();
    }

    @Test
    void numberLiteral() {
        assertEquals(List.of("PUSH 5", "PRINT"), generate("print(5);"));
    }

    @Test
    void variableDeclaration() {
        assertEquals(List.of("PUSH 5", "STORE x"), generate("int x = 5;"));
    }

    @Test
    void variableLoad() {
        assertEquals(List.of("LOAD x", "PRINT"), generate("print(x);"));
    }

    @Test
    void assignment() {
        assertEquals(List.of("PUSH 7", "STORE x"), generate("x = 7;"));
    }

    @Test
    void addition() {
        assertEquals(List.of("PUSH 2", "PUSH 3", "ADD", "PRINT"), generate("print(2 + 3);"));
    }

    @Test
    void multiplicationComesBeforeAddition() {
        assertEquals(List.of("PUSH 2", "PUSH 3", "PUSH 4", "MUL", "ADD", "PRINT"),
                generate("print(2 + 3 * 4);"));
    }

    @Test
    void parenthesesChangeTheOrder() {
        assertEquals(List.of("PUSH 2", "PUSH 3", "ADD", "PUSH 4", "MUL", "PRINT"),
                generate("print((2 + 3) * 4);"));
    }

    @Test
    void subtractionKeepsLeftToRightOrder() {
        assertEquals(List.of("PUSH 10", "PUSH 4", "SUB", "PUSH 3", "SUB", "PRINT"),
                generate("print(10 - 4 - 3);"));
    }

    @Test
    void division() {
        assertEquals(List.of("PUSH 8", "PUSH 2", "DIV", "PRINT"), generate("print(8 / 2);"));
    }

    @Test
    void unaryMinus() {
        assertEquals(List.of("PUSH 5", "NEG", "PUSH 8", "ADD", "PRINT"), generate("print(-5 + 8);"));
    }

    @Test
    void expressionStatementEndsWithPop() {
        assertEquals(List.of("PUSH 5", "PUSH 3", "ADD", "POP"), generate("5 + 3;"));
    }

    @Test
    void variablesInsideAnExpression() {
        assertEquals(List.of("LOAD a", "LOAD b", "MUL", "LOAD c", "ADD", "STORE z"),
                generate("int z = a * b + c;"));
    }

    @Test
    void helloProgram() {
        String source = "int age = 21;\nint marks = 85;\nprint(age);\nprint(marks);\nprint(10 + 20 * 3);";
        assertEquals(List.of(
                "PUSH 21", "STORE age",
                "PUSH 85", "STORE marks",
                "LOAD age", "PRINT",
                "LOAD marks", "PRINT",
                "PUSH 10", "PUSH 20", "PUSH 3", "MUL", "ADD", "PRINT"), generate(source));
    }

    @Test
    void emptyProgramGivesNoInstructions() {
        assertEquals(List.of(), generate(""));
    }

    @Test
    void instructionsCanBeComparedWithEquals() {
        List<Instruction> code = new CodeGenerator().generate(parse("print(2 + 3);"));
        assertEquals(List.of(new Instruction.Push(2), new Instruction.Push(3),
                new Instruction.Add(), new Instruction.Print()), code);
    }

    @Test
    void oneGeneratorCanBeUsedTwice() {
        CodeGenerator generator = new CodeGenerator();
        generator.generate(parse("print(1);"));
        List<Instruction> second = generator.generate(parse("print(2);"));
        assertEquals(2, second.size());   // the first program's instructions are not kept
    }
}