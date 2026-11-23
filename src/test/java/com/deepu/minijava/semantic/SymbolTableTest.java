package com.deepu.minijava.semantic;

import com.deepu.minijava.diagnostics.SemanticException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {

    @Test
    void declareAndLookup() {
        SymbolTable table = new SymbolTable();
        table.declare("x", Type.INT, 1, 5);
        Symbol symbol = table.lookup("x").orElseThrow();
        assertEquals("x", symbol.name());
        assertEquals(Type.INT, symbol.type());
        assertEquals(1, symbol.line());
    }

    @Test
    void lookupOfUnknownNameIsEmpty() {
        assertTrue(new SymbolTable().lookup("y").isEmpty());
    }

    @Test
    void duplicateDeclarationFails() {
        SymbolTable table = new SymbolTable();
        table.declare("x", Type.INT, 1, 5);
        SemanticException e = assertThrows(SemanticException.class,
                () -> table.declare("x", Type.INT, 3, 5));
        assertEquals("Variable 'x' is already declared on line 1", e.getMessage());
        assertEquals(3, e.getLine());
        assertEquals(5, e.getColumn());
    }

    @Test
    void innerScopeSeesOuterVariable() {
        SymbolTable table = new SymbolTable();
        table.declare("x", Type.INT, 1, 5);
        table.enterScope();
        assertTrue(table.lookup("x").isPresent());
    }

    @Test
    void innerVariableDisappearsAfterExit() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.declare("y", Type.INT, 2, 5);
        table.exitScope();
        assertTrue(table.lookup("y").isEmpty());
    }

    @Test
    void innerScopeCannotReuseAnOuterName() {
        SymbolTable table = new SymbolTable();
        table.declare("x", Type.INT, 1, 5);
        table.enterScope();
        assertThrows(SemanticException.class, () -> table.declare("x", Type.INT, 2, 5));
    }

    @Test
    void siblingScopesMayReuseTheSameName() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.declare("y", Type.INT, 1, 5);
        table.exitScope();
        table.enterScope();
        assertDoesNotThrow(() -> table.declare("y", Type.INT, 2, 5));
    }

    @Test
    void globalScopeCannotBeClosed() {
        SymbolTable table = new SymbolTable();
        assertThrows(IllegalStateException.class, table::exitScope);
    }
}