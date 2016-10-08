package com.dadfha.lod;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class LodHelper {
	
	public static final String NS_XSD_PREFIX = "xsd";
	public static final String NS_XSD_IRI = "http://www.w3.org/2001/XMLSchema";
	
	public static final String NS_RDFS_PREFIX = "rdfs";
	public static final String NS_RDFS_IRI = "http://www.w3.org/2000/01/rdf-schema#";
	
	public static String identifyDatatype(String val) {
		return null;		
	}
	
	/**
	 * Check if a string is a valid URL.
	 * @param text
	 * @return
	 */
	public static boolean isURL(String text) {
		try {
			new URL(text);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	/**
	 * Check if a string is in RDF/Turtle Prefixed Name (prefix:suffix) form.
	 * Note that abc:123:foo:bar is also valid. 
	 * @param text
	 * @return boolean
	 */
	public static boolean isPrefixedName(String text) {
		return text.matches("\\w*:\\S+");
	}
	
	/**
	 * Formatting RDF Turtle tuple (either one of subject, predicate, or object).
	 * If it's an IRI or relative path, wrap it with <> and return.
	 * 
	 * The original tuple is returned otherwise if it's in RDF/Turtle Prefixed Name 
	 * or if it's already in <IRI> form.
	 * 
	 * @param tuple
	 * @return String
	 * @throws Exception 
	 */
	public static String formatTtlTuple(String tuple) throws Exception {
		if(isPrefixedName(tuple)) return tuple;
		else if(tuple.startsWith("<") && tuple.endsWith(">")) {
			if(!isURL(tuple.substring(1, tuple.length() - 1))) throw new Exception("The tuple is not valid bracketed IRI: " + tuple); 
			else return tuple;
		} else {
			return "<" + tuple + ">";
		}
	}
	
	/**
	 * Format object tuple for RDF/Turtle. 
	 * 
	 * Value in absolute IRI or prefix:suffix form will be put in bracket or return as is, respectively.   
	 * 
	 * Since there's no way to differentiate a relative IRI from a normal string, 
	 * all property value are treated as string literal by default, except when datatype 
	 * is provided as "xsd:anyURI".
	 *  
	 * @param object
	 * @param datatype
	 * @param langCode
	 * @return String
	 * @throws Exception 
	 */
	public static String formatTtlObject(String object, String datatype, String langCode) throws Exception {		
		// if not a valid IRI, then it could be prefix:suffix form of IRI or relative IRI.
		if(datatype != null) {
			if(datatype.equals("anyURI") || datatype.equals("xsd:anyURI")) {
				return formatTtlTuple(object);
			} else {
				return formatTtlLiteral(object, datatype, langCode);		
			}
		}
		
		if(isURL(object)) return "<" + object + ">";
		else if(isPrefixedName(object)) return object;
		else { // treat everything as string from this point!
			return formatTtlLiteral(object, datatype, langCode);
		}				
	}
	
	/**
	 * Format RDF Turtle literal according to its datatype and language code.
	 * If no datatype is specified, it's default to xsd:string.
	 * @param literal
	 * @param datatype XML Schema Datatype 1.1 (http://www.w3.org/TR/xmlschema11-2/)
	 * @param langCode
	 * @return formatted String
	 */
	public static String formatTtlLiteral(String literal, String datatype, String langCode) {		
		if(datatype == null) datatype = "string";
		switch(datatype) {
		case "string":
			// wrap the value within quotes
			literal = "\"" + literal + "\"";
			if(langCode != null) literal = literal.concat("@" + langCode);
			break;			
		case "int":
		case "integer":
			break;
		case "long":
			literal = "\"" + literal + "\"^^xsd:long";
			break;
		case "short":
			literal = "\"" + literal + "\"^^xsd:short";
			break;
		case "decimal":
			break;
		case "float":
			literal = "\"" + literal + "\"^^xsd:float";
			break;
		case "double":
			break;
		case "boolean":
			break;
		case "byte":
			literal = "\"" + literal + "\"^^xsd:byte";
			break;
		case "QName":
			literal = "\"" + literal + "\"^^xsd:QName";
			break;
		case "NOTATION":
			literal = "\"" + literal + "\"^^xsd:NOTATION";
			break;
		case "dateTime":
			literal = "\"" + literal + "\"^^xsd:dateTime";
			break;
		case "base64Binary":
			literal = "\"" + literal + "\"^^xsd:base64Binary";
			break;
		case "hexBinary":
			literal = "\"" + literal + "\"^^xsd:hexBinary";
			break;
		case "unsignedInt":
			literal = "\"" + literal + "\"^^xsd:unsignedInt";
			break;
		case "unsignedShort":
			literal = "\"" + literal + "\"^^xsd:unsignedShort";
			break;
		case "unsignedByte":
			literal = "\"" + literal + "\"^^xsd:unsignedByte";				
			break;
		case "time":
			literal = "\"" + literal + "\"^^xsd:time";
			break;
		case "date":
			literal = "\"" + literal + "\"^^xsd:date";
			break;
		case "gYearMonth":
			literal = "\"" + literal + "\"^^xsd:gYearMonth";
			break;
		case "gYear":
			literal = "\"" + literal + "\"^^xsd:gYear";
			break;
		case "gMonthDay":
			literal = "\"" + literal + "\"^^xsd:gMonthDay";
			break;
		case "gDay":
			literal = "\"" + literal + "\"^^xsd:gDay";
			break;
		case "gMonth":
			literal = "\"" + literal + "\"^^xsd:gMonth";
			break;
// all IRI should be in the bracket in RDF/Turtle
//		case "anyURI":
//			literal = "\"" + literal + "\"^^xsd:anyURI";
//			break;			
		case "duration":
			literal = "\"" + literal + "\"^^xsd:duration";
			break;
		case "NCName":
			literal = "\"" + literal + "\"^^xsd:NCName";
			break;
		default:
			throw new IllegalArgumentException("Unrecognized datatype: " + datatype);
		}
		return literal;
	}
	
	/**
	 * Build an RDF Turtle Triple statement.
	 * The generated statement is either a single statement or open statements 
	 * for other predicate-object pairs. 
	 * 
	 * @param subject
	 * @param predicate
	 * @param object 
	 * @param datatype of object
	 * @param langCode of object
	 * @param endSentence
	 * @return String
	 * @throws Exception 
	 */
	public static String buildTtlTriple(String subject, String predicate, String object, String datatype, String langCode, boolean endSentence) throws Exception {		
		subject = formatTtlTuple(subject);
		predicate = formatTtlTuple(predicate);
		object = formatTtlObject(object, datatype, langCode);
		char delimiter = ';';
		if(endSentence) delimiter = '.';
		return subject + " " + predicate + " " + object + " " + delimiter + System.lineSeparator();
	}
	
	/**
	 * Build an RDF Turtle Triple statement.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param endSentence
	 * @return String
	 * @throws Exception
	 */
	public static String buildTtlTriple(String subject, String predicate, String object, boolean endSentence) throws Exception {
		return buildTtlTriple(subject, predicate, object, null, null, endSentence);
	}
	
	/**
	 * Build an RDF Turtle Triple statement. 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return String
	 * @throws Exception
	 */
	public static String buildTtlTriple(String subject, String predicate, String object) throws Exception {
		return buildTtlTriple(subject, predicate, object, null, null, true);
	}	
	
	/**
	 * IMP Build an RDF Turtle Triples from map of predicate and object(s) pairs. 
	 * @param subject
	 * @param predObjs A map containing associations between predicate and object(s). 
	 * @return String
	 * @throws Exception 
	 */
	public static String buildTtlTriple(String subject, Map<String, Map<String, String>> predObjs) throws Exception {
		StringBuilder sb = new StringBuilder();
		subject = formatTtlTuple(subject);
		sb.append(subject + " ");		
		
		for(Entry<String, Map<String, String>> e : predObjs.entrySet()) {
			String predicate = e.getKey();
			Map<String, String> objects = e.getValue();
			predicate = formatTtlTuple(predicate);
			sb.append(predicate + " ");
			
//			for(int i = 0; i < objects.length; i++) {
//				objects[i] = formatTtlObject(objects[i], null, null);
//				sb.append(objects[i]);
//				if(i < objects.length - 1) sb.append(", ");
//				else sb.append(" .");
//			}
		}
		return null;
	}
	
	/**
	 * Build an action part of an RDF Turtle triple (predicate object).
	 * @param predicate
	 * @param object
	 * @param endSentence whether to end the sentence (triple) in this action.
	 * @return String
	 * @throws Exception 
	 */
	public static String buildTtlAction(String predicate, String object, boolean endSentence) throws Exception {
		predicate = formatTtlTuple(predicate);
		object = formatTtlObject(object, null, null);
		char delimiter = ';';
		if(endSentence) delimiter = '.';
		return predicate + " " + object + " " + delimiter + System.lineSeparator();
	}	

}
