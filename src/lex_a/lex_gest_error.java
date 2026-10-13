package src.lex_a;

import java.io.*;

/*
 * Error manager for the lexical analyzer.
 * Writes every lexical error (line number + clear message) to the errors file
 * and keeps the count of errors detected.
 */
public class lex_gest_error
{
	private final Writer writer;
	private int count = 0;

	/*
	 * @brief: opens the errors file for writing
	 * @param: path of the errors file
	 * @throws IOException if the file can't be opened
	 */
	public lex_gest_error(String errorsPath) throws IOException
	{
		this.writer = new BufferedWriter(new FileWriter(errorsPath));
	}

	/*
	 * @brief: registers a lexical error
	 * @param: line where it was detected, message in user terms
	 */
	public void report(int line, String msg) throws IOException
	{
		count++;
		writer.write("Lexical error (line " + line + "): " + msg + "\n");
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
