package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaTable implements SchemaEntity {
	
	/**
	 * Set this to two times the expected number of row per section. 
	 */
	public static final int INIT_ROW_NUM = 200;	
	
	/**
	 * Map between row number and its schema.
	 * 
	 * To guarantee validation of CSV dimension, the golden rule for CSV-X schema is that for every existing cell 
	 * in CSV there must be corresponding schema definition even when the cell is meant to be empty.
	 *  
	 * The same is true in reverse direction wherever there is a schema definition for a cell, there must be actual 
	 * cell in CSV data that matches the schema properties.
	 * 
	 * This way there is no need to describe schema dimension as strict rectangular of rowNum x colNum nor to keep 
	 * tracking of size for each row and column.
	 * 
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
	
	/**
	 * Get schema row. 
	 * @param rowNum
	 * @return SchemaRow or null if not available.
	 */
	public SchemaRow getRow(int rowNum) {
		return schemaRows.get(rowNum);
	}
	
	/**
	 * Get cell.
	 * @param row
	 * @param col
	 * @return Cell or null if not available.
	 */
	public Cell getCell(int row, int col) {
		SchemaRow sr = schemaRows.get(row);
		if(sr != null) return sr.getCell(col);
		else return null;
	}
	
	/**
	 * This method will check for already existing row and cell schema and properly update their properties.
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
