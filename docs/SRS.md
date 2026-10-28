# MiniJava Compiler – Software Requirements Specification (SRS)

**Author:** Deepanshu Chaurasia (Deepu) | **Version:** 1.2 (Draft) | **Date:** 28 October 2026 | **Scope of this document:** Version 1 (V1), with a preview of V2 and V3

> **How to read this document:** Each section starts with a short *What / Why* note in simple words. Technical terms are kept as they are, with a simple meaning next to them.

## 1. Introduction

### 1.1 Purpose

**What:** This document lists everything the MiniJava compiler must do and how well it must do it. **Why:** It is like a shopping list before cooking. If the list is clear, we do not forget things and we do not buy extra things.

### 1.2 Project Goal

Build a small compiler for a Java-like language called **MiniJava**. The compiler reads a `.mj` file, checks it, converts it into instructions, and runs those instructions on a custom virtual machine called **MiniVM**.

The main goal is **learning how compilers work inside**. The second goal is a strong **GitHub portfolio project**.

### 1.3 Scope

**In scope for V1:**

- Integer variables and arithmetic
- Print statements and comments
- Syntax errors and basic semantic errors
- Code generation for a stack-based VM
- Running the code on MiniVM
- Command line usage and unit tests

**Out of scope for V1** (see section 7 for the full list): `if`, `while`, functions, classes, strings, arrays, JVM bytecode.

### 1.4 Definitions (simple meanings)

| Term | Simple meaning |
| --- | --- |
| **Compiler** | A program that converts code written by humans into another form a machine can run |
| **Lexer (Lexical Analyzer)** | Reads text and cuts it into small pieces called tokens, like cutting a sentence into words |
| **Token** | One small meaningful piece, like `int`, `age`, `=`, or `21` |
| **Parser** | Checks that tokens are in a correct order (grammar), like checking a sentence is grammatically correct |
| **AST (Abstract Syntax Tree)** | A tree that shows the structure of the program |
| **Semantic Analysis** | Checks that the program makes sense (for example, a variable is declared before use) |
| **Symbol Table** | A notebook that remembers every variable name and its type |
| **Code Generator** | Converts the AST into simple VM instructions |
| **Virtual Machine (VM)** | A software "computer" that runs the instructions |
| **Operand Stack** | A pile of values, where we only add or remove from the top, like a stack of plates |
| **Opcode / Instruction** | One command for the VM, like `PUSH 5` or `ADD` |
| **EBNF** | A formal way to write grammar rules |
| **SRS** | Software Requirements Specification, this document |
| **SDLC** | Software Development Life Cycle |

## 2. Overall Description

### 2.1 Product Perspective

**What:** Where this product fits. MiniJava is a standalone command line tool. It does not depend on any other system.

The pipeline looks like this:

`Source File (.mj)` → `Lexer` → `Parser` → `AST` → `Semantic Analyzer` → `Code Generator` → `MiniVM` → `Output`

**Analogy:** A factory line. Raw material (source code) goes through many machines. Each machine does one job and passes the result to the next machine.

### 2.2 Users

| User | What they want |
| --- | --- |
| Developer (Deepu) | Understand each compiler stage by building it |
| Recruiter / interviewer | See clean code, tests, and documentation on GitHub |
| Learner / user | Run `.mj` programs and read generated instructions to learn |

### 2.3 Operating Environment

- Java 21 or newer
- Works on Windows, Linux, and macOS (anything that runs Java)
- Built with Maven

### 2.4 Assumptions and Dependencies

- Developer can spend about 25 to 30 hours per week (about 4 hours per day). V1 must be released by 28 November 2026.
- Only standard Java libraries and JUnit 5 are used.
- No ANTLR, ASM, or similar libraries that do the compiler work.
-

## 3. Functional Requirements (Version 1)

**What:** The list of things the compiler must DO. **Why:** Each item has an ID (like FR-1). Later, every test and every task can point to an ID, so we can prove nothing was missed.

**Priority:** M = Must have, S = Should have.

### 3.1 Lexer

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-1 | The lexer shall read source code character by character and produce tokens. | M |
| FR-2 | Each token shall store its type, lexeme (the actual text), line number, and column number. | M |
| FR-3 | The lexer shall recognize keywords: `int`, `print` (and reserve `if`, `else`, `while` for V2). | M |
| FR-4 | The lexer shall recognize identifiers (names like `age`, `total1`). | M |
| FR-5 | The lexer shall recognize integer literals (like `21`). | M |
| FR-6 | The lexer shall recognize operators: `+ - * /` and assignment `=`. | M |
| FR-7 | The lexer shall recognize `(` `)` `{` `}` and `;`. | M |
| FR-8 | The lexer shall skip whitespace and comments (`// ...`). | M |
| FR-9 | The lexer shall report an error with line and column for an invalid character (for example `@`). | M |
| FR-10 | The lexer shall add an end-of-file (EOF) token at the end. | M |
| FR-43 | The lexer shall reject an integer literal that is too large for a 32-bit int, or that touches letters (for example 12abc). | M |

### 3.2 Parser and AST

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-11 | The parser shall be a hand-written recursive-descent parser. | M |
| FR-12 | The parser shall build an AST from tokens. | M |
| FR-13 | The parser shall support integer literals, variables, arithmetic expressions, and parentheses. | M |
| FR-14 | The parser shall follow correct operator precedence: `*` and `/` before `+` and `-`. Example: `2 + 3 * 4` gives 14. | M |
| FR-15 | The parser shall support variable declarations (`int x = 5;`). | M |
| FR-16 | The parser shall support print statements (`print(x);`). | M |
| FR-17 | The parser shall support expression statements. | M |
| FR-18 | The parser shall support unary minus (`-x`). | S |
| FR-19 | The parser shall report useful syntax errors (for example, missing `;`, unmatched `)`) with line and column. | M |
| FR-20 | AST classes shall not depend on the lexer or the code generator. | M |
| FR-41 | The parser shall support assignment statements to a variable, for example x = x + 1; | M |

AST node examples for V1: `NumberExpr`, `VariableExpr`, `BinaryExpr`, `UnaryExpr`, `VariableDeclaration`, `PrintStatement`, `ExpressionStatement`, AssignStatement. (`BlockStatement`, `IfStatement`, `WhileStatement` come in V2.)

### 3.2.1 Initial Grammar (EBNF draft)

This is the first draft of the language rules. It will be moved to `docs/grammar.md` during the Design phase.

```text
program     = { statement } EOF ;
statement   = varDecl | assignStmt | printStmt | exprStmt ;
varDecl     = "int" IDENTIFIER "=" expression ";" ;
assignStmt  = IDENTIFIER "=" expression ";" ;
printStmt   = "print" "(" expression ")" ";" ;
exprStmt    = expression ";" ;
expression  = term { ( "+" | "-" ) term } ;
term        = unary { ( "*" | "/" ) unary } ;
unary       = "-" unary | primary ;
primary     = INTEGER | IDENTIFIER | "(" expression ")" ;
```

**Simple meaning:** `{ ... }` means "repeat zero or more times". `|` means "or". Because `term` (multiply and divide) sits deeper than `expression` (add and subtract), multiplication is handled first. This is how precedence works.

### 3.3 Semantic Analyzer

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-21 | The analyzer shall keep a symbol table of declared variables with their types. | M |
| FR-22 | The analyzer shall reject use of an undeclared variable. | M |
| FR-23 | The analyzer shall reject duplicate declaration in the same scope. | M |
| FR-24 | The analyzer shall type check expressions and assignments (only `int` in V1). | M |
| FR-25 | The analyzer shall support scope management (one global scope in V1, designed so nested scopes can be added in V2). | S |
| FR-26 | Invalid programs shall be rejected before any execution starts. | M |
| FR-42 | The analyzer shall reject assignment to an undeclared variable, and check that the assigned value has the type of the variable. | M |

**Note:** V1 has only the `int` type, so a pure type mismatch cannot happen yet. The type checker (`typeOf`) is still built now. The tests for "invalid type assignment" are added in V2, when `boolean` arrives.

### 3.4 Code Generator

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-27 | The code generator shall convert the validated AST into MiniVM instructions. | M |
| FR-28 | The V1 instruction set shall include `PUSH`, `LOAD`, `STORE`, `ADD`, `SUB`, `MUL`, `DIV`, `NEG`, `PRINT`, `POP`. | M |
| FR-29 | The generated instructions shall be readable (one per line, printable with a flag like `--show-code`). | M |

Example: `print(2 + 3);` becomes

```text
PUSH 2
PUSH 3
ADD
PRINT
```

**Decision D-6 (added 28 Oct 2026):** An expression statement such as `5 + 3;` calculates a value, but nobody uses it. Without cleanup, that value would stay on the stack forever. So the code generator adds a `POP` instruction after it. `POP` removes the top value and throws it away.

```text
PUSH 5
PUSH 3
ADD
POP
```

### 3.5 MiniVM

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-30 | MiniVM shall execute instructions in order. | M |
| FR-31 | MiniVM shall keep an operand stack and variable storage. | M |
| FR-32 | MiniVM shall run arithmetic operations and print results. | M |
| FR-33 | MiniVM shall report stack underflow as a runtime error. | M |
| FR-34 | MiniVM shall report division by zero as a runtime error. | M |
| FR-35 | MiniVM shall report unknown or invalid instructions. | M |
| FR-36 | MiniVM shall run the generated instructions, never the original source text. | M |
| FR-44 | Arithmetic shall use 32-bit signed integers like Java: division cuts toward zero, and overflow wraps around without an error. | M |

### 3.6 Integration and Command Line

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-37 | `Main` shall connect all stages: Source → Lexer → Parser → AST → Semantic Analyzer → Code Generator → MiniVM → Output. | M |
| FR-38 | The program shall accept a `.mj` file path from the command line, for example `java -jar minijava-compiler.jar examples/hello.mj`. | M |
| FR-39 | If the file is missing or not `.mj`, the program shall show a clear message. | M |
| FR-40 | If compilation fails, the program shall show the error type, message, line, and column, and exit with a non-zero code. | M |

## 4. Non-Functional Requirements

**What:** How WELL the system must work (quality rules). **Why:** A compiler that works but is messy cannot be improved later.

| ID | Category | Requirement |
| --- | --- | --- |
| NFR-1 | Readability | Use clear Java naming. Each class has one responsibility. |
| NFR-2 | Testability | Each stage can be tested alone with JUnit 5. |
| NFR-3 | Error quality | Every error message is meaningful and includes line and column when possible. |
| NFR-4 | Buildability | `mvn clean test` passes after every development stage. |
| NFR-5 | Simplicity | Avoid overengineering. Create a class only when it is needed. |
| NFR-6 | Documentation | README, grammar document, SRS, design notes, and example programs are kept up to date. |
| NFR-7 | Performance | Small programs (under 1000 lines) shall compile and run in under 1 second. |
| NFR-8 | Maintainability | New language features can be added without rewriting old stages. |
| NFR-9 | Error handling | Errors are never silently ignored. Use custom exceptions or a diagnostics class. |
| NFR-10 | Code coverage | Aim for good unit test coverage on lexer, parser, semantic analyzer, and VM. |

## 5. Constraints

- Language: Java 21
- Build tool: Maven
- Testing: JUnit 5
- Version control: Git and GitHub
- IDE: IntelliJ IDEA
- Execution target: custom stack-based VM (MiniVM)
- No library that implements a main compiler stage (no ANTLR, no ASM)
- Source file extension: `.mj`

**Why no ANTLR, ASM, or similar libraries?** The goal of this project is to learn how a compiler works inside. These tools do the hardest and most important parts for you, so you would learn the tool, not the compiler. Think of it like learning to cook: using a ready-made meal kit is faster, but you do not learn cooking.

| Library | What it does for you | What we write ourselves instead |
| --- | --- | --- |
| ANTLR | Reads a grammar file and generates the lexer and parser code | `Lexer.java` and `Parser.java`, written by hand |
| ASM | Creates JVM bytecode and `.class` files | `CodeGenerator.java` and our own instruction set run by `MiniVM` |

**What we use instead:** only standard Java (`String`, `char`, `List`, `Map`/`HashMap`, `Deque` for the stack, `record`, `sealed interface`, `switch` expressions) plus JUnit 5 for tests. Maven is only the build tool. It does not do any compiler work.

## 6. Sample Programs and Expected Output

These examples also act as **acceptance tests**.

### 6.1 hello.mj

```java
// hello.mj
int age = 21;
int marks = 85;

print(age);
print(marks);
print(10 + 20 * 3);
```

**Expected output:**

```text
21
85
70
```

### 6.2 arithmetic.mj

```java
print(2 + 3 * 4);
print((2 + 3) * 4);
print(10 - 4 - 3);
print(-5 + 8);
```

**Expected output:**

```text
14
20
3
3
```

### 6.3 Invalid programs (must be rejected)

| Program | Expected error type |
| --- | --- |
| `int x = 5` (no semicolon) | Syntax error: missing `;` |
| `print((2 + 3);` | Syntax error: unmatched `(` |
| `print(y);` | Semantic error: `y` is not declared |
| `int a = 1; int a = 2;` | Semantic error: duplicate declaration of `a` |
| `int x = 5 @ 3;` | Lexical error: invalid character `@` |
| `print(10 / 0);` | Runtime error: division by zero |

## 7. Out of Scope for V1

**Why list this:** It protects us from "scope creep" (adding more and more features and never finishing).

- `if`, `else`, `while` (planned for V2)
- Boolean type and comparison operators (V2)
- Functions, parameters, return values (V3)
- Classes, objects, inheritance (V4, optional)
- Strings, arrays, floating point numbers
- Compiler optimizations
- GUI or IDE plugin
- JVM bytecode generation (advanced, optional)

## 8. Acceptance Criteria for Version 1

V1 is complete only when **all** of these are true:

1. `hello.mj` and `arithmetic.mj` give the exact expected output.
2. All invalid programs in section 6.3 are rejected with correct and clear messages.
3. All JUnit tests pass with `mvn clean test`.
4. The JAR file runs from the command line.
5. README explains how to build, run, and write MiniJava programs.
6. Code is pushed to GitHub and tagged `v1.0.0`.

## 9. Preview of Later Versions (not detailed yet)

| Version | Main features |
| --- | --- |
| V2: Control Flow | `boolean`, comparison operators, `if`, `if-else`, `while`, nested blocks, variable scopes, jump instructions |
| V3: Functions | Function declarations, parameters, return values, local variables, call stack in MiniVM |
| V4: OOP (optional) | Classes, objects, fields, methods, basic inheritance |
| Advanced (optional) | Real JVM bytecode and `.class` files |

Each version will get its own SRS update before coding starts.

## 10. Risks

| ID | Risk | Chance | Impact | Plan |
| --- | --- | --- | --- | --- |
| R-1 | Parser feels too difficult | Medium | High | Start with only expressions. Test each small rule. Draw the AST on paper first. |
| R-2 | Losing motivation | Medium | High | Small weekly goals. Push to GitHub often. Share progress on LinkedIn. |
| R-3 | Scope creep | High | Medium | Follow the Out of Scope list. Add new ideas to a backlog only. |
| R-4 | College exams reduce time | High | Medium | Keep buffer weeks in the timeline. |
| R-5 | Hard-to-fix bugs | Medium | Medium | Write tests first. Commit small changes. Use Git to go back. |
| R-6 | Poor error messages | Medium | Low | Store line and column in tokens from day one. |

## 11. Estimated Timeline

**Assumption:** about 25 to 30 hours per week (about 4 hours per day), starting 28 October 2026. The work is the same as the earlier 10 to 12 week plan. Only the calendar is shorter, so no concept is skipped. To save time we shorten documents and polish, never the concepts or the tests.

| Sprint | Dates | Goal | Requirements | Done when |
| --- | --- | --- | --- | --- |
| 1 | 28 Oct to 3 Nov | Project setup, diagnostics base, `Token`, `Lexer` with tests | FR-1 to FR-10, FR-43 | `mvn test` is green, `--tokens` prints tokens |
| 2 | 4 to 10 Nov | AST classes, recursive-descent `Parser` with tests | FR-11 to FR-20, FR-41 | Source code becomes an AST, syntax errors are reported |
| 3 | 11 to 17 Nov | `SemanticAnalyzer`, instructions, `CodeGenerator` with tests | FR-21 to FR-29, FR-42 | A checked AST becomes a list of instructions |
| 4 | 18 to 24 Nov | `MiniVM`, full `Main` pipeline, end-to-end tests, README, tag `v1.0.0` | FR-30 to FR-40, FR-44 | `hello.mj` prints 21, 85, 70 |
| Buffer | 25 to 28 Nov | Bug fixes, polish docs, spare time for college work |  | **V1 released by 28 Nov 2026** |
| After V1 |  | V2 (control flow) and V3 (functions) |  | Estimated after V1 is released |

**What we shorten:** documents stay small (`README.md`, `design.md`, `grammar.md`; CHANGELOG and ADR notes only if time allows) and CI is optional. **What we never skip:** every compiler stage and the JUnit tests for each stage.

## 12. Engineering Practices (how we will work)

### 12.1 Definition of Done

A task is **done** only when:

- Code is written and follows naming rules
- Unit tests are written and pass
- `mvn clean test` passes
- Code is committed with a clear message
- Related document is updated if needed

### 12.2 Git Workflow

- `main` branch always builds and passes tests.
- One branch per feature, for example `feature/lexer`.
- Merge with a Pull Request, even when working alone.
- Commit style: `feat: add integer token`, `fix: handle empty file`, `test: add precedence tests`, `docs: update grammar`.
- Version tags: `v1.0.0`, `v2.0.0`.

### 12.3 Tools

- GitHub Projects: task board (To Do, In Progress, Done)
- GitHub Issues: one issue per task, linked to requirement IDs
- GitHub Actions: runs `mvn test` on every push

### 12.4 Documents in the repository

```text
minijava-compiler/
├── README.md
├── CHANGELOG.md
├── LICENSE
└── docs/
    ├── SRS.md
    ├── grammar.md
    ├── design.md
    └── adr/   (Architecture Decision Records)
```

## 13. Traceability (Requirement to Test)

**What:** A table that connects each requirement to the test that proves it. **Why:** Real companies do this so no requirement is forgotten. We will fill the Test column as we write tests.

| Requirement IDs | Area | Planned test class |
| --- | --- | --- |
| FR-1 to FR-10, FR-43 | Lexer | `LexerTest` |
| FR-11 to FR-20, FR-41 | Parser and AST | `ParserTest` |
| FR-21 to FR-26, FR-42 | Semantic Analyzer | `SemanticAnalyzerTest` |
| FR-27 to FR-29 | Code Generator | `CodeGeneratorTest` |
| FR-30 to FR-36, FR-44 | MiniVM | `MiniVMTest` |
| FR-37 to FR-40 | Integration | `EndToEndTest` |

## 14. Revision History

| Version | Date | Author | Change |
| --- | --- | --- | --- |
| 1.0 | 27 Oct 2026 | Deepu | First draft of V1 requirements |
| 1.1 | 28 Oct 2026 | Deepu | V1 deadline set to 1 month (28 Nov 2026); timeline rewritten; added reason for no ANTLR or ASM |
| 1.2 | 28 Oct 2026 | Deepu | Added POP (decision D-6), assignment statement (FR-41, FR-42), lexer number rule (FR-43), 32-bit integer rule (FR-44), and the 4-sprint plan |

## 15. Approval

Since this is a solo project, the developer acts as the reviewer. Before starting the Design phase, read the full document once and confirm:

- [x] Requirements are clear and complete for V1
- [x] Out of scope list is agreed
- [x] Timeline is realistic for my schedule
