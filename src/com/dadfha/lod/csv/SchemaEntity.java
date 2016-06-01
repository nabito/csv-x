package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public abstract class SchemaEntity {
	
	/**
	 * The name given for a schema entity. Must be unique within the scope of schema.
	 * If explicitly specified in schema file, it'll also be used as variable name for the entity.
	 * 
	 * This must be XML QNAME so it can suffix an IRI to be an addressable resource 
	 * in Linked Data according to RDF model.
	 */
	String name; 
	
	/**
	 * The value defined by '@value' property for each schema entity. 
	 * By default, this is the property to store parsed CSV value for each cell.
	 */
	String value;

	/**
	 * Extra/user-defined properties.
	 * HashMap storing mapping between property's name and its value.
	 * 
	 * Storing extra field's properties in a collection rather than class's attributes has many merits.
	 * 
	 * It avoids code change whenever there is a new property introduced, either from schema's syntax
	 * update or user's defined annotation. By default, all these new properties should automatically 
	 * be reflected in each datapoint when a schema is applied to a CSV.
	 * 
	 * Imagine doesn't have to do:
	 * 
	 *     datapoint.setNewProp(section.getNewPropAtField(row, col));
	 *     
	 * Which involves creating new attributes, methods in both Field, Schema, and Datapoint classes! 
	 * 
	 * Except for meta-properties to explain cell's concept like row, column, id, and etc. which are not up to 
	 * application domain and won't be changed. 
	 * 
	 */	
	 Map<String, String> properties = new HashMap<String, String>();	
	
	/**
	 * Get parent schema of this schema entity.
	 * @return Schema.
	 */
	public abstract Schema getParentSchema();

	/**
	 * Get schema table that the entity is contained in.
	 * Just returning itself if the entity is of type SchemaTable. 
	 * @return SchemaTable.
	 */
	public abstract SchemaTable getSchemaTable();
	
	/**
	 * Schema Entity Reference Expression (SERE) of this schema entity in full(expanded) form.
	 * 	E.g. \@table[name].@cell[x,y]
	 * @return String
	 */
	public abstract String getRefEx();
	
	/**
	 * Get literal value of a property with the given name. 
	 * @param propertyName
	 * @return String holding literal value of the property or null if there is none.
	 */
	public String getProperty(String propertyName) {
		String retVal = null;
		switch(propertyName.toLowerCase()) {
		case "name":
			retVal = name;
			break;
		case "value":
			retVal = value;
			break;
		default:
			retVal = properties.get(propertyName);
			break;
		}
		return retVal;		
	}
	
	/**
	 * Get the whole extra/user-defined property-value map collection.
	 * @return
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
	
	/**
	 * Add a property name-value map.
	 * Already existing key will be overwritten.
	 * @param prop
	 * @param val
	 */
	public void addProperty(String prop, String val) {
		properties.put(prop, val);
	}
	
	/**
	 * Add properties to the schema entity. 
	 * Any existing properties with the same name will be overwritten. 
	 * @param properties
	 */
	public void addProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}	
	
	/**
	 * Get name of this schema entity.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
	/**
	 * Get value of this schema entity.
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Set value of this schema entity.
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Give SERE output to identify itself.
	 * @return String
	 */
	public String toString() {
		return getRefEx();
	}

}
