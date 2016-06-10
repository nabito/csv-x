package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * The value to substitute when the reading is empty, a.k.a. nothing in between value separator (e.g. comma). 
	 * Note that empty string (a.k.a. value within quote "") is regarded as a value and won't make a cell Empty Cell.
	 */
	private String emptyCellFill = null;
	
	/**
	 * Regular Expression for any whitespace characters except newline.
	 */
	@SuppressWarnings("unused")
	private static final String REGEX_WS = "[^\\S\r\n]*?";
	
	/**
	 * Map of the value(s) to replace.
	 * Note that empty value always has key of empty string ("").
	 */
	private Map<String, String> replaceValueMap;	
	
	/**
	 * @deprecated This is not needed anymore. Considering remove this.  
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
	 * Mapping between variable name and schema entity. 
	 * Variable has global scope within a schema table.
	 */
	private Map<String, SchemaEntity> varMap = new HashMap<String, SchemaEntity>();		
	
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
	 * Schema Property definitions shared among all schema entity in this schema table. 
	 * This collection also serves as variable name-schema property mapping because a 
	 * schema property cannot exists alone without a name anyway. 
	 */
	private Map<String, SchemaProperty> sProps = new HashMap<String,SchemaProperty>();	

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
		setName(name);
		parent = s;
	}
	
	/**
	 * OPT considering creating data classes for all schema entity.
	 * 
	 * Create copy for data object that has everything except varMap, SchemaRow, and its SchemaCell inside. 
	 * This is used for data model creation based on schema blueprint. Each actual data table will 
	 * still holds reference to its parent schema.
	 */
	public static SchemaTable createDataObject(SchemaTable st, String tableName) {
		SchemaTable newTable = new SchemaTable(tableName, st.parent);
		newTable.commonProps.putAll(st.commonProps);
		newTable.emptyCellFill =  st.emptyCellFill;
		newTable.properties.putAll(st.properties);
		newTable.replaceValueMap.putAll(st.replaceValueMap);
		newTable.sProps.putAll(st.sProps);
		return newTable;
	}
	
	/**
	 * Get parent schema that has this schema table as a member.
	 * @return Schema parent schema
	 */
	public Schema getParentSchema() {
		return parent;
	}
	
	/**
	 * Get Empty Cell filling.
	 * @return String to fill an Empty Cell or Java null as default.
	 */
	public String getEmptyCellFill() {
		return emptyCellFill;
	}	
	
	/**
	 * Set Empty Cell filling.
	 * @param val
	 */
	public void setEmptyCellFill(String val) {
		emptyCellFill = val;
	}
	
	/**
	 * Get replacing value from the map.
	 * @param key the original cell's value.
	 * @return String of replacing value or null if there is no substitute value for the key.
	 */
	public String getReplaceValue(String key) {		
		return replaceValueMap.get(key); 
	}
	
	public Map<String, String> getReplaceValueMap() {
		return replaceValueMap;
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
	 * Get schema row. 
	 * @param rowNum
	 * @return SchemaRow or null if not available.
	 */
	public SchemaRow getRow(int rowNum) {
		return schemaRows.get(rowNum);
	}
	
	/**
	 * Get all schema rows inside this schema table.
	 * @return Map<Integer, SchemaRow> between row number and its corresponding schema row object.
	 */
	public Map<Integer, SchemaRow> getSchemaRows() {
		return schemaRows;
	}
	
	/**
	 * Add schema row. If there are existing SchemaRow object it will be overwritten.
	 * @param sr
	 */
	public void addRow(SchemaRow sr) {
		schemaRows.put(sr.getRowNum(), sr);
	}	
	
	/**
	 * Get cell.
	 * @param row
	 * @param col
	 * @return Cell or null if not available.
	 */
	public SchemaCell getCell(int row, int col) {
		SchemaRow sr = schemaRows.get(row);
		return sr.getCell(col);
	}
	
	/**
	 * This method will check for already existing row and cell schema and properly update their properties.
	 * It will create row and cell schema object as needed if there is none before.
	 * The prior cell's schema properties will be overwritten if there are duplicate properties.
	 * @param cell
	 */
	public void addCell(SchemaCell cell) {
		SchemaRow sr = schemaRows.get(cell.getRow());
		if(sr != null) { // if schema object for the row is already there
			SchemaCell c = sr.getCell(cell.getCol()); // check if there is already schema for the cell
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
	
	/**
	 * Check if this schema table contains schema property with the name.
	 * @param name
	 * @return true or false.
	 */
	public boolean hasSchemaProperty(String name) {
		return sProps.containsKey(name);
	}
	
	/**
	 * Get schema property definition object by its name.
	 * @param propName
	 * @return SchemaProperty object or null if schema property with the name is not available.
	 */
	public SchemaProperty getSchemaProperty(String propName) {
		return sProps.get(propName);
	}
	
	/**
	 * Get collection holding map between schema property name and its object inside this schema table.
	 * @return Map<String, SchemaProperty>
	 */
	public Map<String, SchemaProperty> getSchemaProperties() {
		return sProps;
	}
	
	/**
	 * Add a schema property definition to the schema table. 
	 * If schema property of the same name already exists it will be overwritten.
	 * 
	 * Note that this is done in the same fashion as schema table is for schema. 
	 * Thus, its name is automatically registered as a variable for the schema table.
	 *   
	 * @param sProp
	 */
	public void addSchemaProperty(SchemaProperty sProp) {
		sProps.put(sProp.getName(), sProp);
	}
	
	/**
	 * Remove a schema property by name from this schema table.
	 * @param propName
	 */
	public void removeSchemaProperty(String propName) {
		sProps.remove(propName);
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
	 * @deprecated
	 * Record a reference to cell.
	 * @param row
	 * @param col
	 * @param value the value of cell being referenced. Accept null if not known at the time.
	 */
	public void saveRefCell(int row, int col, String value) {		
		refCells.put(new CellIndex(row, col), value);
	}	
	
	/**
	 * @deprecated
	 * This method won't save unreferenced cell.
	 * @param row
	 * @param col
	 * @param val
	 */
	public void updateRefCellVal(int row, int col, String val) {
		refCells.replace(new CellIndex(row,col), val);
	}
	
	/**
	 * Check if there is this variable name declared in the schema.
	 * @param name
	 * @return
	 */
	public boolean hasVar(String name) {
		return varMap.containsKey(name);
	}	
	
	/**
	 * Get schema entity pointed by variable name.
	 * @param name
	 * @return SchemaEntity or null if variable with the name is not found/mapped with null.
	 */
	public SchemaEntity getVarSchemaEntity(String varName) {
		return varMap.get(varName);
	}
	
	/**
	 * Add a variable-schema entity map to the schema.
	 * If variable of the same name already exists, it will throw an error. 
	 * @param varName
	 * @param se
	 */
	public void addVar(String varName, SchemaEntity se) {
		if(varMap.containsKey(varName)) throw new RuntimeException("Variable name '" + varName + "' already exists and is pointed to schema entity: " + se.toString() + "");
		varMap.put(varName, se);
	}
	
	/**
	 * Remove a variable from variable registry.
	 * @param varName
	 */
	public void removeVar(String varName) {
		varMap.remove(varName);
	}
	
	/**
	 * To validate a CSV cell against its schema definition at its corresponding row, col in this schema table.
	 * 
	 * Note that, if a schema has replace value map defined, it will happen "before the validation" of the cell schema.
	 * 
	 * @param row
	 * @param col
	 * @param val
	 * @return boolean
	 */
	public boolean validate(int row, int col, String val) {
		
		// check inputs
		if(row < 0 || col < 0) throw new IllegalArgumentException("Row and Col value must NOT be negative.");
		
		SchemaCell c = getCell(row, col);
		
		if(c == null) {
			System.err.println("Error: Dimension Mismatched - There is no schema definition at: [" + row + "," + col + "]"); 
			return false;
		}
		
		// check if the cell is a valid Empty Cell
		if(c.isEmpty()) return (val == null)? true : false;		
		
		// TODO check datatype and restrictions according to XML Schema Datatype 1.1 (http://www.w3.org/TR/xmlschema11-2/)
		// also the syntax for constraints may be referred from CSVW specs
		// our key point is to design CSV-X schema syntax to be able to express all datatype & restrictions
		// then translate that into XML model for native-XML validation.
		String datatype = c.getDatatype();
		if(datatype != null) {			
			switch(datatype) {
			// mapping between XML datatype and Java datatype in defined in JAXB standard (http://docs.oracle.com/javaee/5/tutorial/doc/bnazq.html)
			case "string":
				break;			
			case "integer":
				break;
			case "int":
				break;
			case "long":
				break;
			case "short":
				break;
			case "decimal":
				break;
			case "float":
				break;
			case "double":
				break;
			case "boolean":
				break;
			case "byte":
				break;
			case "QName":
				break;
			case "dateTime":
				break;
			case "base64Binary":
				break;
			case "hexBinary":
				break;
			case "unsignedInt":
				break;
			case "unsignedShort":
				break;
			case "unsignedByte":
				break;
			case "time":
				break;
			case "date":
				break;
			case "g":
				break;
			case "anySimpleType":
				break;
			case "duration":
				break;
			case "NOTATION":
				break;				
			default:
				throw new RuntimeException("Unsupported datatype: " + datatype);
			}
			
		}
		
		// validate parsed CSV record against CSV-X cell schema's regular expression.
		String regEx = c.getRegEx();
		if(regEx != null) {
		    Pattern p = Pattern.compile(regEx, Pattern.DOTALL);
		    Matcher m = p.matcher(val);
		    if(!m.find()) {
		    	System.err.println("Error: Regular Expression Mismatched at: [" + row + "," + col + "]");
		    	return false;			
		    }
		}
		
		return true;
	}
	
	/**
	 * Change the '@name' property of this schema table while also update its register 
	 * inside hashmap collection of its parent schema. Therefore, it's required that the
	 * table is already initialized with its parent schema. 
	 * 
	 * Remarks: 
	 * 
	 * 1. Default table name cannot be changed. 
	 * Any attempt to do so will cause an error to be thrown.
	 * 
	 * 2. If parent schema has not yet bind with the table, it will be doen so in this method.
	 * 
	 * @param newName
	 */
	@Override
	public void changeName(String newName) {	
		if(parent == null) throw new RuntimeException("Parent schema was not initialized for schema table: " + this);
		String oldName = getName();			
		if(oldName != null) {
			if(oldName.equals(Schema.DEFAULT_TABLE_NAME)) throw new RuntimeException("The default table name cannot be changed.");
			if(parent.hasSchemaTable(oldName)) parent.removeSchemaTable(oldName);
			addProperty(METAPROP_NAME, newName);
		} else { // if it has never been set before, call setName()
			super.setName(newName); 			
		}
		parent.addSchemaTable(this);
	}		

	@Override
	public SchemaTable getSchemaTable() {
		return this;
	}

	@Override
	public String getRefEx() {
		return "@table[" + getName() + "]";
	}

}
