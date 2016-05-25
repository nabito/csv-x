package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SchemaTable stores metadata for CSV's tabular structure.
 * 
 * String 'name' of schema table will never be null and always initialized by constructor
 * in case user hasn't define one.
 * 
 * @author Wirawit
 */
public class SchemaTable extends SchemaEntity {
	
	/**
	 * Set this to two times the expected number of row per section. 
	 */
	public static final int INIT_ROW_NUM = 200;	
	
	/**
	 * Parent schema of this schema table.
	 */
	private Schema parent;
	
	/**
	 * Map type.
	 */
	private String type;
	
	/**
	 * Empty value definition for this schema table.
	 */
	private String emptyValue; 
	
	/**
	 * TODO This is not needed anymore. Considering remove this.  
	 * 
	 * Collection of cell indexes being referenced in each schema table.
	 * 
	 * we want to store a collection of referenced cells, which can be searched
	 * based on <row,col> index instantly, coz' during each csv cell iteration,
	 * we need to check if that cell is being referred in the schema so that we
	 * can selectively save the cell value for later use (e.g. variable-value
	 * substitution). if we're to preserve all csv value for later use, it may
	 * cause out-of-memory problem. if we're to re-parse the value in referred
	 * position, it may be well too expensive operation.
	 * 
	 * private Map<Integer, Map<Integer, String>> refCells; is good construct to
	 * store mapping of cell's <row, col> index with its value. However, nested
	 * collection may need a class wrapper to be able to smoothly operated.
	 * 
	 * Rather, using Map<IntPair, String> where IntPair is a class representing
	 * just <row, col> index with proper hashvalue may serves as more memory
	 * efficient option with less hassles in coding.
	 */
	private Map<CellIndex, String> refCells = new HashMap<CellIndex, String>();	
	
	/**
	 * Common properties among all cells within this schema table.
	 */
	private Map<String, String> commonProps = new HashMap<String, String>();

	/**
	 * Map between row number and its schema.
	 * 
	 * To guarantee validation of CSV dimension, the golden rule for CSV-X schema is that for every existing cell 
	 * in CSV there must be corresponding schema definition even when the cell is meant to be empty.
	 *  
	 * The same is true in reverse direction wherever there is a schema definition for a cell, there must be actual 
	 * cell in CSV data that matches the schema properties.
	 * 
	 * This way there is no need to describe schema dimension as strict rectangular of rowNum x colNum nor to keep 
	 * tracking of size for each row and column.
	 * 
	 */	
	private Map<Integer, SchemaRow> schemaRows = new HashMap<Integer, SchemaRow>(INIT_ROW_NUM);

	/**
	 * Create SchemaTable. If name is not provided (i.e. null) schemaTable's name will be populated with randomly 
	 * generated string to be used internally. The string will start with '@' to prevent naming collision of 
	 * variable declared by user.   
	 * 
	 * The table name can be later replaced by user-defined value in the parsing process. 
	 * 
	 * @param name
	 * @param s
	 */
	public SchemaTable(String name, Schema s) {
		if(name == null) {			
			do { // random name that must be unique within the schema
				name = "@" + UUID.randomUUID().toString();	
			} while(s.getSchemaTable(name) != null);						
		} else {
			if(s.getSchemaTable(name) != null) throw new IllegalArgumentException("Schema table with the same name: " + name + " is already exist in the schema.");
		}
		this.name = name;
		parent = s;
	}
	
	/**
	 * Get parent schema that has this schema table as a member.
	 * @return Schema parent schema
	 */
	public Schema getParentSchema() {
		return parent;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Get table's empty value.
	 * @return
	 */
	public String getEmptyValue() {
		return emptyValue;
	}
	
	public String getCommonProp(String propName) {
		return commonProps.get(propName);
	}
	
	public void addCommonProp(String propName, String propVal) {
		commonProps.put(propName, propVal);
	}
	
	public void addAllCommonProps(Map<String, String> props) {
		commonProps.putAll(props);
	}

	/**
	 * Add schema row. If there are existing SchemaRow object it will be overwritten.
	 * @param sr
	 */
	public void addRow(SchemaRow sr) {
		schemaRows.put(sr.getRowNum(), sr);
	}
	
	/**
	 * Get schema row. 
	 * @param rowNum
	 * @return SchemaRow or null if not available.
	 */
	public SchemaRow getRow(int rowNum) {
		return schemaRows.get(rowNum);
	}
	
	/**
	 * Get cell.
	 * @param row
	 * @param col
	 * @return Cell or null if not available.
	 */
	public Cell getCell(int row, int col) {
		SchemaRow sr = schemaRows.get(row);
		if(sr != null) return sr.getCell(col);
		else return null;
	}
	
	/**
	 * This method will check for already existing row and cell schema and properly update their properties.
	 * It will create row and cell schema object as needed if there is none before.
	 * The prior cell's schema properties will be overwritten if there are duplicate properties.
	 * @param cell
	 */
	public void addCell(Cell cell) {
		SchemaRow sr = schemaRows.get(cell.getRow());
		if(sr != null) { // if schema object for the row is already there
			Cell c = sr.getCell(cell.getCol()); // check if there is already schema for the cell
			if(c != null) { // update schema info
				c.merge(cell);
			} else { // create new cell schema!
				sr.addCell(cell);
			}
		} else { // if there is no schema object for the row yet, create one
			sr = new SchemaRow(cell.getRow(), this);
			sr.addCell(cell);
			schemaRows.put(cell.getRow(), sr);
		}
	}
	
	@Override
	public String getProperty(String propertyName) {		
		String retVal = super.getProperty(propertyName);
		if(retVal == null) {
			switch(propertyName) {
			case "type":
				retVal = type;
				break;
			case "emptyValue":
				retVal = emptyValue;
				break;			
			default:
				throw new RuntimeException("Unrecognized property name: " + propertyName + " in schema table: " + name);
			}						
		}
		return retVal;
	}
	
	/**
	 * Check if a cell is referenced in the schema table.
	 * @param row
	 * @param col
	 * @return boolean whether or not a cell is being referenced in the table.
	 */
	public boolean isCellRef(int row, int col) {
		return refCells.containsKey(new CellIndex(row, col));
	}
	
	/**
	 * Record a reference to cell.
	 * @param row
	 * @param col
	 * @param value the value of cell being referenced. Accept null if not known at the time.
	 */
	public void saveRefCell(int row, int col, String value) {		
		refCells.put(new CellIndex(row, col), value);
	}	
	
	/**
	 * This method won't save unreferenced cell.
	 * @param row
	 * @param col
	 * @param val
	 */
	public void updateRefCellVal(int row, int col, String val) {
		refCells.replace(new CellIndex(row,col), val);
	}

	@Override
	public SchemaTable getSchemaTable() {
		return this;
	}

	@Override
	public String getRefEx() {
		return "@table[" + name + "]";
	}

}
