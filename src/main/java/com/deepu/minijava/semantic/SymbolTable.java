package com.deepu.minijava.semantic;

import com.deepu.minijava.diagnostics.SemanticException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Remembers every declared variable. It is a stack of scopes: the top scope is the innermost one.
 * V1 uses only the global scope. In V2, every block will open and close a scope.
 */
public class SymbolTable {

    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        enterScope();   // the global scope
    }

    /** Opens a new scope on top of the stack. */
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    /** Closes the top scope. The global scope can never be closed. */
    public void exitScope() {
        if (scopes.size() == 1) {
            throw new IllegalStateException("The global scope cannot be closed");
        }
        scopes.pop();
    }

    /** Searches from the innermost scope to the global scope. */
    public Optional<Symbol> lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {   // an ArrayDeque used as a stack starts at the top
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return Optional.of(symbol);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a variable to the innermost scope.
     * A name that is already visible in ANY scope cannot be declared again (same rule as Java).
     */
    public void declare(String name, Type type, int line, int column) {
        Optional<Symbol> existing = lookup(name);
        if (existing.isPresent()) {
            throw new SemanticException("Variable '" + name + "' is already declared on line "
                    + existing.get().line(), line, column);
        }
        scopes.peek().put(name, new Symbol(name, type, line, column));
    }
}