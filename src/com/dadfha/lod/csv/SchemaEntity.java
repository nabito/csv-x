package com.dadfha.lod.csv;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dadfha.lod.LodHelper;

public abstract class SchemaEntity {
	
	private static final Logger logger = LogManager.getLogger();

	/**
	 * The name given for a schema entity. Must be unique within the scope of
	 * schema. If explicitly specified for a schema entity description, 
	 * it'll also be used as variable name for the entity. 
	 * 
	 * The variable name must follow identifier naming.
	 */
	public static final String METAPROP_NAME = "@name";
	
	public static final String METAPROP_NAME_PRED = Schema.NS_PREFIX + ":name";

	/**
	 * The value defined by '@value' property for each schema entity. 
	 * 
	 * For Cell, this is the property to store parsed CSV value for each cell.
	 * For Row, Table, and Property this has no specific semantic defined.
	 */
	public static final String METAPROP_VALUE = "@value";
	
	public static final String METAPROP_VALUE_PRED = Schema.NS_PREFIX + ":value";

	/**
	 * The unique id for the entity within a processing context. Can be UUID for
	 * world-wide scope. 
	 * 
	 * OPT This can be auto-generated as IETF CSV row,col URI addressing scheme for Cell 
	 * so the Provenance of where it came from is preserved. 
	 * 
	 */
	public static final String METAPROP_ID = "@id";
	
	public static final String METAPROP_ID_PRED = Schema.NS_PREFIX + ":id";
	
	/**
	 * Language code to tell what default language the literals inside this entity is written in.
	 * This should comply with ISO 639-1 (https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes). 
	 */
	public static final String METAPROP_LANG = "@lang";
	
	public static final String METAPROP_LANG_PRED = Schema.NS_PREFIX + ":lang";
	
	/**
	 * The user-defined data model type in which the cell is mapped to.
	 */
	public static final String METAPROP_MAPTYPE = "@mapType";
	
	public static final String METAPROP_MAPTYPE_PRED = Schema.NS_PREFIX + ":mapType";
	
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
	
	public static final String METAPROP_REGEX_PRED = Schema.NS_PREFIX + ":regex";
	
	/**
	 * The XML datatype according to latest XML Schema Datatype specification.
	 * 
	 * For Cell, it's applied to '@value' property.
	 * For Row, it's applied to '@value' of all cells within the row.
	 * For Table, it's applied to '@value' of all cells within the table.
	 * For Property, it's applied to its mapped literal in the use context.
	 */
	public static final String METAPROP_DATATYPE = "@datatype";	
	
	public static final String METAPROP_DATATYPE_PRED = Schema.NS_PREFIX + ":datatype";
	
	/**
	 * The meta property storing name to identify schema data.
	 */
	public static final String METAPROP_DATANAME = "@dataName";
	
	public static final String METAPROP_DATANAME_PRED = Schema.NS_PREFIX + ":dataName";
	
	/**
	 * The meta property storing name to identify schema table.
	 */
	public static final String METAPROP_TBLNAME = "@tableName";
	
	public static final String METAPROP_TBLNAME_PRED = Schema.NS_PREFIX + ":tableName";
	
	/**
	 * The meta property storing name to identify schema property.
	 */
	public static final String METAPROP_PROPNAME = "@propName";	
	
	public static final String METAPROP_PROPNAME_PRED = Schema.NS_PREFIX + ":propName";

	/**
	 * The meta property for RDF template mapping. 
	 */
	public static final String METAPROP_MAP_TEMPLATE = "@mapTemplate";
	
	public static final String METAPROP_MAP_TEMPLATE_PRED = Schema.NS_PREFIX + ":mapTemplate";

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
	 * Check if property with the specified name is defined in this schema entity.
	 * @param propName
	 * @return true or false
	 */
	public boolean hasProperty(String propName) {
		return properties.containsKey(propName);
	}

	/**
	 * Get literal value of a property with the given name.
	 * IMP soon this must support return type of Object as per JSON capability.
	 * @param propName
	 * @return String holding literal value of the property 
	 * or null if the property is not yet defined.
	 */
	public String getProperty(String propName) {
		return properties.get(propName);
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
	 * Get value of this schema entity specified by '@value' property.
	 * 
	 * @return String
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
	 * Check if the schema entity has a template mapping or not.
	 * @return boolean
	 */
	public boolean hasTemplateMapping() {
		return properties.containsKey(METAPROP_MAP_TEMPLATE);
	}
	
	/**
	 * Get the mapping with a template with this schema entity.
	 * @return String or null if there's no mapping.
	 */
	public String getTemplateMapping() {
		return getProperty(METAPROP_MAP_TEMPLATE);
	}
	
	/**
	 * Set the mapping with a template with this schema entity.  
	 * @param mapping
	 */
	public void setTemplateMapping(String mapping) {
		addProperty(METAPROP_MAP_TEMPLATE, mapping);
	}

	/**
	 * Give SERE output to identify itself.
	 * 
	 * @return String
	 */
	public String toString() {
		return getRefEx();
	}
	
	/**
	 * Serialize to RDF Turtle format.
	 * @return String
	 * @throws Exception 
	 */
	public String getTtl() throws Exception {
		
		StringBuilder sb = new StringBuilder();		
		String subject = null, predicate = null, object = null;		
		Schema parentSchema = getParentSchema();
		SchemaTable parentTable = getSchemaTable();		
		// for schema entity that isn't scoped in a schema table, e.g. schema template, default table is referred.
		if(parentTable == null) parentTable = parentSchema.getDefaultTable();
		
		// populate main subject
		String id = getId(); // use the id for triple subject if one is defined
		if(id == null) subject = getRefEx(); // default id for every entity is its SERE
		else subject = id;
				
		// check schema entity type, denote the type in ttl too
		predicate = "rdf:type";
		Class<? extends SchemaEntity> objCls = this.getClass();
		if(objCls.equals(SchemaTable.class)) {
			object = SchemaTable.CLASS_IRI;
		} else if(objCls.equals(SchemaRow.class)) {
			object = SchemaRow.CLASS_IRI;
		} else if(objCls.equals(SchemaColumn.class)) {
			object = SchemaColumn.CLASS_IRI;			
		} else if(objCls.equals(SchemaCell.class)) {
			object = SchemaCell.CLASS_IRI;
		} else if(objCls.equals(SchemaProperty.class)) {
			object = SchemaProperty.CLASS_IRI;
		} else if(objCls.equals(SchemaData.class)) {
			object = SchemaData.CLASS_IRI;
		} else if(objCls.equals(SchemaTemplate.class)) {
			object = SchemaTemplate.CLASS_IRI;			
		} else {
			throw new RuntimeException("Unrecognized Schema Entity type. Should never got here.");
		}
		
		// add node type annotation
		sb.append(LodHelper.buildTtlTriple(subject, predicate, object, true));
		
		// for each property inside a schema entity
		for(Map.Entry<String, String> e : properties.entrySet()) {
			String propName = e.getKey();
			String propVal = e.getValue();
			String datatype = null, langCode = null;			
			
			switch(propName) {
			case METAPROP_DATANAME:
				predicate = METAPROP_DATANAME_PRED;
				break;
			case METAPROP_DATATYPE:
				predicate = METAPROP_DATATYPE_PRED;
				if(propVal.indexOf(':') == -1) propVal = "xsd:" + propVal; 
				break;
			case METAPROP_ID:
				predicate = METAPROP_ID_PRED;
				datatype = "xsd:anyURI";
				break;
			case METAPROP_LANG:
				predicate = METAPROP_LANG_PRED;
				break;
			case METAPROP_MAPTYPE:
				predicate = METAPROP_MAPTYPE_PRED;
				break;		
			case METAPROP_MAP_TEMPLATE:
				predicate = METAPROP_MAP_TEMPLATE_PRED;
				break;						
			case METAPROP_NAME:
				predicate = METAPROP_NAME_PRED;
				break;
			case METAPROP_PROPNAME:
				predicate = METAPROP_PROPNAME_PRED;
				break;			
			case METAPROP_REGEX:
				predicate = METAPROP_REGEX_PRED;
				break;			
			case METAPROP_TBLNAME:
				predicate = METAPROP_TBLNAME_PRED;
				break;			
			case METAPROP_VALUE:				
				predicate = METAPROP_VALUE_PRED;
				datatype = getDatatype();
				if(getLang() != null) langCode = getLang();
				else langCode = (String) parentSchema.getProperty(METAPROP_LANG);
				break;
			default: 					
				predicate = getRefEx() + "/" + propName;
				langCode = (String) parentSchema.getProperty(METAPROP_LANG);				
				// link with schema property definition if one is defined in parent table
				assert(parentTable != null);
				SchemaProperty sProp = parentTable.getSchemaProperty(propName); 
				if(sProp != null) {					
					// declare this property type
					String propId = sProp.getProperty(METAPROP_ID);
					if(propId != null) {						
						sb.append(LodHelper.buildTtlTriple(predicate, "rdf:type", propId));
					} else {					
						sb.append(LodHelper.buildTtlTriple(predicate, "rdf:type", sProp.getRefEx()));
					}					
					datatype = sProp.getDatatype();
					langCode = sProp.getLang();
				}
				break;
			} // end property switch case	
			
			// create object tuple, resolve {var} expression, if any
			object = SchemaProcessor.processVarEx(propVal, this, propName, null);			
			// TODO in v1.x, must recall original datatype of value in CSV-X schema
			//this.getProperty(propName)
			
			// add statement(s) for each property-value pair inside this schema entity
			sb.append(LodHelper.buildTtlTriple(subject, predicate, object, datatype, langCode, true)); 
			
		} // end for each property
		
		return sb.toString();
	}

}
