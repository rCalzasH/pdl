# PdL-JS: Lexical and Syntactic Analyzer

Front end of a compiler for **PdL-JS**, a JavaScript subset, written in Java for the *Procesadores de Lenguajes* course (UPM).

Given a source file, the project:

1. splits it into tokens (**lexical analysis**),
2. checks that the tokens follow the grammar and records which rules were used (**top-down syntactic analysis**),
3. reports any lexical or syntax error in separate files.

> **Status:** lexical and syntactic analyzers are done. The symbol table / semantic analysis is a separate, upcoming part and is not implemented yet.

---

## Project structure

```
src/
├── lex_a/
│   ├── lex_a.java            Lexical analyzer (scanner)
│   ├── lex_gest_error.java   Lexical error manager
│   └── Tokens.java           Token types (enum)
└── sin_a/
    ├── sin_a.java            Syntactic analyzer (recursive descent) + main
    └── sin_gest_error.java   Syntax error manager
```

| File | Responsibility |
|------|----------------|
| `Tokens.java` | Enum with every token type the lexer can emit. |
| `lex_a.java` | Reads the source char by char and writes the tokens file. Reports lexical errors through `lex_gest_error`. |
| `lex_gest_error.java` | Writes lexical errors (`line + message`) to the errors file and counts them. |
| `sin_a.java` | Entry point of the whole project. Runs the lexer, loads the tokens, runs the parser, writes the parse file. |
| `sin_gest_error.java` | Writes syntax errors (`token position + lexeme + expected`) to its own file and counts them. |

---

## How it works

```
 source file ──► lex_a ──► tokens.txt ──► sin_a (parser) ──► parse.txt
                   │                            │
                   ▼                            ▼
              errors.txt                   sin_errors.txt
         (lex_gest_error)               (sin_gest_error)
```

1. `sin_a.main` creates a `lex_a` and runs it over the source. Tokens go to `tokens.txt`.
2. If the lexer found any error, the errors are reported and **the parser is not run**.
3. Otherwise `sin_a` reads `tokens.txt`, converts each line into a grammar terminal and appends `$` (end of input).
4. The recursive descent parser consumes the terminals. Every time it applies a grammar rule, it appends the rule number to the parse string.
5. On success the parse is written to `parse.txt`. On the first syntax error, the error is written to `sin_errors.txt` and the analysis stops.

You only need to execute `sin_a`; the lexer is called internally.

---

## Build and run

Requires a JDK (Java 8 or newer). From the project root (the folder that contains `src/`):

```bash
javac src/lex_a/*.java src/sin_a/*.java
java src.sin_a.sin_a <source> [tokens_file] [parse_file] [lex_errors_file] [syntax_errors_file]
```

| Argument | Default |
|----------|---------|
| `<source>` | required |
| `tokens_file` | `tokens.txt` |
| `parse_file` | `parse.txt` |
| `lex_errors_file` | `errors.txt` |
| `syntax_errors_file` | `sin_errors.txt` |

The lexer can still be run alone: `java src.lex_a.lex_a <source> [tokens_file] [errors_file]`.

---

## Lexical analyzer

### Tokens

| Token | Attribute | Matches |
|-------|-----------|---------|
| `WORD` | keyword number 1-14 | `boolean break case echo float function if int let prompt return string switch void` (numbered in this order) |
| `ID` | the name | letters, digits and `_`, starting with a letter or `_` |
| `ENT` | the value | integer constant, max **32767** |
| `REAL` | the value | digits `.` digits |
| `CAD` | the text, in `"..."` | string with `'...'` or `"..."` (must close on the same line, `\` escapes the next char) |
| `ASIG` / `ASIGRES` | none | `=` / `-=` |
| `SUMA`, `NEG`, `MENOR` | none | `+`, `!`, `<` |
| `COMA`, `PUNTCOMA`, `DOSPUNT` | none | `,` `;` `:` |
| `PARIZ`, `PARDERCH`, `CORIZ`, `CORDERCH` | none | `(` `)` `{` `}` |

Whitespace, `// line comments` and `/* block comments */` are skipped.

### Output format

One token per line, `<TOKEN, attribute>`:

```
let int x;     →   <WORD, 9>
                   <WORD, 8>
                   <ID, x>
                   <PUNTCOMA, >
```

### Lexical errors

Written as `Lexical error (line N): message`. Detected cases:

- malformed real number (`3.` with no digits after the dot)
- integer larger than 32767
- unterminated string
- unterminated block comment
- `-` not followed by `=`, `/` not starting a comment
- any unexpected character

The lexer keeps going after an error, so one run reports all of them.

---

## Syntactic analyzer

A **top-down recursive descent** parser: one method per non-terminal (`A` ... `U`), choosing the rule by looking at the current token (LL(1)).

### Grammar

Axiom: `A`. `λ` is the empty production. Terminals are in lowercase and match the token names (keywords use their own name).

| # | Rule | # | Rule |
|---|------|---|------|
| 1 | `A → B A` | 27 | `J → void` |
| 2 | `A → H A` | 28 | `J → C id K` |
| 3 | `A → λ` | 29 | `K → , C id K` |
| 4 | `B → let C id ;` | 30 | `K → λ` |
| 5 | `B → if ( O ) E` | 31 | `L → B L` |
| 6 | `B → switch ( O ) { D }` | 32 | `L → λ` |
| 7 | `B → E` | 33 | `M → O N` |
| 8 | `C → int` | 34 | `M → λ` |
| 9 | `C → float` | 35 | `N → , O N` |
| 10 | `C → boolean` | 36 | `N → λ` |
| 11 | `C → string` | 37 | `O → P` |
| 12 | `D → case O : L D` | 38 | `P → R Q` |
| 13 | `D → λ` | 39 | `Q → < R Q` |
| 14 | `E → id F` | 40 | `Q → λ` |
| 15 | `E → echo O ;` | 41 | `R → T S` |
| 16 | `E → prompt ( id ) ;` | 42 | `S → + T S` |
| 17 | `E → return G ;` | 43 | `S → λ` |
| 18 | `E → break ;` | 44 | `T → ! T` |
| 19 | `F → = O ;` | 45 | `T → id U` |
| 20 | `F → -= O ;` | 46 | `T → ent` |
| 21 | `F → ( M ) ;` | 47 | `T → real` |
| 22 | `G → O` | 48 | `T → cad` |
| 23 | `G → λ` | 49 | `T → ( O )` |
| 24 | `H → function I id ( J ) { L }` | 50 | `U → ( M )` |
| 25 | `I → C` | 51 | `U → λ` |
| 26 | `I → void` | | |

### How tokens become terminals

`sin_a` reads each line of `tokens.txt`:

- `WORD` with attribute `n` becomes the n-th keyword (`<WORD, 9>` → `let`).
- Any other token becomes its lowercase name (`<PARIZ, >` → `pariz`, `<ID, x>` → `id`).
- `$` is added at the end.

### Output format

The parse is the list of rule numbers in the order they were applied, preceded by the parse type:

```
let int x;     →   descendente 1 4 8 3
```

### Syntax errors

Written as `Syntax error (token N, 'lexeme'): expected ...`, where `N` is the position of the token in the token list:

```
let int ;      →   Syntax error (token 3, 'puntcoma'): expected 'id'
```

The parser stops at the first syntax error. The tokens file has no line numbers, so syntax errors point to a token position, not a source line.

---

## Example

`test.js`:

```js
let int x;
function int f(int a, string b) { return a + 1; }
x = f(3, "hi");
if (x < 2) echo x;
switch (x) { case 1: break; }
```

```bash
java src.sin_a.sin_a test.js
```

If the program is valid, `parse.txt` contains the rule sequence and the console prints `Syntax OK`. Otherwise the offending error is printed and saved to the corresponding errors file.

---

## Design notes

- **Separation of concerns:** scanning, parsing and error reporting are separate classes. The analyzers never format error messages themselves.
- **File-based pipeline:** the lexer and the parser communicate through `tokens.txt`, which makes each stage easy to test on its own.
- **Symbol table:** it will be added as a separate part and is intentionally kept out of the lexer.

## Known limitations

- Only the first syntax error is reported (no error recovery).
- Syntax errors have a token position instead of a line number.
- Operators are limited to the group's token table: `+`, `!`, `<`, `=`, `-=`.
- No semantic analysis or symbol table yet.
