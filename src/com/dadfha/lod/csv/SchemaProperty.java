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
		setName(propName);
		this.parentTable = parentTable;
	}
	
	/**
	 * Set the '@name' property of this schema property while also update its registry 
	 * inside hashmap collection of parent schema table.
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {
		String oldName = getName();		
		assert(oldName != null && parentTable.hasSchemaProperty(oldName)) : "A schema property must always have a name and a registry inside its parent table."; 
		parentTable.removeSchemaProperty(oldName);
		setName(name);
		parentTable.addSchemaProperty(this);
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getParentSchema()
	 */
	@Override
	public Schema getParentSchema() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getSchemaTable()
	 */
	@Override
	public SchemaTable getSchemaTable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.dadfha.lod.csv.SchemaEntity#getRefEx()
	 */
	@Override
	public String getRefEx() {
		return "@property[" + getName() + "]";
	}

}
