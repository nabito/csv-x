package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class SchemaRow extends SchemaEntity {
	
	/**
	 * Parent schema table.
	 */
	private SchemaTable parentTable;
	
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
	 * Constructor.
	 * @param rowNum
	 * @param sTable
	 */
	public SchemaRow(int rowNum, SchemaTable sTable) {
		// initialization
		this.setRowNum(rowNum);
		parentTable = sTable;
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
	
	public boolean isEmpty() {
		return (cells.size() == 0);
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
