package com.dadfha.mimamo.air;

import java.util.HashMap;

/**
 * Each Datapoint instance hold at least a unique property-value pair that distinguishes it from other Datapoint.
 * @author Wirawit
 *
 */
public class Datapoint {
	
	// IMP move all these attributes into hashmap for consistency?
	// TODO having RDF as backend for relation between datapoint i.e. data structure 
	
	/**
	 * handle name used to refer to this datapoint, for CSV this is "row,col" in the grid 
	 * (not character space wise in the file). 
	 * For RDF, it's the entity's URI. 
	 * The point is to be able to uniquely identify this datapoint out of original data source.
	 */
	private String id; 
	
	/**
	 * human readable label of this datapoint. rdfs:label for RDF.
	 * For CSV, field's name can be used, if it make sense to human or it has to be annotated in a comment somewhere.
	 */
	private String label;
	
	/**
	 * simple types from XML schema datatype 1.1. 
	 * This practice follows RDF 1.1 standard.
	 */
	private String datatype;
	
	/**
	 * the stored value of this datapoint.
	 */
	private String value;
	
	/**
	 * other properties.
	 */
	private HashMap<String, String> properties = new HashMap<String, String>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}	
	public HashMap<String, String> getProperties() {
		return properties;
	}
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
	
	public String toString() {
		//return (value != null)? value.toString() + "(" + id + ")" : "";
		return (value != null)? value.toString() : "";
	}
	
	public int hashCode() {
		// TODO find a better hashcode
		return 0;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof Datapoint)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		Datapoint dp = (Datapoint) o;
		
		if(!(id == dp.getId() && label == dp.getLabel() && datatype == dp.getDatatype() && value.equals(dp.getValue()))) return false;
		
		return properties.equals(dp.getProperties());		
	}


}
