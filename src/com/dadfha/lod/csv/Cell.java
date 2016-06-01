package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public class Cell extends SchemaEntity {	
	
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
	 */
	private int subRow = -1;
	
	/**
	 * Relative column in a schema starting from 0.
	 */
	private int col;
	
	/**
	 * The unique id for this cell within a processing context. Can be UUID for world-wide scope.
	 */
	private String id;
	
	/**
	 * The data model type in which the cell is mapped to.
	 */
	private String type;
	
	private String regEx;
	
	private String datatype;
	
	private String lang;
	
	private String value;
	
	/**
	 * Copy constructor.
	 * @param c
	 */
	public Cell(Cell c) {
		row = c.row;
		col = c.col;
		id = c.id;
		type = c.type;
		regEx = c.regEx;
		datatype = c.datatype;
		lang = c.lang;
		value = c.value;
		parentTable = c.parentTable;
		// Collection default copy constructor is deepcopy as long as the object type inside is immutable.
		properties = new HashMap<String, String>(c.getProperties()); 
	}
	
	/**
	 * Create cell.
	 * @param row
	 * @param col
	 * @param sTable
	 */
	public Cell(int row, int col, SchemaTable sTable) {
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
	public Cell(int row, int col, Map<String, String> properties, SchemaTable sTable) {
		this(row, col, sTable);
		this.properties.putAll(properties);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setType(String type) {
		this.type = type;
	}	

	public String getType() {
		return type;
	}
	
	public String getRegEx() {
		return regEx;
	}
	
	public void setRegEx(String regEx) {
		this.regEx = regEx;
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
	 * Empty Cell Schema is defined as a cell that holds Empty Value but contains no other schema property. 
	 * Therefore, a cell holding Empty Value with at least a single schema property is NOT considered an Empty Cell.    
	 * Depending on schema table definition, an Empty Value may be represented by a string, e.g. "n/a".
	 * If a schema has replace value map defined, it will happen before the validation of the cell schema.   
	 * By default, an Empty Cell has empty string value "" and is validated by "[^\\S\r\n]*?" regular expression.
	 * 
	 * TODO refactor empty value & replace value map to be at schema table level? or support at both level!!
	 * 
	 * @return true if this cell is 
	 */
	public boolean isEmpty() {
		return (id == null && type == null && regEx == null && datatype == null && lang == null && value.equals(parentTable.getEmptyValue()))? true : false;
	}
	
	/**
	 * Check if this cell is in a repeating row.
	 * @return boolean
	 */
	public boolean isInRepeatingRow() {
		return parentTable.getRow(row).isRepeat();
	}
	
	@Override
	public String getProperty(String propertyName) {		
		String retVal = super.getProperty(propertyName);
		if(retVal == null) {
			switch(propertyName.toLowerCase()) {
			case "row":
				retVal = Integer.toString(row);
				break;
			case "col":
				retVal = Integer.toString(col);
				break;
			case "id":
				retVal = id;
				break;
			case "regex":
				retVal = regEx;
				break;
			case "datatype":
				retVal = datatype;
				break;
			case "lang": // TODO move some of this to SchemaEntity class & do the same in SchemaRow
				retVal = lang;
				break;				
			case "type":
				retVal = type;
				break;
			case "value":
				retVal = value;
				break;
			default:
				throw new RuntimeException("Unrecognized property name: " + propertyName + " in cell: " + name);
			}						
		}
		return retVal;
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
	public void merge(Cell cell) {
		row = cell.row;
		col = cell.col;
		id = cell.id;
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
	 * 1. It represents the same cell coordinate [row, col] with the same ID.
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
		
		if(!(o instanceof Cell)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		Cell c = (Cell) o;
		
		if(row != c.row || col != c.col || (id.compareTo(c.id) != 0)) return false; 

		return true;
	}

	@Override
	public String getRefEx() {
		return parentTable.getRefEx() + ".@cell[" + row + "," + col + "]";
	}

}
