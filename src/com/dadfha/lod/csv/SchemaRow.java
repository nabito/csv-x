package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaRow {
	
	/**
	 * Expected number of cell per row.
	 */
	private static final int INIT_COL_NUM = 100;
	
	/**
	 * List of cell, i.e. column, containing in the row 
	 */
	private List<Cell> cells = new ArrayList<Cell>(INIT_COL_NUM);
	
	/**
	 * Subrows accommodate different schema definitions within repeating rows.
	 * 
	 * FIXME there's no way to store infinite subrow definitions, therefore must store by range selection  
	 * 
	 */
	private List<SchemaRow> subRows = new ArrayList<SchemaRow>(SchemaTable.INIT_ROW_NUM);
	
	private Map<CellSelection, Property> subRows = new HashMap<CellSelection, Cell>();
	
	/**
	 * Row number. -1 indicates uninitialized state.
	 */
	private int rowNum = -1;
	
	/**
	 * The exact number of times this row is repeated.
	 * Minus value indicates indefinite.
	 */
	private int repeatTimes = 0;
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	/**
	 * Constructor.
	 * @param rowNum
	 */
	public SchemaRow(int rowNum) {
		this.setRowNum(rowNum);
	}
	
	public Cell getCol(int col) {
		return cells.get(col);
	}
	
	public void addCell(Cell cell) {
		// FIXME the cell col number should be checked and add at certain index to preserve index-colNum mapping
		// also must check size of the list as in SchemaTable for SchemaRow
		cells.add(cell);
	}

	public boolean isRepeat() {
		return (repeatTimes != 0);
	}

	public int getRepeatTimes() {
		return repeatTimes;
	}

	public void setRepeatTimes(int repeatTimes) {
		this.repeatTimes = repeatTimes;
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	public void addProperty(String key, Object val) {
		properties.put(key, val);
	}	

}
