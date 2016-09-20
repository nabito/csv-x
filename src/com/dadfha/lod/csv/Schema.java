package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	 * CSV-X namespace.
	 */
	public static final String NS = "http://dadfha.com/ont/csvx";
	
	/**
	 * CSV-X default namespace prefix.
	 */
	public static final String NS_PREFIX = "csvx";
	
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
	 * The base IRI (or other addressing scheme) for all schema entity.
	 * Any \@id in each schema entity will override the base. Therefore, prefix must be used 
	 * to define unique local name over a base.  
	 * 
	 * If \@base not defined in a schema, it'll be default to empty string. 
	 * 
	 */
	public static final String METAPROP_BASE = "@base";
	
	/**
	 * Map between table name and schema table.
	 * 
	 * The order in which the table is declared is significant and preserved, 
	 * as the matching of multiple tables inside a csv should be tried in the order 
	 * its declared, one after the other in succession, in order to reduce number 
	 * of trials and errors. Says, if schema table 1 and 2 are defined, at the time
	 * of csv matching, the once the first table done matching, the parser will try 
	 * using schema table 2 to match next csv content. 
	 * 
	 * However, at current implementation, SchemaProcessor.parseCsvWithSchema() will retry all
	 * schema tables from the beginning after a successful parse of table rather than continuing
	 * to the next schema table because there's a chance that the next csv content could be 
	 * that of the earlier table.
	 *     
	 * There is also no simple way of reordering schema tables collection while iterating without 
	 * modifying the original collection. The only way is to make a copy of the whole collection
	 * and re-arrange its order as we parse. If the order of schema table in actual csv content
	 * is so random, this scheme would create insignificant performance gain. Thus, I decide 
	 * to keep it this way.
	 * 
	 * Again, there may be an efficient way to implement this using Ring Buffer collection which
	 * has the nature of looping through all elements in order but a special counter must be 
	 * introduced to signify the begin and end of loop as per parsing trials. 
	 * This feature is deferred to the future version.
	 * 
	 * Since a schema table always has a name, this also serve as the registry 
	 * for schema table inside the schema. IMP However, referencing table inside the table 
	 * itself is done via variable name defined using '@name'. This is to support cross-
	 * referencing between table "instance" in future version, where table name only 
	 * designates a table "prototype" but each actual instance MUST has a uniquely addressable
	 * name. This could be achieved using dynamic variable naming via context var(s). 
	 */
	private Map<String, SchemaTable> sTables = new LinkedHashMap<String, SchemaTable>();
	
	/**
	 * Associate the dataset with an RDF-based schema via context as in JSON-LD
	 */
	//private List<Object> context; // IMP may use JSON-LD Java object! to map with LinkedData
	
	/**
	 * List of target CSVs.
	 */
	private List<String> targetCsvs = new ArrayList<String>();
	
	/**
	 * Properties of the schema (including user-defined). 
	 * This is not the same as SchemaProperty which is the data property. 
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
	 * RDF template collection.
	 */
	private Map<String, RdfTemplate> rdfTemplates = new HashMap<String,RdfTemplate>();
	
	/**
	 * Set base (IRI) for the whole schema. Existing value will be overwritten.
	 * @param baseIri
	 */
	public void setBase(String base) {
		properties.put(METAPROP_BASE, base);
	}
	
	/**
	 * Get base (IRI) for the schema.
	 * @return String or null if none is defined.
	 */
	public String getBase() {
		return (String) properties.get(METAPROP_BASE);
	}
	
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
		newSchema.rdfTemplates.putAll(s.rdfTemplates);
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
		sTables.put(table.getTableName(), table);
	}
	
	/**
	 * Remove a register of schema table from this schema. 
	 * @param tableName
	 */
	public void removeSchemaTable(String tableName) {
		sTables.remove(tableName);
	}
	
	/**
	 * Get an RDF template by name.
	 * @param name
	 * @return RdfTemplate or null if no template with the name exists.
	 */
	public RdfTemplate getRdfTemplate(String name) {
		return (rdfTemplates.containsKey(name))? rdfTemplates.get(name) : null;
	}
	
	/**
	 * Add an RDF template to the schema. 
	 * Already existing template with the same name will be overwritten.
	 * @param template
	 */
	public void addRdfTemplate(RdfTemplate template) {
		rdfTemplates.put(template.getName(), template);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Schema[" + properties.get("@id") + "]";
	}
	
	/**
	 * Serialize into RDF Turtle format.
	 * @return
	 */
	public String serializeTtl() {
		StringBuilder ttl = new StringBuilder();
		for(Map.Entry<String, SchemaTable> tableE : sTables.entrySet()) {
			String tableName = tableE.getKey();
			SchemaTable sTable = tableE.getValue();
			
			ttl.append(sTable.getTtl());
			
			// for every row
			for(Map.Entry<Integer, SchemaRow> rowE : sTable.getSchemaRows().entrySet()) {
				Integer rowNum = rowE.getKey();
				SchemaRow sRow = rowE.getValue();
				
				ttl.append(sRow.getTtl());
				
				// for every cell
				for(Map.Entry<Integer, SchemaCell> cellE : sRow.getSchemaCells().entrySet()) {
					Integer colNum = cellE.getKey();
					SchemaCell sCell = cellE.getValue();
					
					ttl.append(sCell.getTtl());
					
				}
			}
			
			// for every schema property
			for(Map.Entry<String, SchemaProperty> propE : sTable.getSchemaProperties().entrySet()) {
				String propName = propE.getKey();
				SchemaProperty sProp = propE.getValue();
				
				ttl.append(sProp.getTtl());
				
				// TODO add statememt for which table it's belonged to for Schema Property and Schema Data
				// TODO resolve {var} when serialize too (See CSV dump)
				// TODO recheck handling of @base and prefix properly
				
			}
			
			// for every schema data
			for(Map.Entry<String, SchemaData> dataE : sTable.getSchemaDataMap().entrySet()) {
				String dataName = dataE.getKey();
				SchemaData sData = dataE.getValue();
				
				ttl.append(sData.getTtl());
			}
			
		}
		return ttl.toString();
	}
	
}
