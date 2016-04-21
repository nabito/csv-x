package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

/**
 * Relation is a kind of Property that a cell has with the other cell. It has direction thus is non-symmetrical.
 * TODO check if this is really necessary...
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
	
	public int hashCode() {
		// TODO find a better hashcode
		return 0;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof Relation)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		Relation r = (Relation) o;
		
		return properties.equals(r.getProperties());		
	}	

}
