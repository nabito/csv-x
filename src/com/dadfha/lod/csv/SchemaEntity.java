package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

public abstract class SchemaEntity {

	/**
	 * The name given for a schema entity. Must be unique within the scope of
	 * schema. If explicitly specified in schema file, it'll also be used as
	 * variable name for the entity.
	 * 
	 * This must be XML QNAME so it can suffix an IRI to be an addressable
	 * resource in Linked Data according to RDF model.
	 */
	public static final String METAPROP_NAME = "@name";

	/**
	 * The value defined by '@value' property for each schema entity. 
	 * 
	 * For Cell, this is the property to store parsed CSV value for each cell.
	 * For Row, Table, and Property this has no specific semantic defined.
	 */
	public static final String METAPROP_VALUE = "@value";

	/**
	 * The unique id for the entity within a processing context. Can be UUID for
	 * world-wide scope. 
	 * 
	 * OPT This can be auto-generated as IETF CSV row,col URI addressing scheme for Cell 
	 * so the Provenance of where it came from is preserved. 
	 * 
	 */
	public static final String METAPROP_ID = "@id";
	
	/**
	 * Language code to tell what default language the literals inside this entity is written in.
	 * This should comply with ISO 639-1 (https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes). 
	 */
	public static final String METAPROP_LANG = "@lang";
	
	/**
	 * The user-defined data model type in which the cell is mapped to.
	 */
	public static final String METAPROP_MAPTYPE = "@maptype";
	
	/**
	 * Regular expression applied differently for each schema entity type.
	 * 
	 * For Cell, it's applied to '@value' property.
	 * For Row, it's applied to '@value' of all cells within the row.
	 * For Table, it's applied to '@value' of all cells within the table.
	 * For Property, it's applied to its mapped literal in the use context.
	 * 
	 */
	public static final String METAPROP_REGEX = "@regex";
	
	/**
	 * The XML datatype according to latest XML Schema Datatype specification.
	 * 
	 * For Cell, it's applied to '@value' property.
	 * For Row, it's applied to '@value' of all cells within the row.
	 * For Table, it's applied to '@value' of all cells within the table.
	 * For Property, it's applied to its mapped literal in the use context.
	 */
	public static final String METAPROP_DATATYPE = "@datatype";	

	/**
	 * Schema enitity's properties. HashMap storing mapping between property's
	 * name and its value.
	 * 
	 * Storing extra field's properties in a collection rather than class's
	 * attributes has many merits.
	 * 
	 * It avoids code change whenever there is a new property introduced, either
	 * from schema's syntax update or user's defined annotation. By default, all
	 * these new properties should automatically be reflected in each datapoint
	 * when a schema is applied to a CSV.
	 * 
	 * Imagine doesn't have to do:
	 * 
	 * datapoint.setNewProp(section.getNewPropAtField(row, col));
	 * 
	 * Which involves creating new attributes, methods in both Field, Schema,
	 * and Datapoint classes!
	 * 
	 * Except for meta-properties to explain cell's concept like row and column
	 * which are not explicitly written in the schema file and cannot be changed
	 * by user.
	 * 
	 * IMP in the next version, consider change the map to <String, Object> to 
	 * support variety of datatype which may be needed by user-defined data. 
	 * 
	 */
	Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Default constructor.
	 */
	public SchemaEntity() {}
	
	/**
	 * Copy constructor for SchemaEntity
	 * 
	 * @param se
	 */
	public SchemaEntity(SchemaEntity se) {
		// Collection default copy constructor is deepcopy as long as the object
		// type inside is immutable, which is so for String.
		properties = new HashMap<String, String>(se.getProperties());
	}

	/**
	 * Get parent schema of this schema entity.
	 * 
	 * @return Schema.
	 */
	public abstract Schema getParentSchema();

	/**
	 * Get schema table that the entity is contained in. Just returning itself
	 * if the entity is of type SchemaTable.
	 * 
	 * @return SchemaTable.
	 */
	public abstract SchemaTable getSchemaTable();

	/**
	 * Schema Entity Reference Expression (SERE) of this schema entity in
	 * full(expanded) form. E.g. \@table[name].@cell[x,y]
	 * 
	 * @return String
	 */
	public abstract String getRefEx();

	/**
	 * Get literal value of a property with the given name.
	 * 
	 * @param propertyName
	 * @return String holding literal value of the property 
	 * or null if the property is not yet defined.
	 */
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	/**
	 * Get the whole extra/user-defined property-value map collection.
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Add a property name-value map. Already existing key will be overwritten.
	 * 
	 * @param prop
	 * @param val
	 */
	public void addProperty(String prop, String val) {
		properties.put(prop, val);
	}

	/**
	 * Add properties to the schema entity. Any existing properties with the
	 * same name will be overwritten.
	 * 
	 * @param properties
	 */
	public void addProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}

	/**
	 * Get name of this schema entity.
	 * 
	 * @return String of variable name or null if none defined. 
	 */
	public String getName() {
		return getProperty(METAPROP_NAME);
	}

	/**
	 * Set name of this schema entity. 
	 * 
	 * Since 'name' is used as an identifier for schema entity, to ensure safe update the name of this schema entity 
	 * the override implementation of this method must consider the following:
	 * 
	 * 1. Any reference to this schema entity by its name must also get update.
	 * 2. If the name property is used for hash creation or generate output of some kinds from the property, 
	 * those produced value should also get update too.
	 * 3. The change doesn't violate the program's business logics.
	 * 
	 * It may also incorporate any additional program logics into the method as well.
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {		
		addProperty(METAPROP_NAME, name);
	}

	/**
	 * Get value of this schema entity.
	 * 
	 * @return
	 */
	public String getValue() {
		return getProperty(METAPROP_VALUE);
	}

	/**
	 * Set value of this schema entity.
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		addProperty(METAPROP_VALUE, value);
	}

	/**
	 * Get id of this entity.
	 * 
	 * @return String
	 */
	public String getId() {
		return getProperty(METAPROP_ID);
	}

	/**
	 * Set id of this entity.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		addProperty(METAPROP_ID, id);
	}
	
	/**
	 * Get default language code.
	 * 
	 * @return String
	 */
	public String getLang() {
		return getProperty(METAPROP_LANG);
	}

	/**
	 * Set default language code.
	 *  
	 * @param lang
	 */
	public void setLang(String lang) {
		addProperty(METAPROP_LANG, lang);
	}
	
	public String getMapType() {
		return getProperty(METAPROP_MAPTYPE);
	}
	
	public void setMapType(String type) {
		// IMP check if it's recognized type (Datapoint or user-defined)
		addProperty(METAPROP_MAPTYPE, type);
	}		
	
	public String getRegEx() {
		return getProperty(METAPROP_REGEX);
	}
	
	public void setRegEx(String regEx) {
		addProperty(METAPROP_REGEX, regEx);
	}
	
	public String getDatatype() {
		return getProperty(METAPROP_DATATYPE);
	}
	
	public void setDatatype(String datatype) {
		// TODO check if it's a recognized XML datatype.
		addProperty(METAPROP_DATATYPE, datatype);
	}	

	/**
	 * Give SERE output to identify itself.
	 * 
	 * @return String
	 */
	public String toString() {
		return getRefEx();
	}

}
