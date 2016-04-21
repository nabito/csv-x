package com.dadfha.lod.csv;

import java.util.HashMap;

import com.univocity.parsers.csv.CsvParserSettings;

public class CsvSchemaParserSettings extends CsvParserSettings {
	
	/**
	 * Define a key-value map of string that when found while parsing should be replaced. 
	 */
	private HashMap<String, String> replaceValues = new HashMap<String, String>();
	
	public HashMap<String, String> getReplaceValues() {
		return replaceValues;
	}

	public void setReplaceValues(HashMap<String, String> replaceValues) {
		this.replaceValues = replaceValues;
	}	

}
