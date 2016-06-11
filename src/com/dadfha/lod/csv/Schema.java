package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A CSV-X schema are metadata describing unique syntactic, structural, contextual, and semantic information 
 * for contents described in a CSV file. A data parsed w.r.t. a schema is regarded as a dataset. 
 * A CSV file may contain more than one dataset of the same or different schema.   
 * 
 * There can only be one CSV-X file per one CSV file, but a CSV-X may have more than one pattern (schema table).
 * By giving each table a name via '@name' meta property one can give parser a hint through CSV comment annotation 
 * to reduce trial-n-errors, hence increase parsing performance.
 *     
 * @author Wirawit
 */
public class Schema {
	
	/**
	 * Any schema entity besides schema table declared in global scope, i.e. is not under any table schema entity, 
	 * will be assigned to default table. 
	 * 
	 * IMP In the next version, it may override all schema entities under a named table with the same signature, 
	 * namely:
	 * 
	 * @cell[signature]
	 * @row[signature]
	 * @property[signature] or @property with @name = "signature".
	 * 
	 * This feature may, in the future, help reducing effort in describing schema table sharing a portion with 
	 * common schema definition.  
	 *  
	 * At current version (1.0) the semantic of schema entities inside default table is up to user where default
	 * table is regarded as just another table with default name. One recommend pattern is to use default table 
	 * to hold common schema entity independent of CSV contents, like Schema Property.
	 *  
	 */
	public static final String DEFAULT_TABLE_NAME = "@defaultTable";
	
	/**
	 * ID of the schema with the same definition as in JSON-LD.
	 */
//	private String id;
	
	/**
	 * Map between table name and schema table.
	 * 
	 * Since a schema table always has a name, this also serve as the variable registry 
	 * for schema table inside the schema.
	 */
	private Map<String, SchemaTable> sTables = new HashMap<String, SchemaTable>();
	
	/**
	 * Associate the dataset with an RDF-based schema via context as in JSON-LD
	 */
	//private List<Object> context; // IMP may use JSON-LD Java object! to map with LinkedData
	
	/**
	 * List of target CSVs.
	 */
	private List<String> targetCsvs = new ArrayList<String>();
	
	/**
	 * Schema properties (including user-defined). 
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();	
	
	/**
	 * This is for future version of CSV-X to support user-defined processing function
	 * as per property for each schema entity.
	 * 
	 * The schema keeps the collection as map between SERE and Function class.
	 */
	private Map<String, Function<String, Object>> userFuncs = new HashMap<String, Function<String, Object>>();
	
	/**
	 * Create data schema to hold actual data table(s).
	 * Every attributes in original schema will be copied to this new data schema 
	 * except for schema table which is expected to be expanded from original schema blueprint.    
	 * @param s
	 * @return Schema
	 */
	public static Schema createDataObject(Schema s) {
		Schema newSchema = new Schema();
		newSchema.properties.putAll(s.properties);
		newSchema.targetCsvs.addAll(s.targetCsvs);
		newSchema.userFuncs.putAll(s.userFuncs);
		return newSchema;
	}	

	SchemaCell getCell(int row, int col, SchemaTable table) {
		if(table == null) table = sTables.get("default");
		return table.getCell(row, col);
	}
	
	public Object getProperty(String propName) {
		return properties.get(propName);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	public void addProperty(String key, Object val) {
		properties.put(key, val);
	}
	
	public void replaceProperty(String key, Object val) {
		properties.replace(key, val);
	}

	public boolean isRepeatingRow(int row, SchemaTable table) {
		return table.getRow(row).isRepeat();
	}
	
	/**
	 * Get target CSV(s)
	 * @return list of target CSV(s) or null if none defined.
	 */
	public List<String> getTargetCsvs() {
		return targetCsvs;
	}
	
	public boolean addTargetCsv(String csvId) {
		return targetCsvs.add(csvId);
	}
	
	/**
	 * Get user-defined function for a property of a schema entity. 
	 * @param se
	 * @param propName
	 * @return Function or null is there is no function defined for the property.
	 */
	public Function<String, Object> getUserPropHandlingFn(SchemaEntity se, String propName) {
		return userFuncs.get(se.getRefEx() + "." + propName);
		// IMP introduce how to defined user-function in schema
	}	
	
	/**
	 * Check if this schema contains a schema table with the name.
	 * @param name
	 * @return true or false.
	 */
	public boolean hasSchemaTable(String name) {
		return sTables.containsKey(name);
	}
	
	/**
	 * Get schema table by its name.
	 * @param name
	 * @return SchemaTable of the input name or null if table with the name does not exist.
	 */
	public SchemaTable getSchemaTable(String name) {
		return sTables.get(name);
	}
	
	/**
	 * Get the map collection between table's name and schema table object.
	 * @return Map<String, SchemaTable>
	 */
	public Map<String, SchemaTable> getSchemaTables() {
		return sTables;
	}
	
	/**
	 * Get default schema table. One will be created if it hasn't been done yet.
	 * @return SchemaTable
	 */
	public SchemaTable getDefaultTable() {
		SchemaTable sTable;
		if((sTable = sTables.get(DEFAULT_TABLE_NAME)) == null) sTable = new SchemaTable(DEFAULT_TABLE_NAME, this);
		return sTable;
	}
	
	/**
	 * Add input schema table to the map. 
	 * Replacing existing schema table with the same name.  
	 * @param table
	 */
	public void addSchemaTable(SchemaTable table) {
		sTables.put(table.getName(), table);
	}
	
	/**
	 * Remove a register of schema table from this schema. 
	 * @param tableName
	 */
	public void removeSchemaTable(String tableName) {
		sTables.remove(tableName);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Schema[" + properties.get("@id") + "]";
	}
	
}
