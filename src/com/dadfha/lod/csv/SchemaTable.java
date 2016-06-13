package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
	 * Note that empty value always has key of empty string ("") 
	 * and is not the same as Empty Cell definition.
	 */
	private Map<String, String> replaceValueMap = new HashMap<String, String>();	
	
	/**
	 * Set of values to be ignored during validation.
	 * 
	 * Note that though the validation of the value is skipped but the dimension 
	 * check whether there is a cell schema definition during validation is still intact.
	 */
	private Set<String> ignoreValues = new HashSet<String>();
		
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
	 */	
	private Map<Integer, SchemaRow> schemaRows = new HashMap<Integer, SchemaRow>(INIT_ROW_NUM);
	
	/**
	 * This collection serves as property name-schema property mapping.
	 * It's shared among all schema entity in this schema table. 
	 */
	private Map<String, SchemaProperty> sProps = new HashMap<String,SchemaProperty>();
	
	/**
	 * Map collection for schema data objects.
	 */
	private Map<String, SchemaData> sDataMap = new HashMap<String,SchemaData>();

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
		assert(name != null) : "From @table[name] syntax, declaring table without name is not allowed.";
		if(name == null) {			
			do { // random name that must be unique within the schema
				name = "@" + UUID.randomUUID().toString();	
			} while(s.getSchemaTable(name) != null);						
		} else {
			if(s.hasSchemaTable(name)) throw new IllegalArgumentException("Schema table with the same name: " + name + " is already exist in the schema.");
		}
		parent = s;
		setTableName(name);		
	}
	
	/**
	 * Create copy for data object that has everything except SchemaRow, and its SchemaCell inside. 
	 * This is used for data model creation based on schema blueprint. 
	 * 
	 * @param dSchema
	 * @param st
	 * @param tableName
	 * @return SchemaTable
	 */
	public static SchemaTable createDataObject(Schema dSchema, SchemaTable st, String tableName) {
		SchemaTable newTable = new SchemaTable(tableName, dSchema);
		newTable.commonProps.putAll(st.commonProps);
		newTable.emptyCellFill =  st.emptyCellFill;
		// copy all table properties except table name
		for(Map.Entry<String, String> e : st.properties.entrySet()) {
			String key = e.getKey();
			String val = e.getValue();
			if(!key.equals(METAPROP_TBLNAME)) newTable.properties.put(key, val);
		}
		newTable.replaceValueMap.putAll(st.replaceValueMap);
		newTable.ignoreValues.addAll(st.ignoreValues);
		newTable.sProps.putAll(st.sProps);
		newTable.varMap.putAll(st.varMap);
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
	 * Check if the replace value map for this table has a mapping for the key. 
	 * @param key
	 * @return true or false.
	 */
	public boolean hasReplaceValueFor(String key) {
		return replaceValueMap.containsKey(key);
	}
	
	/**
	 * Get replacing value from the map with the specified key. 
	 * 
	 * Note that if null value is return, it could either mean that 
	 * the key is mapped with null or there's no such mapping.
	 * 
	 * So user must check before calling this method using hasReplaceValueFor().
	 * 
	 * @param key
	 * @return String of replacing value or null.
	 */
	public String getReplaceValue(String key) {
		return replaceValueMap.get(key);		
	}
	
	public Map<String, String> getReplaceValueMap() {
		return replaceValueMap;
	}
	
	/**
	 * Check if a value is marked to be ignored.
	 * @param val
	 * @return boolean true or false.
	 */
	public boolean isIgnoreValue(String val) {
		return ignoreValues.contains(val);
	}
	
	/**
	 * Add a value to the ignore set.
	 * @param val
	 */
	public void addIgnoreValue(String val) {
		ignoreValues.add(val);
	}
	
	/**
	 * Get the whole ignore values set.
	 * @return Set<String>
	 */
	public Set<String> getIgnoreValuesSet() {
		return ignoreValues;
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
	 * Add a schema cell to this schema table or merge with an existing one. This method checks for an already 
	 * existing row and cell schema and properly update their properties (merge). If there are properties with 
	 * the same name prior cell's schema properties will be overwritten. It also creates new row schema object 
	 * as needed if there is none before. This way, user doesn't need to write schema row definition if there's
	 * no particular reason because a schema cell already implies an existence of a schema row it resides in.  
	 * 
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
	
	public void addSchemaData(SchemaData sData) {
		sDataMap.put(sData.getDataName(), sData);
	}

	public void removeSchemaData(String dataName) {
		sDataMap.remove(dataName);
	}
	
	public boolean hasSchemaData(String name) {
		return sDataMap.containsKey(name);
	}
	
	public SchemaData getSchemaData(String dataName) {
		return sDataMap.get(dataName);
	}

	public Map<String, SchemaData> getSchemaDataMap() {
		return sDataMap;
	}
	
	/**
	 * Add a schema property definition to the schema table. 
	 * If schema property of the same name already exists it will be overwritten.
	 *   
	 * @param sProp
	 */
	public void addSchemaProperty(SchemaProperty sProp) {
		sProps.put(sProp.getPropertyName(), sProp);
	}
	
	/**
	 * Remove a schema property by name from this schema table.
	 * @param propName
	 */
	public void removeSchemaProperty(String propName) {
		sProps.remove(propName);
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
		if(varMap.containsKey(varName)) throw new RuntimeException("Variable name '" + varName + "' already exists and is pointed to schema entity: " + se);
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
	 * IMP consider moving this fn back to SchemaProcessing
	 * 
	 * Currently supported modes are:
	 * 
	 * MODE_IGNORE_ERR_MSG
	 * 
	 * @param sRow schema row number
	 * @param sCol schema column number
	 * @param val CSV's cell value
	 * @param mode
	 * @return boolean true if the validate is success or false otherwise.
	 */
	public boolean validate(int sRow, int sCol, String val, int mode) {
		
		// initialize processing mode
		boolean ignoreErrMsg = ((SchemaProcessor.MODE_IGNORE_ERR_MSG & mode) != 0)? true : false;
		//ignoreErrMsg = false;
		
		// check inputs
		if(sRow < 0 || sCol < 0) throw new IllegalArgumentException("Row and Col value must NOT be negative.");
		
		// check dimension, whether there is schema cell definition at specified row and column
		SchemaCell c = getCell(sRow, sCol);		
		if(c == null) {
			if(!ignoreErrMsg) System.err.println("Error: Dimension Mismatched - There is no schema definition at: [" + sRow + "," + sCol + "]"); 
			return false;
		}
		
		// search in ignore values set, exit if found
		if(isIgnoreValue(val)) return true; // IMP should print out INFO msg on this, since it may change the validation result without notice.
		
		// check if the cell is a valid Empty Cell
		if(c.isEmpty()) {
			if(val == null) return true;
			else {
				if(!ignoreErrMsg) System.err.println("Error: Schema Mismatched - Expecting EmptyCell but found cell value '" + val + "' at: [" + sRow + "," + sCol + "]");
				return false;		
			}
		} else { // in case the schema cell is not Empty Cell, the value must not be null
			if(val == null) {
				if(!ignoreErrMsg) System.err.println("Error: There is no cell value, i.e. null, to validate against schema cell at: [" + sRow + "," + sCol + "]");
				return false;
			}
		}		

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
		    	if(!ignoreErrMsg) {
		    		System.err.println("Error: Regular Expression Mismatched for schema cell: " + c);
		    		System.err.println("Expecting pattern: " + regEx + " found: " + val);
		    	}
		    	return false;			
		    }
		}
		
		return true;
	}
	
	public String getTableName() {
		return getProperty(METAPROP_TBLNAME);
	}
	
	/**
	 * Set the '@talbeName' property of this schema table while also update its register 
	 * inside hashmap collection of its parent schema. Therefore, it's required that the
	 * table is already initialized with its parent schema. 
	 * 
	 * Remarks: 
	 * 
	 * 1. Default table name cannot be changed. 
	 * Any attempt to do so will cause an error to be thrown.
	 * 
	 * 2. If parent schema has not yet bind with the table, it will be done so in this method.
	 * 
	 * @param name
	 */
	public void setTableName(String name) {	
		if(parent == null) throw new RuntimeException("Parent schema was not initialized for schema table: " + this);
		String oldName = getTableName();			
		if(oldName != null) {
			if(oldName.equals(Schema.DEFAULT_TABLE_NAME)) throw new RuntimeException("The default table name cannot be changed.");
			if(parent.hasSchemaTable(oldName)) parent.removeSchemaTable(oldName);
		}
		addProperty(METAPROP_TBLNAME, name);
		parent.addSchemaTable(this);
	}
	
	/**
	 * Set the '@name' property of this schema table. If the variable name for this schema table 
	 * is registered, the register with old variable name will also be updated with new one.  
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {	
		if(parent == null) throw new RuntimeException("Parent schema was not initialized for schema table: " + this);
		String oldName = getName();			
		if(oldName != null && varMap.containsKey(oldName)) {
			varMap.remove(oldName);			
			super.setName(name);
			varMap.put(name, this);
		} else { // if it has never been set before
			super.setName(name); 			
		}		
	}

	@Override
	public SchemaTable getSchemaTable() {
		return this;
	}

	@Override
	public String getRefEx() {
		return "@table[" + getTableName() + "]";
	}

}
