package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

public class SchemaFunction extends SchemaEntity {
	
	/**
	 * IRI for schema function entity.
	 */
	public static final String CLASS_IRI = "csvx:SchemaFunction";	
	
	/**
	 * Functions belong to a parent schema.
	 */
	private final Schema parentSchema;	
	
	/**
	 * Function parameter(s).
	 */
	private List<String> params = new ArrayList<String>();	
	
	public SchemaFunction(String funcName, Schema schema) {
		assert(funcName != null) : "@func[name] must always have name, null must never be passed in.";
		properties.put(METAPROP_FUNCNAME, funcName);
		parentSchema = schema;
	}
	
	public String getName() {
		throw new RuntimeException("The variable name for template is currently not supported.");
	}
	
	public void setName(String name) {
		throw new RuntimeException("The variable name for template is currently not supported.");
	}	
	
	/**
	 * Get the name of schema function.
	 * @return String
	 */
	public String getFunctionName() {
		return properties.get(METAPROP_FUNCNAME);
	}
	
	/**
	 * Set the name of schema function.
	 * @param String name
	 */
	public void setFunctionName(String name) {
		properties.put(METAPROP_FUNCNAME, name);
	}
	
	/**
	 * Get all parameters for this function.
	 * @return
	 */
	public List<String> getParameterList() {
		return params;
	}

	/**
	 * Add all function parameters to the definition, preserving the original iterator's order. 
	 * @param params
	 */
	public void addParams(List<String> params) {
		this.params.addAll(params);
	}	
	
	public String getScript() {
		return properties.get(METAPROP_FUNC_SCRIPT);
	}
	
	public void setScript(String script) {
		properties.put(METAPROP_FUNC_SCRIPT, script);
	}

	@Override
	public Schema getParentSchema() {
		return parentSchema;
	}

	@Override
	public SchemaTable getSchemaTable() {
		return null;
	}

	@Override
	public String getRefEx() {
		return parentSchema.toString() + ".@func[" + getFunctionName() + "]";
	}

}
