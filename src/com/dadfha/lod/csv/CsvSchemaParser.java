package com.dadfha.lod.csv;

import java.util.HashMap;

import com.univocity.parsers.csv.CsvParser;

/**
 * This is the parser for CSV schema as well as a customizable CSV parser according to a CSV schema.
 * @author Wirawit
 *
 */
public class CsvSchemaParser extends CsvParser {

	/**
	 * Parser buffer size in KB unit (multiple of 1024 bytes) for disk IO performance. 
	 */
	public static final int BUFFER_SIZE = 8 * 1024;
	
	public CsvSchemaParser(CsvSchemaParserSettings settings) {
		super(settings);		
	}

	// TODO utilize HashMap<String, String> replaceValues = new HashMap<String, String>();
	
	// IMP make it support inline comment, so that we can represent all the schema metadata in a CSV format
	// which will improve performance (allow all metadata for each field extracted in 1 parse) 
	// than basing on JSON-expressed FieldSelect, which requires multiple revisit of each field.
	
	// IMP modify AbstractParser so that it supports col by col parsing to increase performance?
	
}
