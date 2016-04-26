package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaRow {
	
	/**
	 * Expected number of cell per row.
	 */
	private static final int INIT_COL_NUM = 100;
	
	/**
	 * List of cell, i.e. column, containing in the row 
	 */
	private Map<Integer, Cell> cells = new HashMap<Integer, Cell>(INIT_COL_NUM);
	
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
		// initialization
		this.setRowNum(rowNum);
	}
	
	public Cell getCell(int col) {
		return cells.get(col);
	}
	
	public void addCell(Cell cell) {
		cells.put(cell.getCol(), cell);
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
