package com.dadfha.lod.csv;

public class SchemaColumn extends SchemaEntity {
	
	/**
	 * Class IRI for schema row.
	 */
	public static final String CLASS_IRI = Schema.NS_PREFIX + ":SchemaColumn";		
	
	/**
	 * Parent schema table.
	 */
	private SchemaTable parentTable;	
	
	/**
	 * Column number. -1 indicates uninitialized state.
	 */
	private int colNum = -1;

	/**
	 * Column repeating times. Minus value indicates infinite. [0..Inf]
	 */
	private int repeatTimes = 0;
	
	/**
	 * Constructor.
	 * @param colNum
	 * @param sTable
	 */
	public SchemaColumn(int colNum, SchemaTable sTable) {
		this.colNum = colNum;
		parentTable = sTable;
	}	

	/**
	 * @return the colNum
	 */
	public int getColNum() {
		return colNum;
	}

	/**
	 * @param colNum the colNum to set
	 */
	public void setColNum(int colNum) {
		this.colNum = colNum;
	}
	
	/**
	 * Check whether this column's schema is repeating.
	 * @return
	 */
	public boolean isRepeat() {
		return (getRepeatTimes() != 0);
	}

	/**
	 * The exact number of times this column is repeated.
	 * Minus value indicates infinite.
	 * @return int repeating times.
	 */
	public int getRepeatTimes() {
		return repeatTimes;
	}

	/**
	 * Set column's repeating times.
	 * @param repeatTimes
	 */
	public void setRepeatTimes(int repeatTimes) {
		this.repeatTimes = repeatTimes;		
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
		return parentTable.getRefEx() + ".@col[" + colNum + "]";
	}

}
