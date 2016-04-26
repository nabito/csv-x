package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaTable {
	
	/**
	 * Set this to two times the expected number of row per section. 
	 */
	public static final int INIT_ROW_NUM = 200;	
	
	/**
	 * Map between row number and its schema.
	 */	
	private Map<Integer, SchemaRow> schemaRows = new HashMap<Integer, SchemaRow>(INIT_ROW_NUM);

	public SchemaTable() {}
	
	/**
	 * Add schema row. If there are existing SchemaRow object it will be overwritten.
	 * @param sr
	 */
	public void addRow(SchemaRow sr) {
		schemaRows.put(sr.getRowNum(), sr);
	}
	
	public SchemaRow getRow(int rowNum) {
		return schemaRows.get(rowNum);
	}
	
	/**
	 * This method will check for already available row and cell schema and properly update their properties.
	 * It will create row and cell schema object as needed if there is none before.
	 * The prior cell's schema properties will be overwritten if there are duplicate properties.
	 * @param cell
	 */
	public void addCell(Cell cell) {
		SchemaRow sr = schemaRows.get(cell.getRow());
		if(sr != null) { // if schema object for the row is already there
			Cell c = sr.getCell(cell.getCol()); // check if there is already schema for the cell
			if(c != null) { // update schema info
				c.merge(cell);
			} else { // create new cell schema!
				sr.addCell(cell);
			}
		} else { // if there is no schema object for the row yet, create one
			sr = new SchemaRow(cell.getRow());
			sr.addCell(cell);
			schemaRows.put(cell.getRow(), sr);
		}
	}

}
