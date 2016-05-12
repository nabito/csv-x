package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaRow implements SchemaEntity {
	
	/**
	 * Expected number of cell per row.
	 */
	private static final int INIT_COL_NUM = 100;
	
	/**
	 * List of cell, i.e. column, containing in the row.
	 */
	private Map<Integer, Cell> cells = new HashMap<Integer, Cell>(INIT_COL_NUM);
	
	/**
	 * Row number. -1 indicates uninitialized state.
	 */
	private int rowNum = -1;
	
	/**
	 * Row repeating times. Minus value indicates indefinite.
	 */
	private int repeatTimes = 0;
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, String> properties = new HashMap<String, String>();
	
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

	/**
	 * Check whether this row's schema is repeating.
	 * @return
	 */
	public boolean isRepeat() {
		return (getRepeatTimes() != 0);
	}

	/**
	 * The exact number of times this row is repeated.
	 * Minus value indicates indefinite.
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

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}	
	
	/**
	 * Add property to the SchemaRow.
	 * Any existing property with the same name will be overwritten.
	 * @param key
	 * @param val
	 */
	public void addProperty(String key, String val) {
		properties.put(key, val);
	}
	
	/**
	 * Add properties to the SchemaRow. 
	 * Any existing properties with the same name will be overwritten. 
	 * @param properties
	 */
	public void addProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}
	
	public boolean isEmpty() {
		return (cells.size() == 0);
	}

}
