package com.dadfha.lod.csv;

/**
 * The purpose of schema data is to address arbitrary data modeling needs.
 * This is considered an important feature in the completion of data transformation and mapping 
 * since some target data model may not be able to map directly with tabular-view-based CSV schema model.
 * 
 * IMP Let this be the feature of the next version.
 * 
 * @author Wirawit
 *
 */
public class SchemaData extends SchemaEntity {
	
	/**
	 * Class IRI for schema data.
	 */
	public static final String CLASS_IRI = Schema.NS_PREFIX + ":SchemaData";	
	
	SchemaTable parentTable;
	
	public SchemaData(String dataName, SchemaTable parentTable) {
		assert(dataName != null) : "@data[name] must always have name, null must never be passed in.";
		properties.put(METAPROP_DATANAME, dataName);
		this.parentTable = parentTable;
	}
	
	public static SchemaData createDataObject(SchemaData sData, SchemaTable parentDataTable) {
		SchemaData dData = new SchemaData(sData.getDataName(), parentDataTable); 
		dData.properties.putAll(sData.properties);
		return dData;
	}		
	
	public String getDataName() {
		return getProperty(METAPROP_DATANAME);
	}
	
	/**
	 * Set the '@dataName' property of this schema data while also update its register 
	 * inside hashmap collection of its parent schema table.
	 * 
	 * Note that if its parent table has not yet bind with this schema data, it'll be 
	 * done so by this method.
	 * 
	 * @param name
	 */	
	public void setDataName(String name) {
		if(parentTable == null) throw new RuntimeException("Parent table was not initialized for schema data: " + this);
		String oldName = getDataName();
		if(oldName != null && parentTable.hasSchemaData(oldName)) {			
			parentTable.removeSchemaData(oldName);		
		} 
		addProperty(METAPROP_DATANAME, name);
		parentTable.addSchemaData(this);
	}

	/**
	 * Set the '@name' property of this schema data and, if available, update its variable register 
	 * inside hashmap collection of its parent schema table.
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {
		if(parentTable == null) throw new RuntimeException("Parent table was not initialized for schema data: " + this);
		String oldName = getName();		
		if(oldName != null && parentTable.hasVar(oldName)) {
			parentTable.removeVar(oldName);			
			super.setName(name);
			parentTable.addVar(name, this);
		} else { // if it has never been set before
			super.setName(name);
		}
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
		return parentTable.getRefEx() + ".@data[" + getDataName() + "]";
	}

}
