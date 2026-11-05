package com.deepu.minijava.parser.ast;

/** A statement: one complete instruction, like int x = 5; */
public sealed interface Stmt permits VariableDeclaration, AssignStatement, PrintStatement, ExpressionStatement {
}