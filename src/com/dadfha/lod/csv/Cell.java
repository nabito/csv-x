package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dadfha.mimamo.air.Datapoint;

public class Cell implements SchemaEntity {	
	
	/**
	 * 
	 * FIXME any properties inside { "key" : "val" } map in JSON schema file should all be stored in this map 
	 * for lean and consistency in processing a chunk of properties.
	 * (Or else coder will have to remember what properties should be excluded from this map).
	 * 
	 * HashMap storing mapping between property's name and its value.
	 * 
	 * Storing field's properties in a collection rather than class's attributes has many merits.
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
	 * IMP Most but not all properties will eventually be applied to each datapoint (schema's field<->datapoint).
	 * Therefore, we should maintain a mapping table of what properties get/not get transferred to datapoint when 
	 * a CSV is parsed against a schema.
	 * 
	 * Rather than pertaining those properties that won't be included in a datapoint as attributes and avoid creating 
	 * above mapping, it's better to keep everything in generic fashion (all properties in a collection) because in the 
	 * future there may be some data model that want to include such schema information in there model too (says, field's 
	 * header information for a tabular oriented data model). 
	 * 
	 * Except for some really fundamental properties to cell's concept like row, column, and uuid which are not up to 
	 * schema's definition and won't be changed. This slightly increases performance too.
	 * 
	 */
	private Map<String, String> properties = new HashMap<String, String>();
	
	/**
	 * 
	 * In addition to fundamental properties, row and column, below are other cell property definitions:
	 * 
	 * 1. String name
	 * 
	 * Name of the datapoint which must be unique within the scope of CSV schema.
	 * This must be xml QNAME so it can suffix an IRI to create UUID for each cell.
	 * 
	 * 2. String label
	 * 
	 * Human readable lable of the cell. This is an extra attribute and never is a content of the cell.
	 * 
	 * 3. Class<? extends Cell> type
	 * 
	 * Type of the cell as defined by our CSV schema. This information is reflected in Java type system.
	 * 
	 * 		Cell is a cell containing a value.
	 * 		EmptyCell 	as its name states, it holds no data thus empty. 
	 * 					It'll be validated against [^\\S\r\n]*? regEx for whitespace characters except newline.
	 * 
	 * 
	 */
	
	/**
	 * relative row in a schema.
	 */
	private int row;
	
	/**
	 * relative column in a schema.
	 */
	private int col;
	
	/**
	 * The unique id for this cell within a processing context. Can be UUID for world-wide scope.
	 */
	private String id;
	
	/**
	 * The data type in which the cell will be mapped to.
	 */
	private String type;
	
	private String regEx;
	
	private String datatype;
	
	private String lang;
	
	private String value;
	
	/**
	 * Copy constructor.
	 * @param c
	 */
	public Cell(Cell c) {
		row = c.row;
		col = c.col;
		// Collection default copy constructor is deepcopy as long as the object type inside is immutable.
		properties = new HashMap<String, String>(c.getProperties()); 
	}
	
	/**
	 * Constructor.
	 * @param row
	 * @param col
	 */
	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public Cell(int row, int col, Map<String, String> properties) {
		this(row, col);
		this.properties.putAll(properties);
	}
	
	/**
	 * Constructor.
	 * @param row
	 * @param col
	 * @param regEx
	 */
	public Cell(int row, int col, String regEx) {
		this.row = row;
		this.col = col;
		setRegEx(regEx);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setType(String type) {
		this.type = type;
	}	

	public String getType() {
		return type;
	}
	
	public String getRegEx() {
		return regEx;
	}
	
	public void setRegEx(String regEx) {
		this.regEx = regEx;
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}	

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}	
	
	/**
	 * Add property in key-value map.
	 * Already existing key will be overwritten.
	 * @param key
	 * @param val
	 */
	public void addProperty(String key, String val) {
		properties.put(key, val);
	}
	
	/**
	 * Add properties to the Cell. 
	 * Any existing properties with the same name will be overwritten. 
	 * @param properties
	 */
	public void addProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}	
	
	/**
	 * Merge cells content. Any duplicate properties will be overwritten.
	 * @param cell
	 */
	public void merge(Cell cell) {
		row = cell.row;
		col = cell.col;
		id = cell.id;
		properties.putAll(cell.properties);
	}
	
	/**
	 * Hashcode = row's lower 16bits from 17th position and row's higher 16bits entering left-to-right 
	 * LSB first from 16th position (position count from 1, LSB from left-side).
	 * Then XOR everything with col.
	 * 
	 * This biased bits arrangement is designed to minimize hash value collision given high correlation 
	 * between row and col value, each are better placed at different bit position.
	 * Moreover, it's more common to have higher number of row than col, thus row's bits greater than 16th 
	 * are padded to the right from center of bits string.
	 * 
	 * OPT may be compare performance (e.g. collision %) of this hash vs. eclipse auto-generated
	 */
	public int hashCode() {		
	    int rowTopBits = row &= 0xFF00; 
	    int distance = Math.abs(16 - Integer.numberOfLeadingZeros(rowTopBits));
	    int hash = ( (rowTopBits >>> distance) | (row << 16) ) ^ col;
	    return hash;
	}
	
	/**
	 * A pair of field is considered equal if and only if:
	 * 1. It represents the same field's coordinate [row, col] with the same UUID.
	 * 2. It is exactly the same Java object for 1.
	 * 3. It has the same hash code.
	 * 4. It is of the same type 'Cell'.
	 * 
	 * Note: The schema properties of both cells may not be the same which regarded as conflict in properties 
	 * but still considered equal objects.    
	 * 
	 */
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof Cell)) return false;
		
		// JVM contract: equal object must has same hashcode. The true is NOT vice versa.
		if(hashCode() != o.hashCode()) return false;
		
		Cell c = (Cell) o;
		
		if(row != c.row || col != c.col || (id.compareTo(c.id) != 0)) return false; 

		return true;
	}

}
