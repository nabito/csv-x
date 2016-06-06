package com.dadfha.lod.csv;

import java.util.Map;

public class SchemaCell extends SchemaEntity {	
	
	/**
	 * Parent schema table this cell is contained in.
	 */
	private SchemaTable parentTable;
	
	/**
	 * Relative row in a schema starting from 0.
	 */
	private int row;
	
	/**
	 * An index of subrow inside a repeating row starting from 0.
	 * -1 indicates uninitialized state.
	 * 
	 * Note: this is not used at version 1.0 since there is no schema model to describe subrow just yet.
	 * Also, actual parsed in subrow data will be counted as normal row, since the principle is to have 
	 * a master schema table as a blueprint for an actual schema data model expanded from CSV contents.
	 */
	private int subRow = -1;
	
	/**
	 * Relative column in a schema starting from 0.
	 */
	private int col;
	
	/**
	 * Copy constructor.
	 * @param c
	 */
	public SchemaCell(SchemaCell c) {
		super(c);
		parentTable = c.parentTable;
		row = c.row;
		subRow = c.subRow;
		col = c.col;		 
	}
	
	/**
	 * Create cell.
	 * @param row
	 * @param col
	 * @param sTable
	 */
	public SchemaCell(int row, int col, SchemaTable sTable) {
		this.row = row;
		this.col = col;
		parentTable = sTable;
	}
	
	/**
	 * Create cell with properties.
	 * @param row
	 * @param col
	 * @param properties
	 * @param sTable
	 */
	public SchemaCell(int row, int col, Map<String, String> properties, SchemaTable sTable) {
		this(row, col, sTable);
		this.properties.putAll(properties);
	}
	
	/**
	 * Create data cell object.
	 * @param schemaCell
	 * @param parentDataTable
	 * @param value
	 * @return
	 */
	public static SchemaCell createDataObject(SchemaCell schemaCell, SchemaTable parentDataTable, String value) {
		SchemaCell dataCell = new SchemaCell(schemaCell);
		dataCell.parentTable = parentDataTable;
		dataCell.setValue(value);
		return dataCell;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}
	
	public int getSubRow() {
		return subRow;
	}

	public void setSubRow(int subRow) {		
		this.subRow = subRow;
	}	

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	/**
	 * Check if this is an Empty Cell Schema.
	 * 
	 * Empty Cell Schema is defined as a cell that contains no value nor other schema property. 
	 * Therefore, a cell holding Empty Value with at least a single schema property is NOT considered an Empty Cell.
	 *     
	 * Note that empty string value "" is regarded as a value making a cell NOT an Empty Cell.
	 * 
	 * The content of all Empty Cells can be filled with a value during parsing in if '@emptyCellFill' is defined in 
	 * the schema table, making the cell non-empty thus cell schema definition should anticipate the filled value too.
	 *  
	 * The filling is mutual exclusive to '@replaceValueMap' operation, meaning if an Empty Cell is filled 
	 * with a value specified in '@emptyCellFill', it won't be subjected to value replacement of '@replaceValueMap'. 
	 * 
	 * @return true if this cell is an Empty Cell by definition. 
	 */
	public boolean isEmpty() {
		String value = getValue(); 
		return (properties.isEmpty() && value == null)? true : false;
	}
	
	/**
	 * Check if this cell is in a repeating row.
	 * @return boolean
	 */
	public boolean isInRepeatingRow() {
		return parentTable.getRow(row).isRepeat();
	}
	
	@Override
	public SchemaTable getSchemaTable() {
		return parentTable;
	}

	@Override
	public Schema getParentSchema() {
		return parentTable.getParentSchema();
	}	
	
	/**
	 * Merge cells content. Any duplicate properties will be overwritten.
	 * @param cell
	 */
	public void merge(SchemaCell cell) {
		row = cell.row;
		subRow = cell.subRow;
		col = cell.col;
		if(parentTable != cell.parentTable) throw new RuntimeException("Cell Schema of different Schema Table cannot be merged.");
		properties.putAll(cell.properties);
	}
	
	/**
	 * Hashcode = row's lower 16bits from 17th position and row's higher 16bits entering left-to-right 
	 * LSB first from 16th position (position count from 1, LSB from left-side).
	 * Then XOR everything with col.
	 * 
	 * This biased bits arrangement is designed to minimize hash value collision given high correlation 
	 * between row and col value, each are better placed at different bit position.
	 * Moreover, it's more common to have higher number of row than col, thus row's bits greater than 16th 
	 * are padded to the right from center of bits string.
	 * 
	 * OPT may be compare performance (e.g. collision %) of this hash vs. eclipse auto-generated
	 */
	public int hashCode() {		
	    int rowTopBits = row &= 0xFF00; 
	    int distance = Math.abs(16 - Integer.numberOfLeadingZeros(rowTopBits));
	    int hash = ( (rowTopBits >>> distance) | (row << 16) ) ^ col;
	    return hash;
	}
	
	/**
	 * A pair of field is considered equal if and only if:
	 * 1. It represents the same cell coordinate [row, col] and subRow with the same parent table.
	 * 2. It is exactly the same Java object for 1.
	 * 3. It has the same hash code.
	 * 4. It is of the same type 'Cell'.
	 * 
	 * Note: The schema properties of both cells may not be the same which regarded as conflict in properties 
	 * but still considered equal objects.    
	 * 
	 */
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof SchemaCell)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		SchemaCell c = (SchemaCell) o;
		
		if(row != c.row || col != c.col || parentTable != c.parentTable) return false; 

		return true;
	}

	@Override
	public String getRefEx() {
		return parentTable.getRefEx() + ".@cell[" + row + "," + col + "]";
	}

}
