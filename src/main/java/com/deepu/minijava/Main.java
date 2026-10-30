package com.deepu.minijava;

import com.deepu.minijava.diagnostics.CompileException;
import com.deepu.minijava.lexer.Lexer;
import com.deepu.minijava.lexer.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Entry point: Source File -> Lexer -> (more stages are added in later sprints) */
public class Main {

    private static final int EXIT_OK = 0;
    private static final int EXIT_ERROR = 1;   // compile error or runtime error in the .mj program
    private static final int EXIT_USAGE = 2;   // wrong command line or unreadable file

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    /** Does all the work and returns the exit code. Kept separate from main() so tests can call it. */
    static int run(String[] args, PrintStream out, PrintStream err) {
        boolean showTokens = false;
        String fileName = null;

        for (String arg : args) {
            if (arg.equals("--tokens")) {
                showTokens = true;
            } else if (arg.startsWith("--")) {
                err.println("Unknown option: " + arg);
                return usage(err);
            } else if (fileName == null) {
                fileName = arg;
            } else {
                err.println("Only one source file is allowed.");
                return usage(err);
            }
        }

        if (fileName == null) {
            return usage(err);
        }
        if (!fileName.endsWith(".mj")) {
            err.println("The source file must have the .mj extension: " + fileName);
            return EXIT_USAGE;
        }

        String source;
        try {
            source = readSource(Path.of(fileName));
        } catch (IOException e) {
            err.println("Cannot read file '" + fileName + "': " + e.getMessage());
            return EXIT_USAGE;
        }

        try {
            List<Token> tokens = new Lexer(source).scanTokens();

            if (showTokens) {                       // this option stays for the whole project
                for (Token token : tokens) {
                    out.println(token);
                }
                return EXIT_OK;
            }

            // SPRINT 2: the Parser is added here.
            // SPRINT 3: the SemanticAnalyzer and CodeGenerator are added here.
            // SPRINT 4: MiniVM is added here (and its VmException is caught below).
            out.println("Lexing finished: " + tokens.size() + " tokens. (The parser is not built yet.)");
            return EXIT_OK;
        } catch (CompileException e) {
            err.println(e.format());
            return EXIT_ERROR;
        }
    }

    private static String readSource(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) {            // remove the invisible BOM some Windows editors add
            text = text.substring(1);
        }
        return text;
    }

    private static int usage(PrintStream err) {
        err.println("Usage: java -jar minijava-compiler.jar [--tokens] <file.mj>");
        return EXIT_USAGE;
    }
}