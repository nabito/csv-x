package com.dadfha.lod.csv;

public class EmptyField extends Cell {
	
	/**
	 * TODO consider removing this too, but preserving notion of "Empty field regex" in Field class
	 * Copy constructor for Field.
	 * @param f
	 */
	public EmptyField(Cell f) {
		super(f);
	}
	
	/**
	 * Match with regular expression of "[^\\S\r\n]*?" which means all whitespace characters except new line \r\n. 
	 * @param row
	 * @param col
	 */
	public EmptyField(int row, int col) {
		super(row, col, "[^\\S\r\n]*?");
	}

}
