package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

/**
 * IMP This is the same concept as Object Property rather than Data Property as now used in the schema.
 * However, at the moment, there is no need to propose a powerful generic data model, which may invite a lot of 
 * criticisms, rather than to just focusing on a data model that can describe non-uniform CSV for automated 
 * parsing and utilization.
 * 
 * Relation is a kind of Property that a cell has with the other cell. It has direction thus is non-symmetrical.
 * In the next version it will merge the concept of Data Property (@property) with Object Property (Relation),
 * creating the super "Property" which may link both literal value and object. 
 * 
 * @author Wirawit
 */
public class Relation {
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();	
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	public void addProperty(String key, Object val) {
		properties.put(key, val);
	}

}
