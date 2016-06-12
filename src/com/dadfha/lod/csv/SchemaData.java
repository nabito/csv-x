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
	
	SchemaTable parentTable;
	
	public SchemaData(String dataName, SchemaTable parentTable) {
		assert(dataName != null) : "@data[name] must always have name, null must never be passed in.";
		properties.put(METAPROP_DATANAME, dataName);
		this.parentTable = parentTable;
	}
	
	public void setDataName(String name) {
		// TODO at least we can complete this in advance before we forget
	}
	
	@Override
	public void setName(String name) {
		// TODO at least we can complete this in advance before we forget
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
		return "@data[" + getName() + "]";
	}

}
