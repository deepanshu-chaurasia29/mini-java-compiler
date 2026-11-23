package com.deepu.minijava.semantic;

import com.deepu.minijava.diagnostics.SemanticException;
import com.deepu.minijava.parser.ast.*;

/**
 * Stage 3 of the compiler: checks that a program makes sense.
 * It stops at the first error. Use one analyzer object for one program.
 */
public class SemanticAnalyzer {

    private final SymbolTable symbols = new SymbolTable();

    /** Throws a SemanticException if the program is not valid. Returns normally if it is valid. */
    public void analyze(Program program) {
        for (Stmt statement : program.statements()) {
            checkStatement(statement);
        }
    }

    private void checkStatement(Stmt statement) {
        switch (statement) {
            case VariableDeclaration d -> {
                Type valueType = typeOf(d.initializer());                      // 1. check the value first
                checkAssignable(Type.INT, valueType, d.line(), d.column());    // 2. V1: every variable is an int
                symbols.declare(d.name(), Type.INT, d.line(), d.column());     // 3. only now the name exists
            }
            case AssignStatement a -> {
                Symbol target = lookupOrFail(a.name(), a.line(), a.column());
                Type valueType = typeOf(a.value());
                checkAssignable(target.type(), valueType, a.line(), a.column());
            }
            case PrintStatement p -> typeOf(p.expression());
            case ExpressionStatement e -> typeOf(e.expression());
        }
    }

    /** Checks an expression and returns its type. */
    private Type typeOf(Expr expression) {
        return switch (expression) {
            case NumberExpr n -> Type.INT;
            case VariableExpr v -> lookupOrFail(v.name(), v.line(), v.column()).type();
            case BinaryExpr b -> {
                Type left = typeOf(b.left());
                Type right = typeOf(b.right());
                if (left != Type.INT || right != Type.INT) {
                    throw new SemanticException("Operator '" + b.operator().symbol()
                            + "' needs two int values, but got " + left.displayName()
                            + " and " + right.displayName(), b.line(), b.column());
                }
                yield Type.INT;
            }
            case UnaryExpr u -> {
                Type operand = typeOf(u.operand());
                if (operand != Type.INT) {
                    throw new SemanticException("Operator '-' needs an int value, but got "
                            + operand.displayName(), u.line(), u.column());
                }
                yield Type.INT;
            }
        };
    }

    private Symbol lookupOrFail(String name, int line, int column) {
        return symbols.lookup(name).orElseThrow(
                () -> new SemanticException("Variable '" + name + "' is not declared", line, column));
    }

    private void checkAssignable(Type target, Type value, int line, int column) {
        if (target != value) {
            throw new SemanticException("Cannot assign a value of type " + value.displayName()
                    + " to a variable of type " + target.displayName(), line, column);
        }
    }
}