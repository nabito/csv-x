package com.dadfha.mimamo.air;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.dadfha.lod.csv.Relation;

/**
 * The idea of "Datapoint" is that it must represent exactly one nominal value which means one thing 
 * and can be referred through 'hasValue' property making it possible to utilize data without 
 * knowing application specific name for the property.
 * 
 * There can be other peripheral/context properties describing the datapoint, which cannot avoid 
 * using domain specific name. In this case, the extensible property definition can provide useful hint 
 * for software agent to recognize its structure and meaning, a.k.a. knowing how to consume it, 
 * using URI, title, description, or any other form of semantic representation.
 * 
 * TODO This dataset and datapoint model leverage conventions and patterns in LinkedDataPlatform specification 
 * to represent a list of data...2B Continue..
 * 
 * As a default, let's transfer every property to the mapped object including meta-property (@) because in the 
 * future there may be some data model that want to include such schema information in there model too (says, regEx
 * to validate any future change in value).
 * 
 * For some property, 1-1 mapping with field is not desired, rather a special treatment is needed, therefore, mapping
 * with "transformation method" using lambda expression is needed. 
 * 
 * 		Ex. '@datatype' of cell's value should be converted to just a datatype of property(ies) 
 * 			referring to the value. Without data conversion value will be stored as String.
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
	private Map<Relation, HashSet<Datapoint>> relations = new HashMap<Relation, HashSet<Datapoint>>();	
	
	
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
	private Map<String, String> properties = new HashMap<String, String>();
	
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
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
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
