package com.dadfha.mimamo.air;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.dadfha.lod.csv.Cell;
import com.dadfha.lod.csv.Relation;

/**
 * Each Datapoint instance hold at least a unique property-value pair that distinguishes it from other Datapoint.
 * 
 * Let's transfer every property to the mapped object including meta-property (@) because in the 
 * future there may be some data model that want to include such schema information in there model too (says, field's 
 * header information for a tabular oriented data model).
 * 
 * @author Wirawit
 *
 */
public class Datapoint {
	
	/**
	 * TODO rethink if this is necessary...
	 * Relations the cell has with other cell(s). 
	 * This is regarded as a special kind of property. 
	 */
	private Map<Relation, HashSet<Cell>> relations = new HashMap<Relation, HashSet<Cell>>();	
	
	
	// IMP move all these attributes into hashmap for consistency?	
	
	/**
	 * handle name used to refer to this datapoint, for CSV this is "row,col" in the grid 
	 * (not character space wise in the file). 
	 * For RDF, it's the entity's URI. 
	 * The point is to be able to uniquely identify this datapoint out of original data source.
	 */
	private String id; 
	
	/**
	 * human readable title of this datapoint. 
	 * This property is mapped to rdfs:label for RDF.
	 */
	private String title;
	
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
		return id.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof Datapoint)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		Datapoint dp = (Datapoint) o;
		
		if(!(id == dp.getId() && title == dp.getTitle() && datatype == dp.getDatatype() && value.equals(dp.getValue()))) return false;
		
		return properties.equals(dp.getProperties());		
	}


}
