package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaRow extends SchemaEntity {
	
	/**
	 * Class IRI for schema row.
	 */
	public static final String CLASS_IRI = Schema.NS_PREFIX + ":SchemaRow";	
	
	/**
	 * Parent schema table.
	 */
	private SchemaTable parentTable;
	
	/**
	 * Expected number of cell per row.
	 */
	private static final int INIT_COL_NUM = 100;
	
	/**
	 * Collection of cells containing in the row indexed by column number.
	 */
	private Map<Integer, SchemaCell> cells = new HashMap<Integer, SchemaCell>(INIT_COL_NUM);
	
	/**
	 * Row number. -1 indicates uninitialized state.
	 */
	private int rowNum = -1;

	/**
	 * Row repeating times. Minus value indicates infinite.
	 */
	private int repeatTimes = 0;

	/**
	 * Constructor.
	 * @param rowNum
	 * @param sTable
	 */
	public SchemaRow(int rowNum, SchemaTable sTable) {
		this.rowNum = rowNum;
		parentTable = sTable;
	}
	
	/**
	 * Get schema for a cell in this row at a specified column.
	 * @param col column number.
	 * @return SchemaCell of specified column (col) or null if the schema for cell doesn't exist. 
	 */
	public SchemaCell getCell(int col) {
		return cells.get(col);
	}
	
	/**
	 * Get all schema cells within this schema row.
	 * @return Map<Integer, SchemaCell> between column number and its corresponding schema cell object.
	 */
	public Map<Integer, SchemaCell> getSchemaCells() {
		return cells;
	}	
	
	public void addCell(SchemaCell cell) {
		cells.put(cell.getCol(), cell);
	}

	/**
	 * Check whether this row's schema is repeating.
	 * @return
	 */
	public boolean isRepeat() {
		return (getRepeatTimes() != 0);
	}

	/**
	 * The exact number of times this row is repeated.
	 * Minus value indicates infinite.
	 * @return int repeating times.
	 */
	public int getRepeatTimes() {
		return repeatTimes;
	}

	/**
	 * Set row's repeating times.
	 * @param repeatTimes
	 */
	public void setRepeatTimes(int repeatTimes) {
		this.repeatTimes = repeatTimes;		
	}

	/**
	 * Return row number this schema row represents.
	 * @return
	 */
	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	
	public boolean isEmpty() {
		return (cells.size() == 0);
	}
	
	/**
	 * Create data row object from a blueprint schema row. 
	 * This is not the same as cloning since parent table will be of actual data table
	 * instead of schema table.  
	 * @param sRow
	 * @param dataRowNum
	 * @param parentDataTable
	 * @return SchemaRow
	 */
	public static SchemaRow createDataObject(SchemaRow sRow, int dataRowNum, SchemaTable parentDataTable) {
		SchemaRow dRow = new SchemaRow(dataRowNum, parentDataTable);
		dRow.properties.putAll(sRow.properties);
		dRow.repeatTimes = sRow.repeatTimes;
		return dRow;
	}
	
	/**
	 * Set the '@name' property of this schema row while also update variable register, if available, 
	 * inside hashmap collection of its parent schema table.
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {		
		if(parentTable == null) throw new RuntimeException("Parent table was not initialized for schema row: " + this);
		String oldName = getName();
		if(oldName != null) {			
			if(parentTable.hasVar(oldName)) {
				parentTable.removeVar(oldName);
				addProperty(METAPROP_NAME, name);
				parentTable.addVar(name, this);	
			} else {
				addProperty(METAPROP_NAME, name);
			}
		} else { // if it has never been set before, call setName()
			super.setName(name); 			
		}	
	}

	@Override
	public Schema getParentSchema() {
		return parentTable.getParentSchema();
	}

	@Override
	public SchemaTable getSchemaTable() {
		return parentTable;
	}

	@Override
	public String getRefEx() {
		return parentTable.getRefEx() + ".@row[" + rowNum + "]";
	}

}
