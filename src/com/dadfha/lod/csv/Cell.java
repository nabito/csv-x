package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dadfha.mimamo.air.Datapoint;

public class Cell {
	
	/**
	 * Relations the cell has with other cell(s). 
	 * This is regarded as a special kind of property. 
	 */
	private Map<Relation, HashSet<Cell>> relations = new HashMap<Relation, HashSet<Cell>>();	
	
	/**
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
	 * Except for some really fundamental properties to field's concept like row and column which are not up to schema's 
	 * definition and won't be changed. This slightly increases performance too.
	 * 
	 * Note: This is similar to Data Property in OWL.
	 * FIXME Should the Map<String be replaced with Map<Property --> Yes, to store its mapped object's type and so on...
	 * Therefore, this should be merged with above map where Relation is a subclass of Property??
	 * 
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	private Map<String, String> plainProperties;
	
	/**
	 * 
	 * In addition to fundamental properties, row and column, below are other field property definitions:
	 * 
	 * 1. String name
	 * 
	 * Name of the datapoint which must be unique within the scope of CSV schema.
	 * This must be xml QNAME so it can suffix an IRI to create UUID for each cell.
	 * 
	 * 2. String label
	 * 
	 * Human readable lable of the field. This is an extra attribute and never is a content of the field.
	 * 
	 * 3. Class<? extends Field> type
	 * 
	 * Type of the field as defined by our CSV schema. This information is reflected in Java type system.
	 * 
	 * 		Field is a field containing a value.
	 * 		HeaderField is a subclass of Field representing field which role is to be header for data.
	 * 		EmptyField 	as its name states, it holds no data thus empty. 
	 * 					It'll be validated against [^\\S\r\n]*? regEx for whitespace characters except newline.
	 * 
	 * 4. String regEx
	 * 
	 * 5. datatype
	 * 
	 * 6. comment
	 * 
	 * 7. value
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
	 * The unique id for this cell both in local and world-wide scope.
	 */
	private String uuid;
	
	/**
	 * Copy constructor.
	 * @param f
	 */
	public Cell(Cell f) {
		row = f.row;
		col = f.col;
		properties = new HashMap<String, Object>(f.getProperties());
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
		plainProperties = properties;
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

	public String getName() {
		return (String) properties.get("name");
	}

	public void setName(String name) {
		properties.put("name", name);
	}

	public Class<? extends Cell> getType() {
		return this.getClass();
	}

	public String getLabel() {
		return (String) properties.get("label");
	}

	public void setLabel(String label) {
		properties.put("label", label);
	}
	
	public String getRegEx() {
		return (String) properties.get("regEx");
	}
	
	public void setRegEx(String regEx) {
		properties.put("regEx", regEx);
	}
	
	public String getDataType() {
		return (String) properties.get("dataType");
	}
	
	public void setDataType(String dataType) {
		properties.put("dataType", dataType);
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
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	public void addProperty(String key, Object val) {
		properties.put(key, val);
	}
	
	/**
	 * Cloning as new type via deep copy and return ref to new Obj
	 * @param type
	 * @return
	 */
	public Cell setType(Class<? extends Cell> type) {		
		if(type == HeaderField.class) {
			return new HeaderField(this);
		} else if(type == EmptyField.class) {
			return new EmptyField(this);
		} else if(type == Cell.class) {
			return this;
		} else {
			throw new RuntimeException("Conversion attemp for unknown field type.");
		}
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
		
		if(row != c.row || col != c.col || (uuid.compareTo(c.uuid) != 0)) return false; 

		return true;
	}

	public Map<Relation, HashSet<Cell>> getRelations() {
		return relations;
	}

	public void setRelations(Map<Relation, HashSet<Cell>> relations) {
		this.relations = relations;
	}
	
	

}
