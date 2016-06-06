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
	 * Collection of cells containing in the row indexed by column number.
	 */
	private Map<Integer, SchemaCell> cells = new HashMap<Integer, SchemaCell>(INIT_COL_NUM);
	
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
