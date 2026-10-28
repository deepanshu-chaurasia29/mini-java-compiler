# MiniJava Compiler – Design and Architecture (V1)

**Author:** Deepanshu Chaurasia (Deepu) | **Version:** 1.1 (Draft) | **Date:** 28 October 2026 | **Based on:** SRS v1.2

> **How to read this document:** The SRS said *what* to build. This document says *how* we will build it. Every section has a short *What / Why / How*. Nothing here is code you must memorize. It is the blueprint we follow while coding.

## 1. Design Goals

**What:** The rules that guide every design choice. **Why:** When two options look good, these goals help us choose one.

1. **Easy to learn from:** pick the simplest design that still teaches the real concept.
2. **One class, one job:** the lexer only makes tokens. The parser only builds the tree. And so on.
3. **Stages talk only through data:** each stage gets input and gives output, and does not know how other stages work.
4. **Ready for V2 and V3, but not built yet:** leave small "doors" (like a scope stack), but do not write V2 code now.
5. **Always testable:** every stage can be tested alone with JUnit 5.

## 2. Architecture Overview

**What:** The big picture of the compiler. **Analogy:** A factory line. Each machine does one job and passes the result forward. If one machine breaks, we know exactly which one.

`Source text` → **Lexer** → `List<Token>` → **Parser** → `AST` → **SemanticAnalyzer** → `checked AST` → **CodeGenerator** → `List<Instruction>` → **MiniVM** → `Output`

| Stage | Input | Output | Package | Main class |
| --- | --- | --- | --- | --- |
| Lexer | `String` (source code) | `List<Token>` | `lexer` | `Lexer` |
| Parser | `List<Token>` | AST (`Program`) | `parser` | `Parser` |
| Semantic Analyzer | AST | Same AST, or an error | `semantic` | `SemanticAnalyzer` |
| Code Generator | AST | `List<Instruction>` | `codegen` | `CodeGenerator` |
| MiniVM | `List<Instruction>` | Printed output | `vm` | `MiniVM` |
| Errors |  |  | `diagnostics` | `CompileException` and children |

**Rule of dependency (very important):** arrows only point forward.

- `parser` uses `lexer` (it needs `Token`).
- `semantic` and `codegen` use `parser.ast`.
- `vm` uses only `Instruction`. It never sees the AST or the source code (this is requirement FR-36).
- `parser.ast` uses nothing. It stays independent (requirement FR-20).

## 3. Package and File Structure

```text
minijava-compiler/
├── pom.xml
├── README.md
├── docs/ (SRS, design.md, grammar.md)
├── examples/ (hello.mj, arithmetic.mj, conditions.mj)
└── src/
    ├── main/java/com/deepu/minijava/
    │   ├── Main.java
    │   ├── lexer/        Token, TokenType, Lexer
    │   ├── parser/       Parser
    │   │   └── ast/      Program, Expr, Stmt, operators and their records
    │   ├── semantic/     Type, Symbol, SymbolTable, SemanticAnalyzer
    │   ├── codegen/      Instruction, CodeGenerator
    │   ├── vm/           MiniVM
    │   └── diagnostics/  CompileException + LexerException, ParseException,
    │                     SemanticException, VmException
    └── test/java/com/deepu/minijava/
        ├── lexer/ parser/ semantic/ codegen/ vm/ (one test class per stage)
        └── EndToEndTest.java
```

**Why this layout:** one package per compiler stage. When you open the project, the folders tell the story of the pipeline.

## 4. Token Design (Lexer)

**What:** A token is one small meaningful piece of the source code. **Why:** The parser should not worry about spaces, comments or single characters. It wants clean pieces like "keyword `int`", "name `age`", "number `21`".

### 4.1 TokenType (enum)

| Group | Token types | Example text |
| --- | --- | --- |
| Keywords | `INT`, `PRINT` (and reserved for V2: `IF`, `ELSE`, `WHILE`) | `int`, `print` |
| Literals and names | `INTEGER`, `IDENTIFIER` | `21`, `age` |
| Arithmetic | `PLUS`, `MINUS`, `STAR`, `SLASH` | `+ - * /` |
| Assignment | `EQUAL` | `=` |
| Symbols | `LEFT_PAREN`, `RIGHT_PAREN`, `LEFT_BRACE`, `RIGHT_BRACE`, `SEMICOLON` | `( ) { } ;` |
| Special | `EOF` | end of file |

Comparison operators (`<`, `==`, and so on) are added in V2. We do not create them now (SRS: avoid features outside the current milestone).

### 4.2 Token (a Java `record`)

```java
public record Token(TokenType type, String lexeme, int line, int column) { }
```

**Simple meaning:** a `record` is a short way to make a class that only holds data. `lexeme` is the exact text from the file. `line` and `column` are saved from the very start so that every later error can say *where* the problem is.

Example: for `int age = 21;` the lexer gives

```text
INT("int") 1:1   IDENTIFIER("age") 1:5   EQUAL("=") 1:9   INTEGER("21") 1:11   SEMICOLON(";") 1:13   EOF 1:14
```

## 5. Lexer Design

**How it works:** it keeps a position (`current`) in the source string and loops: look at one character, decide what token it starts, build the token, move forward.

| Part | Design |
| --- | --- |
| Fields | `source`, `current` (index), `line`, `column`, `tokens` list |
| Main method | `List<Token> scanTokens()` |
| Helper methods | `peek()` (look without moving), `advance()` (move forward), `isAtEnd()` |
| Whitespace | skip it, and count new lines |
| Comments | when we see `//`, skip until the end of the line |
| Numbers | keep reading while the next character is a digit |
| Words | keep reading letters, digits and `_`. Then check a keyword map (`"int"` → `INT`). If not found, it is an `IDENTIFIER` |
| Bad character | throw `LexerException("Unexpected character '@'", line, column)` |

**Lexer rules fixed now (so they never change later):** names use only English letters and `_`; keywords are case sensitive; only `//` comments exist in V1; a tab counts as one column and `\r` is whitespace; numbers are decimal, must fit in a 32-bit `int`, and must not touch letters (`12abc` is an error); the lexer stops at the first error.

**Trade-off (why hand-written and not regex):** regular expressions are shorter but hide how a lexer works. Reading character by character teaches the real idea, which is a small state machine.

## 6. AST Design

**What:** The AST is a tree that shows the structure of the program. **Analogy:** Think of a sentence diagram from school, where the sentence is broken into subject, verb and object. The AST does the same for code.

Example: `int x = 2 + 3 * 4;` becomes

```text
VariableDeclaration(name = x)
  └─ BinaryExpr(+)
       ├─ NumberExpr(2)
       └─ BinaryExpr(*)
            ├─ NumberExpr(3)
            └─ NumberExpr(4)
```

See how `3 * 4` sits deeper in the tree. Deeper nodes are calculated first. This is how precedence is stored.

### 6.1 Node types (V1)

```java
public enum BinaryOperator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");
    private final String symbol;
    BinaryOperator(String symbol) { this.symbol = symbol; }
    public String symbol() { return symbol; }
}
public enum UnaryOperator { NEGATE }          // NOT is added in V2

public sealed interface Expr permits NumberExpr, VariableExpr, BinaryExpr, UnaryExpr { }
public sealed interface Stmt permits VariableDeclaration, AssignStatement, PrintStatement, ExpressionStatement { }

public record NumberExpr(int value, int line, int column) implements Expr { }
public record VariableExpr(String name, int line, int column) implements Expr { }
public record BinaryExpr(Expr left, BinaryOperator operator, Expr right, int line, int column) implements Expr { }
public record UnaryExpr(UnaryOperator operator, Expr operand, int line, int column) implements Expr { }

public record VariableDeclaration(String name, Expr initializer, int line, int column) implements Stmt { }
public record AssignStatement(String name, Expr value, int line, int column) implements Stmt { }
public record PrintStatement(Expr expression, int line, int column) implements Stmt { }
public record ExpressionStatement(Expr expression, int line, int column) implements Stmt { }

public record Program(List<Stmt> statements) { }
```

**Fixed rule 1: operators are enums, not Strings.** A typo like `"+ "` cannot be caught by the compiler, but a wrong enum name can. A `switch` on an enum is also checked for missing cases.

**Fixed rule 2: what `line` and `column` mean.** They point to the most helpful token for error messages: the *name* for declarations and assignments, the *operator* for binary and unary expressions, the `print` keyword for print statements, and the first token for everything else.

**Why `line` and `column` inside nodes:** the semantic analyzer finds errors later (like "undeclared variable"). It needs to say where. The AST does not import `Token`, so it stays independent of the lexer.

### 6.2 How do later stages walk the tree? (trade-off)

| Option | Simple meaning | Verdict |
| --- | --- | --- |
| Visitor pattern | Every node has an `accept(visitor)` method. Classic textbook way, but needs a lot of extra code | Too heavy for 1 month |
| `sealed interface` + pattern matching `switch` (Java 21) | One `switch` checks which node it is. The compiler warns if you forget a case | **Chosen** |

**Why:** Java 21 gives us this feature, it is short and readable, and when we add `IfStatement` in V2 the compiler tells us every place we forgot to handle it.

## 7. Parser Design

**What:** The parser takes tokens and builds the AST using the grammar from SRS section 3.2.1. **Recursive descent in one idea:** *one method for each grammar rule*. Rules call each other, so the methods call each other.

| Grammar rule | Parser method |
| --- | --- |
| `program` | `Program parse()` |
| `statement` | `Stmt statement()` |
| `varDecl` | `Stmt varDeclaration()` |
| `printStmt` | `Stmt printStatement()` |
| `exprStmt` | `Stmt expressionStatement()` |
| `expression` (`+`, `-`) | `Expr expression()` |
| `term` (`*`, `/`) | `Expr term()` |
| `unary` (`-x`) | `Expr unary()` |
| `primary` (number, name, brackets) | `Expr primary()` |
| assignStmt | Stmt assignmentStatement() |

**Helper methods:** `peek()`, `advance()`, `check(type)`, `match(type...)` (move forward if the type matches), `consume(type, message)` (must match, or throw an error).

**Assignment or expression statement?** A statement that starts with a name can be `x = 5;` (assignment) or `x + 1;` (expression). The parser looks one more token ahead: if the first token is an `IDENTIFIER` and the next token is `EQUAL`, it is an assignment. For this we add one more helper, `peekNext()`.

**Why precedence works:** `expression()` calls `term()`, and `term()` calls `unary()`. So `*` is always built deeper in the tree than `+`.

**Left-to-right rule:** `10 - 4 - 3` must be `(10 - 4) - 3 = 3`. We use a `while` loop in `expression()`, so the left side keeps growing.

**Error handling (V1):** stop at the first error. Throw `ParseException("Expected ';' after expression", line, column)`. Trade-off: reporting many errors at once (called *error recovery*) is nicer, but it is hard and not needed to learn the main concept. We can add it later.

**Where the error points (decision D-11):** for "Expected ..." errors, the position is just *after the previous token* (its line and `endColumn()`). Example: if `;` is missing at the end of line 1, the error says line 1, not line 2. Other errors, like "Expected an expression", point at the current token. The words `if`, `else` and `while` give "Expected a statement" in V1, because they are reserved for V2.

## 8. Semantic Analyzer Design

**What:** The parser checks *grammar*. The semantic analyzer checks *meaning*. For example, `print(y);` has perfect grammar, but `y` was never declared.

### 8.1 Pieces

| Class | Job |
| --- | --- |
| `Type` (enum) | `INT` for now. `BOOLEAN` is added in V2 |
| `Symbol` (record) | `name`, `type`, `line` where it was declared |
| `SymbolTable` | A notebook of declared variables |
| `SemanticAnalyzer` | Walks the AST and uses the symbol table |

### 8.2 Symbol table with a scope stack

```java
private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
```

**Simple meaning:** a stack of notebooks. V1 uses only one notebook (the global scope). In V2, every `{ }` block pushes a new notebook, and leaving the block removes it. That is our "door" for V2, with no extra work today.

| Method | What it does |
| --- | --- |
| `declare(name, type)` | Adds the variable to the top notebook. If it is already there, throw a duplicate error |
| `lookup(name)` | Searches from the top notebook downward. If not found, throw an undeclared error |
| `enterScope()` / `exitScope()` | Push and pop a notebook (used from V2) |

### 8.3 Order of checks for `int x = <expr>;`

1. Check the expression first (`x` must not be visible inside its own initializer).
2. Find the type of the expression.
3. Check it matches the declared type.
4. Then declare `x`.

`typeOf(Expr)` returns a `Type`. For a number it is `INT`. For a variable it is the type from the table. For a binary expression both sides must be `INT`.

## 9. Instruction Set and Code Generator Design

**What:** The code generator turns the tree into a flat list of simple instructions. **Why a stack machine:** it is the simplest target. Expressions become a straight list, with no register juggling. The real JVM also works this way.

### 9.1 Instruction format

```java
public sealed interface Instruction {
    record Push(int value)   implements Instruction { public String toString() { return "PUSH " + value; } }
    record Load(String name) implements Instruction { public String toString() { return "LOAD " + name; } }
    record Store(String name) implements Instruction { public String toString() { return "STORE " + name; } }
    record Add()   implements Instruction { public String toString() { return "ADD"; } }
    record Sub()   implements Instruction { public String toString() { return "SUB"; } }
    record Mul()   implements Instruction { public String toString() { return "MUL"; } }
    record Div()   implements Instruction { public String toString() { return "DIV"; } }
    record Neg()   implements Instruction { public String toString() { return "NEG"; } }
    record Print() implements Instruction { public String toString() { return "PRINT"; } }
    record Pop()   implements Instruction { public String toString() { return "POP"; } }
}
```

**Why one record per instruction (decision D-7):** each instruction holds exactly the data it needs: `Push` holds an `int`, `Load` holds a name, and `Add` holds nothing. The VM never has to turn a String into a number. Tests can compare instructions with `equals`, for example `assertEquals(List.of(new Push(2), new Push(3), new Add()), code)`. In V2 we add `Jump(int target)` without touching any other instruction. `toString()` prints readable assembly like `PUSH 2`, which is what `--show-code` shows.

### 9.2 V1 instruction set

| Instruction | Operand | What it does | Stack effect |
| --- | --- | --- | --- |
| `PUSH n` | number | Put number `n` on top | `... → ..., n` |
| `LOAD x` | variable name | Read variable `x` and push its value | `... → ..., value` |
| `STORE x` | variable name | Pop the top value and save it in `x` | `..., v → ...` |
| `ADD` | none | Pop 2 values, push their sum | `..., a, b → ..., a+b` |
| `SUB` | none | Pop 2, push `a - b` | `..., a, b → ..., a-b` |
| `MUL` | none | Pop 2, push `a * b` | `..., a, b → ..., a*b` |
| `DIV` | none | Pop 2, push `a / b` (error if `b` is 0) | `..., a, b → ..., a/b` |
| `NEG` | none | Pop 1, push `-a` | `..., a → ..., -a` |
| `PRINT` | none | Pop 1 and print it | `..., v → ...` |
| POP | none | Pop 1 value and throw it away (used after an expression statement) | ..., v → ... |

**Order matters:** for `a - b`, `a` is pushed first and `b` second. The VM pops `b` first, then `a`.

### 9.3 How the generator walks the tree

It visits children first, then the operator. This is called **post-order traversal**.

| Node | Instructions made |
| --- | --- |
| `NumberExpr(5)` | `PUSH 5` |
| `VariableExpr(x)` | `LOAD x` |
| `BinaryExpr(l, +, r)` | code for `l`, code for `r`, then `ADD` |
| `UnaryExpr(-, e)` | code for `e`, then `NEG` |
| `VariableDeclaration(x, e)` | code for `e`, then `STORE x` |
| `PrintStatement(e)` | code for `e`, then `PRINT` |
| `ExpressionStatement(e)` | code for `e`, then a pop is needed, so we add a tiny `POP` instruction (see decision D-6) |
| AssignStatement(x, e) | code for e, then STORE x |

## 10. MiniVM Design

**What:** A small software computer that runs the instruction list one by one.

| Part | Design |
| --- | --- |
| `Deque<Integer> stack` | The operand stack. Only the top is used |
| `Map<String, Integer> variables` | Variable storage by name |
| `List<Instruction> program` | The instructions to run |
| `int pc` | *Program counter*: the number of the instruction to run next. Starts at 0 and goes up by 1 each step. In V2 jumps will change it |
| `void run()` | Loop: `while (pc < program.size())` get the instruction, `switch` on its opcode, then do the work |

**Runtime errors (`VmException`):**

| Problem | When | Message example |
| --- | --- | --- |
| Stack underflow | Pop from an empty stack | `Stack underflow at instruction 5 (ADD)` |
| Division by zero | `DIV` with `b == 0` | `Division by zero at instruction 4` |
| Unknown variable | `LOAD` of a name that was never stored | `Unknown variable 'y'` |
| Invalid instruction | Missing operand or unknown opcode | `Invalid instruction at 3` |

**Design point:** MiniVM gets the program and an output stream (`PrintStream`). So tests can capture the output and check it easily.

**Fixed rules for MiniVM:** `PRINT` prints the value followed by a new line. Integers are 32-bit like Java: `DIV` cuts toward zero, and overflow wraps around with no error. Runtime errors show the instruction number, not the source line (decision D-13). The VM stops when the program counter reaches the end of the instruction list.

## 11. Error Handling Design (diagnostics package)

```text
CompileException (base: message, line, column)
 ├── LexerException       (bad character)
 ├── ParseException       (bad grammar)
 ├── SemanticException    (bad meaning)
 VmException (runtime, separate because it happens while running)
```

**Error message format:**

```text
[Semantic Error] line 3, column 7: Variable 'y' is not declared
```

`Main` catches these exceptions, prints the message, and exits with code 1. We never ignore an error silently (requirement NFR-9).

## 12. Main and Command Line Design

**Flow inside `Main`:**

1. Check arguments and that the file ends with `.mj` (FR-39).
2. Read the file into a `String`.
3. `Lexer` → `Parser` → `SemanticAnalyzer` → `CodeGenerator` → `MiniVM`.
4. Catch `CompileException` and `VmException`, print and exit with code 1.

**Options:** `--show-code` prints the generated instructions before running (FR-29). Optional extras if time allows: `--tokens` and `--ast`.

**Exit codes (fixed now):** `0` means success, `1` means an error in the MiniJava program (compile or runtime), `2` means wrong command line or unreadable file. The `--tokens` option prints the tokens and stops. It stays in the project for good.

## 13. Full Example: One Line Through the Whole Pipeline

Program: `print(2 + 3 * 4);`

**Step 1: Lexer**

```text
PRINT  LEFT_PAREN  INTEGER(2)  PLUS  INTEGER(3)  STAR  INTEGER(4)  RIGHT_PAREN  SEMICOLON  EOF
```

**Step 2: Parser (AST)**

```text
PrintStatement
  └─ BinaryExpr(+)
       ├─ NumberExpr(2)
       └─ BinaryExpr(*)
            ├─ NumberExpr(3)
            └─ NumberExpr(4)
```

**Step 3: Semantic Analyzer:** no variables, all numbers are `INT`. It passes.

**Step 4: Code Generator**

```text
PUSH 2
PUSH 3
PUSH 4
MUL
ADD
PRINT
```

**Step 5: MiniVM (stack after each instruction)**

| # | Instruction | Stack (top on right) | Output |
| --- | --- | --- | --- |
| 0 | `PUSH 2` | 2 |  |
| 1 | `PUSH 3` | 2, 3 |  |
| 2 | `PUSH 4` | 2, 3, 4 |  |
| 3 | `MUL` | 2, 12 |  |
| 4 | `ADD` | 14 |  |
| 5 | `PRINT` | (empty) | `14` |

This is the whole compiler in one example.

## 14. Design Decisions (ADR-lite)

**What:** A short log of important choices, with the reason. Real teams call these *Architecture Decision Records*.

| ID | Decision | Why | • Other option we rejected |
| --- | --- | --- | --- |
| • D-1 | • Hand-written lexer, character by character | • Teaches how a lexer works | • Regex, or a library like ANTLR |
| • D-2 | • Recursive-descent parser, one method per rule | • Easy to read and debug | • Parser generators, or table-driven parsers |
| • D-3 | • AST as `sealed interface` + `record` + `switch`, and operators as enums | • Short code, and the compiler warns on missing cases | • Visitor pattern (too much code), operators as Strings |
| • D-4 | • Stop at the first error | • Keeps the parser simple | • Error recovery (added later if time allows) |
| • D-5 | • Variables stored by name in the VM (`Map<String,Integer>`, one map per call in V3). MiniJava follows Java: a variable cannot reuse the name of a visible outer variable | • Name storage is then always correct for V2 blocks and V3 calls | • Slot numbers (faster, but the code generator must repeat scope work) |
| • D-6 | • Add a `POP` opcode for expression statements | • Without it, `5 + 3;` would leave junk on the stack | • Ignore the leftover value (messy) |
| • D-7 | • `sealed interface Instruction` with one `record` per instruction | • Each instruction holds the right type of data, tests can use `equals`, V2 adds `Jump(int)` easily | • `OpCode` enum with a String operand (needs parsing, weak types) |
| • D-8 | • Scope stack in `SymbolTable` from day one | • V2 blocks need it, and it costs almost nothing now | • Single map, rewritten in V2 |
| • D-9 | • VM writes to a `PrintStream` | • Tests can capture output | • Use `System.out` directly |
| • D-10 | • Assignment statement x = expr; is part of V1 | • V2 while loops need it, and without it variables never change | • Add it in V2 (grammar, parser and AST would change) |
| • D-11 | • Expected ... syntax errors point just after the previous token | • A missing ; is reported on the line where it is missing | • Point at the next token (shows the wrong line) |
| • D-12 | • Integers are 32-bit like Java: division cuts toward zero, overflow wraps, the lexer checks literal size | • Matches Java, needs no special code | • BigInteger, or overflow errors |
| • D-13 | • Runtime errors show the instruction number, not the source line | • Keeps Instruction simple, and the SRS does not need more | • A line table now (extra code, not required) |
| • D-14 | • Main exit codes: 0 success, 1 program error, 2 usage error; --tokens stays forever | • Scripts and tests can check the result | • Always exit with 1 |
| • D-15 | • CompileException extends RuntimeException (unchecked) | • No throws in every method; Main has one catch | • Checked exceptions (too much noise) |

**Note on D-6:** `POP` is a small addition to the SRS instruction list (SRS FR-28). It is now added to the SRS (version 1.2).

## 15. Build Order and Test Plan (starts today)

**Why this order:** each step only needs the steps before it, and the project builds after every step.

| Sprint | What to build | Tests written with it |
| --- | --- | --- |
| 1 (28 Oct to 3 Nov) | Maven project, JUnit 5, packages, examples, `CompileException`, `LexerException`, `TokenType`, `Token`, `Lexer`, `Main` with `--tokens` | `TokenTest`, `LexerTest` (19 tests) |
| 2 (4 to 10 Nov) | AST records and operator enums, `ParseException`, `Parser` | `ParserTest`: precedence, left-to-right, missing `;`, unmatched `)`, assignment |
| 3 (11 to 17 Nov) | `Type`, `Symbol`, `SymbolTable`, `SemanticException`, `SemanticAnalyzer`, `Instruction`, `CodeGenerator` | `SemanticAnalyzerTest`, `CodeGeneratorTest` |
| 4 (18 to 24 Nov) | `MiniVM`, `VmException`, full `Main` pipeline, README | `MiniVMTest`, `EndToEndTest` |

## 16. Design Review Checklist

Before we start coding, read the whole document once and confirm:

- [x] The pipeline and package structure make sense to me
- [x] I understand why tokens, AST and instructions are separate things
- [x] I understand the dry run in section 13 (every step)
- [x] I agree with the design decisions D-1 to D-15
- [x] I am ready for step 1 and 2 of the build order
