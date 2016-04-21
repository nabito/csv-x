package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldRow {
	
	/**
	 * Expected number of field per row.
	 */
	private static final int INIT_COL_NUM = 100;
	
	/**
	 * List of field, i.e. column, containing in the row 
	 */
	private List<Field> fields = new ArrayList<Field>(INIT_COL_NUM);
	
	/**
	 * Row number. -1 indicates uninitialized state.
	 */
	private int rowNum = -1;
	
	/**
	 * Whether or not this row is used to recognized a section.
	 */
	private boolean isIdentityRow = false;
	
	/**
	 * Whether or not this row's schema is repeated line by line. 
	 */
	private boolean isRepeat = false;
	
	/**
	 * The exact number of times this row is repeated.
	 * Minus value indicates indefinite.
	 */
	private int repeatTimes = -1;
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	/**
	 * Constructor.
	 * @param rowNum
	 */
	public FieldRow(int rowNum) {
		this.setRowNum(rowNum);
	}	
	
	/**
	 * FIXME each column is not only defined by its regex anymore!!!
	 * 
	 * Construct a row of fields from its contents.
	 *  
	 *  
	 * @param rowNum
	 * @param cols The content of each field in a schema is defined to be regular expression for the field.
	 */
	public FieldRow(int rowNum, String[] cols) {		
		// For each column, populate each field
        for(int colNum = 0; colNum < cols.length; colNum++) {
        	// IMP check all field's properties like field type and init here at the object creation time.
        	fields.add(new Field(rowNum, colNum, cols[colNum]));	
        }
	}
	
	public Field getCol(int col) {
		return fields.get(col);
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public void addField(Field field) {
		fields.add(field);
	}

	public boolean isRepeat() {
		return isRepeat;
	}

	public void setRepeat(boolean isRepeat) {
		this.isRepeat = isRepeat;
	}

	public boolean isIdentityRow() {
		return isIdentityRow;
	}

	public void setIdentityRow(boolean isIdentityRow) {
		this.isIdentityRow = isIdentityRow;
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
