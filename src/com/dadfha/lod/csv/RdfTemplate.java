package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

public class RdfTemplate {
	
	private final Schema parentSchema;
	
	private String templateName;
	
	private List<String> params = new ArrayList<String>();
	
	/**
	 * Description of this template.
	 */
	private String desc;
	
	/**
	 * The template.
	 */
	private String tmp;
	
	public RdfTemplate(String templateName, Schema parentSchema) {
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
	
	public String getDescription() {
		return desc;
	}
	
	public void setDescription(String description) {
		desc = description;
	}
	
	public String getTemplate() {
		return tmp;
	}
	
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

}
