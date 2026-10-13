package src.lex_a;

import java.util.LinkedHashMap;
import src.error_g.*;

/*this class implements the logic behind the symbol table, by using
a linkedhashmap in order to avoid collision bewteen diferent elements*/
public class Symbol_table
{
	private LinkedHashMap<String, ts_entry> table;
	private int position;

//constructor
/*this class instead a normal hashmap cause its already handles
the colission bwetween diff elements in the map*/
public Symbol_table()
{
	this.table = new LinkedHashMap<>();
	this.position = 0;
}
/*
*@brief: set of methods to interact with the atributes of the instance
*@param: the asked atribute to insert/modify or void if is a getter 
*@return: void or the atribute asked if is a getter
*/ 
public Symbol_table get_table()
{
	return (this.table);
}

public int get_position()
{
	return (this.position)
}

public void set_table(Symbol_table ts)
{
	if(ts == null)
		return;
	this.table = ts;
}
public void set_position(int pos)
{
	if (pos < 0)
		return;
	this.position = pos;
}
/*
*@brief: searches for a given element in the table
*@param: the key to look for 
*@return: true if the table contains the given key or false if else
*/
public boolean search_on_table(String key)
{
	if (key == null)
		return (false);
	return (this.table.containsKey(key));
}
/*
*@brief: this method removes a key and value inside the table
*@param: the key to look for;
*@return: void (-)
*/
public void remove_from_table(String key)
{
	if (key == null)
		return;
	this.table.remove(key);
}
/*
*@brief: adds to the table the given simbol
*@param: the symbo to add
*@return: void (-)
*/
public void add_to_table(String to_add, Ts_entry entry)
{
	if (entry == null || to_add == null)
		return;
	this.table.put(to_add, entry);
	this.pos++;
}
/*
*@brief: this method gets an element, searching by key
*@param: the key to look for, and the entry to do eq 
*@return: the element given;
*/
public ts_entry get_element(String key)
{
	if (key == null)
		return null;w
	return (this.table.get(Key))
}
}