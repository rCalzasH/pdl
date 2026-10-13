package src.lex_a;

public enum Tokens
{
	WORD,                                   // keywords, attribute = keyword number (1-14)
	REAL, ENT, CAD, ID,                     // constants and identifiers
	ASIGRES, ASIG,                          // -=  =
	COMA, PUNTCOMA, DOSPUNT,                // , ; :
	PARIZ, PARDERCH, CORIZ, CORDERCH,       // ( ) { }
	SUMA, NEG, MENOR                        // + ! <
}
