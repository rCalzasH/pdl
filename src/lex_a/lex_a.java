package src.lex_a;

import java.io.*;
/*
*this class pretrends to implement the automaton which
*decides the token to gen depending on the state, filling
*in with te transition
*/

public class lex_a
{
	private FileReader reader;
	private FileWriter writer;
	private File file;
	static File tokens_text = new File("../../tokens.txt");
	private int line;

	/*
	*@brief: sets up a lex_a, with its reader and a writer;
	*@param: the string which represents the filepath associated to the tokens_txt to gen
	*@return: returns null if anything goes wrong
	*@Throws Ioexception 
	*/
	public  lex_a(String file_path)
	{
		this.file = new File(file_path);
		if(this.file == null || !this.file.canRead() ||!this.tokens_text.canWrite())
			return null;
		this.reader = new FileReader(this.file);
		this.writer = new FileWriter(this.file);
	}
	
	public String gen_token(int c, String value)
	{
		switch (c)
		{
			case (-1 || 32 || 10 || 9))
				System.exit(0);
				break;
			case (isNumeric(c)):
				value+=c;
				return (token_gen_num(value));
				break;
			case (isAlphabetic(c)):
				value+=c;
				return (token_gen_word(value));
				break;
			case ('+'):
				return	(token_gen_sum());
				break;
			case ('!'):
				return (token_gen_diff());
				break;
			case ('<'):
				return (token_gen_minor())
				break;
			case ('('):
				return (token_gen_open_par());
				break;
			case (')'):
				return(token_gen_close_par());
				break;
			case('{'):
				return(token_gen_open_key());
				break();
			case('}'):
				return(token_gen_close_par());
				break;
			case('"'):
				return(token_gen_com());
				break;
			case(':'):
				return(token_gen_points());
				break;
			case(';'):
				return(token_gen_pc());
				break;
			case('.'):
				value+=c;
				return (token_gen_real(String value));
				break;
			case('-')
				value+=c;
				return (token_gen_neg_number(String value));
				break;
		}
	}
}