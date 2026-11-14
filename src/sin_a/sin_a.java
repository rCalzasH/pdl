package src.sin_a;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import src.lex_a.Tokens;
import src.lex_a.lex_a;

/*
 * Top-down (recursive descent) syntactic analyzer.
 * It runs the lexer by itself, so only this class has to be executed.
 */
public class sin_a
{
	/* keyword table: the attribute written by the lexer (1..14) is the index + 1 */
	private static final String[] KW = { "boolean", "break", "case", "echo", "float", "function", "if",
		"int", "let", "prompt", "return", "string", "switch", "void" };

	private final List<String> tokens;   // terminals as the grammar names them
	private final List<String> lexemes;  // text shown in error messages
	private final sin_gest_error errors;
	private int pos = 0;
	private final StringBuilder parse = new StringBuilder("descendente");

	public sin_a(List<String> tokens, List<String> lexemes, sin_gest_error errors)
	{
		this.tokens = tokens;
		this.lexemes = lexemes;
		this.errors = errors;
	}

	/* ---------- helpers ---------- */

	private String tokenActual()
	{
		return tokens.get(pos);
	}

	private void regla(int n)
	{
		parse.append(' ').append(n);
	}

	private sin_gest_error.SyntaxError error(String expected)
	{
		return errors.report(pos + 1, lexemes.get(pos), expected);
	}

	private void consume(String t)
	{
		if (!tokenActual().equals(t))
			throw error("'" + t + "'");
		pos++;
	}

	private boolean esta(String... set)
	{
		for (String s : set)
			if (tokenActual().equals(s))
				return true;
		return false;
	}

	private boolean expresion()
	{
		return esta("neg", "id", "ent", "real", "cad", "pariz");
	}

	private boolean bloque()
	{
		return esta("let", "if", "switch", "id", "echo", "prompt", "return", "break");
	}

	private boolean tipo()
	{
		return esta("int", "float", "boolean", "string");
	}

	/* ---------- entry point of the analysis ---------- */

	public String run()
	{
		A();
		if (!tokenActual().equals("$"))
			throw error("end of file");
		return parse.toString();
	}

	/* ---------- grammar ---------- */

	private void A()
	{
		if (tokenActual().equals("function")) { regla(2); H(); A(); }
		else if (bloque()) { regla(1); B(); A(); }
		else if (tokenActual().equals("$")) regla(3);
		else throw error("declaration, statement or function");
	}

	private void B()
	{
		if (tokenActual().equals("let"))
		{
			regla(4);
			consume("let"); C(); consume("id"); consume("puntcoma");
		}
		else if (tokenActual().equals("if"))
		{
			regla(5);
			consume("if"); consume("pariz"); O(); consume("parderch"); E();
		}
		else if (tokenActual().equals("switch"))
		{
			regla(6);
			consume("switch"); consume("pariz"); O(); consume("parderch");
			consume("coriz"); D(); consume("corderch");
		}
		else if (bloque()) { regla(7); E(); }
		else throw error("statement");
	}

	private void C()
	{
		switch (tokenActual())
		{
			case "int":     regla(8);  consume("int");     break;
			case "float":   regla(9);  consume("float");   break;
			case "boolean": regla(10); consume("boolean"); break;
			case "string":  regla(11); consume("string");  break;
			default: throw error("type");
		}
	}

	private void D()
	{
		if (tokenActual().equals("case"))
		{
			regla(12);
			consume("case"); O(); consume("dospunt"); L(); D();
		}
		else if (tokenActual().equals("corderch")) regla(13);
		else throw error("'case' or '}'");
	}

	private void E()
	{
		switch (tokenActual())
		{
			case "id":     regla(14); consume("id"); F(); break;
			case "echo":   regla(15); consume("echo"); O(); consume("puntcoma"); break;
			case "prompt": regla(16); consume("prompt"); consume("pariz"); consume("id");
				consume("parderch"); consume("puntcoma"); break;
			case "return": regla(17); consume("return"); G(); consume("puntcoma"); break;
			case "break":  regla(18); consume("break"); consume("puntcoma"); break;
			default: throw error("simple statement");
		}
	}

	private void F()
	{
		switch (tokenActual())
		{
			case "asig":    regla(19); consume("asig"); O(); consume("puntcoma"); break;
			case "asigres": regla(20); consume("asigres"); O(); consume("puntcoma"); break;
			case "pariz":   regla(21); consume("pariz"); M(); consume("parderch"); consume("puntcoma"); break;
			default: throw error("'=', '-=' or '('");
		}
	}

	private void G()
	{
		if (expresion()) { regla(22); O(); }
		else if (tokenActual().equals("puntcoma")) regla(23);
		else throw error("expression or ';'");
	}

	private void H()
	{
		regla(24);
		consume("function"); I(); consume("id"); consume("pariz"); J();
		consume("parderch"); consume("coriz"); L(); consume("corderch");
	}

	private void I()
	{
		if (tipo()) { regla(25); C(); }
		else if (tokenActual().equals("void")) { regla(26); consume("void"); }
		else throw error("return type or 'void'");
	}

	private void J()
	{
		if (tokenActual().equals("void")) { regla(27); consume("void"); }
		else if (tipo()) { regla(28); C(); consume("id"); K(); }
		else throw error("'void' or parameter");
	}

	private void K()
	{
		if (tokenActual().equals("coma")) { regla(29); consume("coma"); C(); consume("id"); K(); }
		else if (tokenActual().equals("parderch")) regla(30);
		else throw error("',' or ')'");
	}

	private void L()
	{
		if (bloque()) { regla(31); B(); L(); }
		else if (esta("corderch", "case")) regla(32);
		else throw error("statement, 'case' or '}'");
	}

	private void M()
	{
		if (expresion()) { regla(33); O(); N(); }
		else if (tokenActual().equals("parderch")) regla(34);
		else throw error("argument or ')'");
	}

	private void N()
	{
		if (tokenActual().equals("coma")) { regla(35); consume("coma"); O(); N(); }
		else if (tokenActual().equals("parderch")) regla(36);
		else throw error("',' or ')'");
	}

	private void O() { regla(37); P(); }

	private void P() { regla(38); R(); Q(); }

	private void Q()
	{
		if (tokenActual().equals("menor")) { regla(39); consume("menor"); R(); Q(); }
		else regla(40);
	}

	private void R() { regla(41); T(); S(); }

	private void S()
	{
		if (tokenActual().equals("suma")) { regla(42); consume("suma"); T(); S(); }
		else regla(43);
	}

	private void T()
	{
		switch (tokenActual())
		{
			case "neg":   regla(44); consume("neg"); T(); break;
			case "id":    regla(45); consume("id"); U(); break;
			case "ent":   regla(46); consume("ent"); break;
			case "real":  regla(47); consume("real"); break;
			case "cad":   regla(48); consume("cad"); break;
			case "pariz": regla(49); consume("pariz"); O(); consume("parderch"); break;
			default: throw error("operand");
		}
	}

	private void U()
	{
		if (tokenActual().equals("pariz")) { regla(50); consume("pariz"); M(); consume("parderch"); }
		else regla(51);
	}

	/* ---------- reading the tokens file written by the lexer ---------- */

	/*
	 * @brief: converts lines "<ID, x>", "<WORD, 7>", "<SUMA, >" into grammar terminals
	 *         (keywords become their own name: "let", "if"...). Adds "$" at the end.
	 */
	private static void load_tokens(String tokensPath, List<String> terms, List<String> lexs) throws IOException
	{
		try (BufferedReader br = new BufferedReader(new FileReader(tokensPath)))
		{
			String ln;
			while ((ln = br.readLine()) != null)
			{
				if (ln.isEmpty())
					continue;
				int comma = ln.indexOf(',');
				if (!ln.startsWith("<") || !ln.endsWith(">") || comma < 0)
					throw new IOException("Malformed line in tokens file: " + ln);
				Tokens t = Tokens.valueOf(ln.substring(1, comma));
				String attr = ln.substring(comma + 1, ln.length() - 1).trim();
				if (t == Tokens.WORD)
				{
					String kw = KW[Integer.parseInt(attr) - 1];
					terms.add(kw);
					lexs.add(kw);
				}
				else
				{
					terms.add(t.name().toLowerCase());
					lexs.add(attr.isEmpty() ? t.name().toLowerCase() : attr);
				}
			}
		}
		terms.add("$");
		lexs.add("end of file");
	}

	/* ---------- main: runs lexer + parser ---------- */

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("Usage: java src.sin_a.sin_a <source> [tokens_file] [parse_file] [lex_errors_file] [syntax_errors_file]");
			System.exit(1);
		}
		String source = args[0];
		String tokensFile = args.length > 1 ? args[1] : "tokens.txt";
		String parseFile = args.length > 2 ? args[2] : "parse.txt";
		String lexErrors = args.length > 3 ? args[3] : "errors.txt";
		String synErrors = args.length > 4 ? args[4] : "sin_errors.txt";

		try
		{
			/* 1) lexical analysis */
			lex_a lexer = new lex_a(source, tokensFile, lexErrors);
			lexer.analyze();
			int lexCount = lexer.get_error_count();
			lexer.close();
			if (lexCount > 0)
			{
				System.err.println(lexCount + " lexical error(s), see " + lexErrors + ". Syntax analysis skipped.");
				System.exit(1);
			}

			/* 2) syntactic analysis */
			List<String> terms = new ArrayList<>();
			List<String> lexs = new ArrayList<>();
			load_tokens(tokensFile, terms, lexs);

			sin_gest_error errors = new sin_gest_error(synErrors);
			try
			{
				String result = new sin_a(terms, lexs, errors).run();
				try (Writer w = new BufferedWriter(new FileWriter(parseFile)))
				{
					w.write(result + "\n");
				}
				System.out.println("Syntax OK. Parse written to " + parseFile);
			}
			catch (sin_gest_error.SyntaxError e)
			{
				System.err.println(e.getMessage() + " (see " + synErrors + ")");
				System.exit(1);
			}
			finally
			{
				errors.close();
			}
		}
		catch (IOException e)
		{
			System.err.println("I/O error: " + e.getMessage());
			System.exit(1);
		}
	}
}
