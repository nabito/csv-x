package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

public class SchemaTemplate extends SchemaEntity {

	/**
	 * IRI for schema template entity.
	 */
	public static final String CLASS_IRI = "csvx:SchemaTemplate";

	private final Schema parentSchema;
	
	/**
	 * Template name.
	 */
	private String templateName;
	
	/**
	 * Template parameter(s).
	 */
	private List<String> params = new ArrayList<String>();
	
	/**
	 * The template.
	 */
	private String tmp;	
	
	public SchemaTemplate(String templateName, Schema parentSchema) {		
		this.templateName = templateName;
		this.parentSchema = parentSchema;
	}	
	
	/**
	 * Get all parameters for this template.
	 * @return
	 */
	public List<String> getParameterList() {
		return params;
	}

	/**
	 * Add all template parameters to the definition, preserving the original iterator's order. 
	 * @param params
	 */
	public void addParams(List<String> params) {
		this.params.addAll(params);
	}
	
	/**
	 * Get the content of template.
	 * @return
	 */
	public String getTemplate() {
		return tmp;
	}
	
	/**
	 * Set the content of template.
	 * @param template
	 */
	public void setTemplate(String template) {
		tmp = template;
	}
	
	/**
	 * @return the templateName
	 */
	public String getName() {
		return templateName;
	}

	/**
	 * @param templateName the templateName to set
	 */
	public void setName(String templateName) {
		this.templateName = templateName;
	}

	/**
	 * @return the parentSchema
	 */
	public Schema getParentSchema() {
		return parentSchema;
	}

	@Override
	public SchemaTable getSchemaTable() {
		return null;
	}

	@Override
	public String getRefEx() {
		return parentSchema.toString() + "@template[" + templateName + "]";
	}

}
