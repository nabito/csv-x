package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.dadfha.lod.LodHelper;

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
	 * User-defined namespace prefixes.
	 */
	private Map<String, String> prefixes = new HashMap<String, String>();
	
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
	 * Schema template collection.
	 */
	private Map<String, SchemaTemplate> sTemplates = new HashMap<String,SchemaTemplate>();
	
	/**
	 * Schema function collection.
	 */
	private Map<String, SchemaFunction> sFuncs = new HashMap<String,SchemaFunction>();	
	
	/**
	 * Map between ?varname in template(s) and UUID for this Schema.
	 */
	private Map<String, UUID> ttlVars = new HashMap<String, UUID>();
	
	/**
	 * Remember lastly generated UUID to check for possible duplicate generation.
	 */
	private UUID lastUUID;
	
	/**
	 * Set base (IRI) for the whole schema. Existing value will be overwritten.
	 * @param baseIri
	 */
	public void setBase(String base) {
		properties.put(SchemaProcessor.METAPROP_BASE, base);
	}
	
	/**
	 * Get base (IRI) for the schema.
	 * @return String or empty String "" if base is never defined.
	 */
	public String getBase() {
		String base = (String) properties.get(SchemaProcessor.METAPROP_BASE);
		if(base == null) base = "";
		return base;
	}
	
	/**
	 * Get the collection for all namespace prefixes.
	 * @return
	 */
	public Map<String, String> getNsPrefixes() {
		return prefixes;
	}
	
	/**
	 * Add a collection of namespace prefixes.
	 * Note that prefix value can either be an IRI or relative path.
	 * @param prefixes
	 */
	public void addNsPrefixes(Map<String, String> prefixes) {
		this.prefixes.putAll(prefixes);
	}
	
	/**
	 * Create data schema to hold actual data table(s).
	 * Every attributes in original schema will be copied to this new data schema 
	 * except for schema table which is expected to be expanded from original schema blueprint.
	 * 
	 * IMP consider creating auto-cloning method in the future, so that whenever we add new
	 * attribute, we'd not forget to make manual copy here. 
	 *     
	 * @param s
	 * @return Schema
	 */
	public static Schema createDataObject(Schema s) {
		Schema newSchema = new Schema();
		newSchema.properties.putAll(s.properties);
		newSchema.targetCsvs.addAll(s.targetCsvs);
		newSchema.userFuncs.putAll(s.userFuncs);
		newSchema.sTemplates.putAll(s.sTemplates);
		newSchema.prefixes.putAll(s.prefixes);
		newSchema.sFuncs.putAll(s.sFuncs);
		return newSchema;
	}	

	SchemaCell getCell(int row, int col, SchemaTable table) {
		if(table == null) table = sTables.get("default");
		return table.getCell(row, col);
	}
	
	/**
	 * Get property object associated with the property name.
	 * @param propName
	 * @return Object associated with the property name or null if it's not yet registered.
	 */
	public Object getProperty(String propName) {
		return properties.get(propName);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	/**
	 * Add a property-Object pair to schema, overwriting existing property with the same name. 
	 * @param key
	 * @param val
	 */
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
	 * Get default schema table. One will be created and registered if it hasn't been done yet.
	 * @return SchemaTable
	 */
	public SchemaTable getDefaultTable() {
		SchemaTable sTable;
		if((sTable = sTables.get(DEFAULT_TABLE_NAME)) == null) {
			sTable = new SchemaTable(DEFAULT_TABLE_NAME, this);
			sTables.put(DEFAULT_TABLE_NAME, sTable);
		}
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
	 * Get a template by name.
	 * @param name
	 * @return SchemaTemplate or null if no template with the name exists.
	 */
	public SchemaTemplate getTemplate(String name) {
		return (sTemplates.containsKey(name))? sTemplates.get(name) : null;
	}
	
	/**
	 * Add a schema template to the schema. 
	 * Already existing template with the same name will be overwritten.
	 * @param SchemaTemplate
	 */
	public void addTemplate(SchemaTemplate template) {
		sTemplates.put(template.getTemplateName(), template);
	}
	
	/**
	 * Get a function by name.
	 * @param name
	 * @return SchemaFunction or null if no function with the name exists.
	 */	
	public SchemaFunction getFunction(String name) {
		return (sFuncs.containsKey(name))? sFuncs.get(name) : null;
	}
	
	/**
	 * Add a schema function to the schema. 
	 * Already existing function with the same name will be overwritten.
	 * @param SchemaFunction
	 */	
	public void addFunction(SchemaFunction func) {
		sFuncs.put(func.getFunctionName(), func);
	}
	
	/**
	 * Add template's variable name within this Schema scope (global). 
	 * @param varName
	 * @return newly generated UID of varName or existing one if the name has been added before. 
	 */
	public UUID addGlobalTemplateVar(String varName) {
		if(ttlVars.containsKey(varName)) { // get previously generated UID
			return ttlVars.get(varName);
		} else { // or generate new UID
			UUID id = generateSchemaUID();
			ttlVars.put(varName, id);
			return id; 
		}
	}
	
	/**
	 * Generate UID within the processing of a schema.
	 * Even though the chance is extremely low, we make sure that there's no duplicate UID 
	 * by comparing with previously generated UID.
	 * @return UUID
	 */
	public UUID generateSchemaUID() {
		UUID id;
		do {
			id = UUID.randomUUID();								
		} while(id.equals(lastUUID));		
		return id;
	}
	
	/**
	 * Get template variable name, e.g. ?x, its associated UUID. 
	 * @param varName
	 * @return UUID or null if there is no such variable name registered.
	 */
	public UUID getTemplateVarUUID(String varName) {
		return ttlVars.get(varName);
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
	 * @throws Exception 
	 */
	public String serializeTtl() throws Exception {
		StringBuilder ttl = new StringBuilder();
		
		// declare base & prefixes
		String base = getBase();
		if(!base.isEmpty()) {
			assert(LodHelper.isURL(base)) : "@base must be in the IRI form.";
			ttl.append("BASE <" + base + ">" + System.lineSeparator());
		}
		for(Map.Entry<String, String> e : getNsPrefixes().entrySet()) {
			String prefixName = e.getKey();
			String prefixIri = e.getValue();
			ttl.append("PREFIX " + prefixName + ": <" + prefixIri + ">" + System.lineSeparator());
		}
		
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
			}
			
			// for every schema data
			for(Map.Entry<String, SchemaData> dataE : sTable.getSchemaDataMap().entrySet()) {
				String dataName = dataE.getKey();
				SchemaData sData = dataE.getValue();
				ttl.append(sData.getTtl());
			}
			
		} // end for each schema table
		
		// TODO in v1.x also serialize each @template & @function
		
		return ttl.toString();
	}
	
}
