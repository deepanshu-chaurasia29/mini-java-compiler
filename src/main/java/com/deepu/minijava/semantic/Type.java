package com.deepu.minijava.semantic;

/** The types of MiniJava. V1 has only int. BOOLEAN is added in V2. */
public enum Type {
    INT("int");

    private final String displayName;

    Type(String displayName) {
        this.displayName = displayName;
    }

    /** The name used in error messages, for example int. */
    public String displayName() {
        return displayName;
    }
}