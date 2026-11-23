package com.deepu.minijava.codegen;

/**
 * One instruction for MiniVM. The VM keeps a stack of integers.
 * Every instruction prints like assembly code, for example PUSH 5.
 */
public sealed interface Instruction {

    /** Puts a number on top of the stack. */
    record Push(int value) implements Instruction {
        @Override
        public String toString() { return "PUSH " + value; }
    }

    /** Reads a variable and puts its value on top of the stack. */
    record Load(String name) implements Instruction {
        @Override
        public String toString() { return "LOAD " + name; }
    }

    /** Takes the top value off the stack and saves it in a variable. */
    record Store(String name) implements Instruction {
        @Override
        public String toString() { return "STORE " + name; }
    }

    /** Takes two values off the stack and puts back left + right. */
    record Add() implements Instruction {
        @Override
        public String toString() { return "ADD"; }
    }

    /** Takes two values off the stack and puts back left - right. */
    record Sub() implements Instruction {
        @Override
        public String toString() { return "SUB"; }
    }

    /** Takes two values off the stack and puts back left * right. */
    record Mul() implements Instruction {
        @Override
        public String toString() { return "MUL"; }
    }

    /** Takes two values off the stack and puts back left / right. */
    record Div() implements Instruction {
        @Override
        public String toString() { return "DIV"; }
    }

    /** Takes one value off the stack and puts back its negative. */
    record Neg() implements Instruction {
        @Override
        public String toString() { return "NEG"; }
    }

    /** Takes the top value off the stack and prints it. */
    record Print() implements Instruction {
        @Override
        public String toString() { return "PRINT"; }
    }

    /** Takes the top value off the stack and throws it away. */
    record Pop() implements Instruction {
        @Override
        public String toString() { return "POP"; }
    }
}