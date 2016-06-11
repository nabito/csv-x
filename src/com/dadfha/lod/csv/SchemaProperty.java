/**
 * 
 */
package com.dadfha.lod.csv;

/**
 * 
 * Do you want this to represent each single property of each schema entity OR property type definition for each actual 
 * property object (which may has its own unique value such as weight)?
 * 
 * There is no such thing as "type" in CSV-X schema, since, for example, the property cannot be reused with
 * parameterized attributes without special syntax designed (how to instantiate?). Also type checking or handling 
 * of conflict type of data literal as in javascript, will become an issue.
 * 
 * However, we can introduce "reusable pattern/template/macro" like we did with {var}. Still we need to think of syntax
 * how a pattern may be parameterized for each instance (Yes, everything is instance, so the "template" is a prototype
 * instance in a way). 
 * 
 * @author Wirawit
 *
 */
public class SchemaProperty extends SchemaEntity {

	/**
	 * Parent schema table.
	 */
	private SchemaTable parentTable;	
	
	public SchemaProperty(String propName, SchemaTable parentTable) {
		this.parentTable = parentTable;
		setName(propName);
	}
	
	/**
	 * Set the '@name' property of this schema property while also update its register 
	 * inside hashmap collection of its parent schema table.
	 * 
	 * Note that if its parent table has not yet bind with this schema property, it'll be 
	 * done so by this method.
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {		
		if(parentTable == null) throw new RuntimeException("Parent table was not initialized for schema property: " + this);
		String oldName = getName();
		if(oldName != null) {			
			if(parentTable.hasSchemaProperty(oldName)) parentTable.removeSchemaProperty(oldName);
			addProperty(METAPROP_NAME, name);			
		} else { // if it has never been set before, call setName()
			super.setName(name);
		}
		parentTable.addSchemaProperty(this);
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getParentSchema()
	 */
	@Override
	public Schema getParentSchema() {
		return parentTable.getParentSchema();
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getSchemaTable()
	 */
	@Override
	public SchemaTable getSchemaTable() {
		return parentTable;
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getRefEx()
	 */
	@Override
	public String getRefEx() {
		return "@property[" + getName() + "]";
	}

}
