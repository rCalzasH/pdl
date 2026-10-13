package src.lex_a;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class lex_a
{
	private static final int MAX_INT = 32767;
	private static final Map<String, Integer> KEYWORDS = new HashMap<>();
	static
	{
		String[] kw = { "boolean", "break", "case", "echo", "float", "function", "if",
			"int", "let", "prompt", "return", "string", "switch", "void" };
		for (int i = 0; i < kw.length; i++)
			KEYWORDS.put(kw[i], i + 1); // attribute: 1..14
	}

	private final PushbackReader reader;
	private final Writer tokenWriter;
	private final lex_gest_error errors;
	private int line = 1;

	/*
	 * @brief: opens the source (read), the tokens file (write) and the error manager
	 * @throws IOException if any file can't be opened
	 */
	public lex_a(String sourcePath, String tokensPath, String errorsPath) throws IOException
	{
		File src = new File(sourcePath);
		if (!src.canRead())
			throw new FileNotFoundException("Cannot read source file: " + sourcePath);
		this.reader = new PushbackReader(new BufferedReader(new FileReader(src)), 1);
		this.tokenWriter = new BufferedWriter(new FileWriter(tokensPath));
		this.errors = new lex_gest_error(errorsPath);
	}

	/* reads one char and counts lines */
	private int read_char() throws IOException
	{
		int c = reader.read();
		if (c == '\n')
			line++;
		return c;
	}

	/* looks at the next char without consuming it */
	private int peek() throws IOException
	{
		int c = reader.read();
		if (c != -1)
			reader.unread(c);
		return c;
	}

	/* consumes the next char only if it equals expected */
	private boolean match(char expected) throws IOException
	{
		if (peek() == expected)
		{
			reader.read(); // expected is never '\n' here
			return true;
		}
		return false;
	}

	private void emit(Tokens t, String attr) throws IOException
	{
		tokenWriter.write("<" + t + ", " + (attr == null ? "" : attr) + ">\n");
	}

	private void emit(Tokens t) throws IOException
	{
		emit(t, null);
	}

	/*
	 * @brief: main loop; decides which token to generate from the first char
	 */
	public void analyze() throws IOException
	{
		int c;
		while ((c = read_char()) != -1)
		{
			if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
				continue;
			if (Character.isLetter(c) || c == '_')
				lex_word(c);
			else if (Character.isDigit(c))
				lex_number(c);
			else if (c == '"' || c == '\'')
				lex_string(c);
			else
				lex_symbol(c);
		}
		tokenWriter.flush();
	}

	/* keywords and identifiers */
	private void lex_word(int first) throws IOException
	{
		StringBuilder sb = new StringBuilder().append((char) first);
		int p;
		while ((p = peek()) != -1 && (Character.isLetterOrDigit(p) || p == '_'))
			sb.append((char) read_char());
		String word = sb.toString();
		Integer kw = KEYWORDS.get(word);
		if (kw != null)
			emit(Tokens.WORD, kw.toString());
		else
			emit(Tokens.ID, word);
	}

	/* integer or real constants */
	private void lex_number(int first) throws IOException
	{
		StringBuilder sb = new StringBuilder().append((char) first);
		while (Character.isDigit(peek()))
			sb.append((char) read_char());
		if (peek() == '.')
		{
			sb.append((char) read_char());
			if (!Character.isDigit(peek()))
			{
				errors.report(line, "malformed real number '" + sb + "': digits expected after '.'");
				return;
			}
			while (Character.isDigit(peek()))
				sb.append((char) read_char());
			emit(Tokens.REAL, sb.toString());
			return;
		}
		try
		{
			if (Integer.parseInt(sb.toString()) > MAX_INT)
				throw new NumberFormatException();
			emit(Tokens.ENT, sb.toString());
		}
		catch (NumberFormatException e)
		{
			errors.report(line, "integer constant " + sb + " is too large (max " + MAX_INT + ")");
		}
	}

	/* strings with ' ' or " " */
	private void lex_string(int quote) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int c;
		while ((c = read_char()) != quote)
		{
			if (c == -1 || c == '\n')
			{
				errors.report(line, "unterminated string, missing closing " + (char) quote);
				return;
			}
			if (c == '\\')
			{
				sb.append('\\');
				c = read_char();
				if (c == -1)
				{
					errors.report(line, "unterminated string, missing closing " + (char) quote);
					return;
				}
			}
			sb.append((char) c);
		}
		emit(Tokens.CAD, "\"" + sb + "\"");
	}

	/* operators, punctuation and comments */
	private void lex_symbol(int c) throws IOException
	{
		switch (c)
		{
			case '+': emit(Tokens.SUMA); break;
			case '!': emit(Tokens.NEG); break;
			case '<': emit(Tokens.MENOR); break;
			case '=': emit(Tokens.ASIG); break;
			case '-':
				if (match('='))
					emit(Tokens.ASIGRES);
				else
					errors.report(line, "unexpected '-': only '-=' is supported");
				break;
			case '/':
				if (match('/'))
					skip_line_comment();
				else if (match('*'))
					skip_block_comment();
				else
					errors.report(line, "unexpected '/': only comments '//' and '/* */' are supported");
				break;
			case '(': emit(Tokens.PARIZ); break;
			case ')': emit(Tokens.PARDERCH); break;
			case '{': emit(Tokens.CORIZ); break;
			case '}': emit(Tokens.CORDERCH); break;
			case ',': emit(Tokens.COMA); break;
			case ';': emit(Tokens.PUNTCOMA); break;
			case ':': emit(Tokens.DOSPUNT); break;
			default:
				errors.report(line, "unexpected character '" + (char) c + "'");
		}
	}

	private void skip_line_comment() throws IOException
	{
		int c;
		while ((c = peek()) != -1 && c != '\n')
			read_char();
	}

	private void skip_block_comment() throws IOException
	{
		int c;
		int prev = 0;
		while ((c = read_char()) != -1)
		{
			if (prev == '*' && c == '/')
				return;
			prev = c;
		}
		errors.report(line, "unterminated block comment, missing '*/'");
	}

	public int get_error_count()
	{
		return errors.get_count();
	}

	public void close() throws IOException
	{
		reader.close();
		tokenWriter.close();
		errors.close();
	}

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("Usage: java src.lex_a.lex_a <source> [tokens_file] [errors_file]");
			System.exit(1);
		}
		String tokens = args.length > 1 ? args[1] : "tokens.txt";
		String errorsFile = args.length > 2 ? args[2] : "errors.txt";
		try
		{
			lex_a lexer = new lex_a(args[0], tokens, errorsFile);
			lexer.analyze();
			lexer.close();
			if (lexer.get_error_count() > 0)
				System.err.println(lexer.get_error_count() + " lexical error(s), see " + errorsFile);
		}
		catch (IOException e)
		{
			System.err.println("I/O error: " + e.getMessage());
			System.exit(1);
		}
	}
}
