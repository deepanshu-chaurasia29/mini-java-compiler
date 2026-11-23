package com.deepu.minijava.codegen;

import com.deepu.minijava.codegen.Instruction.*;
import com.deepu.minijava.parser.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 4 of the compiler: turns a checked AST into a list of MiniVM instructions.
 * It expects a program that the SemanticAnalyzer has already accepted.
 */
public class CodeGenerator {

    private final List<Instruction> code = new ArrayList<>();

    public List<Instruction> generate(Program program) {
        code.clear();
        for (Stmt statement : program.statements()) {
            generateStatement(statement);
        }
        return List.copyOf(code);
    }

    private void generateStatement(Stmt statement) {
        switch (statement) {
            case VariableDeclaration d -> {
                generateExpression(d.initializer());
                code.add(new Store(d.name()));
            }
            case AssignStatement a -> {
                generateExpression(a.value());
                code.add(new Store(a.name()));
            }
            case PrintStatement p -> {
                generateExpression(p.expression());
                code.add(new Print());
            }
            case ExpressionStatement e -> {
                generateExpression(e.expression());
                code.add(new Pop());   // the value is not used, so throw it away
            }
        }
    }

    private void generateExpression(Expr expression) {
        switch (expression) {
            case NumberExpr n -> code.add(new Push(n.value()));
            case VariableExpr v -> code.add(new Load(v.name()));
            case BinaryExpr b -> {
                generateExpression(b.left());    // children first ...
                generateExpression(b.right());
                code.add(binaryInstruction(b.operator()));   // ... operator last
            }
            case UnaryExpr u -> {
                generateExpression(u.operand());
                code.add(unaryInstruction(u.operator()));
            }
        }
    }

    private static Instruction binaryInstruction(BinaryOperator operator) {
        return switch (operator) {
            case ADD -> new Add();
            case SUBTRACT -> new Sub();
            case MULTIPLY -> new Mul();
            case DIVIDE -> new Div();
        };
    }

    private static Instruction unaryInstruction(UnaryOperator operator) {
        return switch (operator) {
            case NEGATE -> new Neg();
        };
    }
}