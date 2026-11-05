package com.deepu.minijava.parser.ast;

import java.util.stream.Collectors;

/** Turns an AST into short readable text. Used by the --ast option and by the tests. */
public final class AstPrinter {

    private AstPrinter() {
    }

    /** One line for each statement. */
    public static String print(Program program) {
        return program.statements().stream()
                .map(statement -> printStatement(statement))
                .collect(Collectors.joining("\n"));
    }

    private static String printStatement(Stmt statement) {
        return switch (statement) {
            case VariableDeclaration d -> "(var " + d.name() + " " + printExpression(d.initializer()) + ")";
            case AssignStatement a -> "(assign " + a.name() + " " + printExpression(a.value()) + ")";
            case PrintStatement p -> "(print " + printExpression(p.expression()) + ")";
            case ExpressionStatement e -> "(expr " + printExpression(e.expression()) + ")";
        };
    }

    private static String printExpression(Expr expression) {
        return switch (expression) {
            case NumberExpr n -> String.valueOf(n.value());
            case VariableExpr v -> v.name();
            case BinaryExpr b -> "(" + b.operator().symbol() + " "
                    + printExpression(b.left()) + " " + printExpression(b.right()) + ")";
            case UnaryExpr u -> "(" + operatorName(u.operator()) + " " + printExpression(u.operand()) + ")";
        };
    }

    private static String operatorName(UnaryOperator operator) {
        return switch (operator) {
            case NEGATE -> "neg";
        };
    }
}