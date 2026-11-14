package src.sin_a;

import java.io.*;

/*
 * Error manager for the syntactic analyzer.
 * Writes every syntax error (token position, found token, expected symbols)
 * to the errors file and keeps the count of errors detected.
 */
public class sin_gest_error
{
	/* thrown by the parser to stop the analysis after a syntax error */
	public static class SyntaxError extends RuntimeException
	{
		public SyntaxError(String msg)
		{
			super(msg);
		}
	}

	private final Writer writer;
	private int count = 0;

	/*
	 * @brief: opens the errors file for writing
	 * @throws IOException if the file can't be opened
	 */
	public sin_gest_error(String errorsPath) throws IOException
	{
		this.writer = new BufferedWriter(new FileWriter(errorsPath));
	}

	/*
	 * @brief: registers a syntax error and builds the exception to throw
	 * @param: pos = 1-based position of the token, found = lexeme found, expected = what was expected
	 * @return: the exception, so the caller can write "throw errors.report(...)"
	 */
	public SyntaxError report(int pos, String found, String expected)
	{
		count++;
		String msg = "Syntax error (token " + pos + ", '" + found + "'): expected " + expected;
		try
		{
			writer.write(msg + "\n");
			writer.flush();
		}
		catch (IOException e)
		{
			System.err.println("Cannot write to errors file: " + e.getMessage());
		}
		return new SyntaxError(msg);
	}

	public int get_count()
	{
		return count;
	}

	public void close() throws IOException
	{
		writer.close();
	}
}
