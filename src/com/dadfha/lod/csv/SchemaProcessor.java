package com.dadfha.lod.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dadfha.mimamo.air.DataSet;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class SchemaProcessor {
	
	/**
	 * Parser buffer size in KB unit (multiple of 1024 bytes) for disk IO performance. 
	 */
	public static final int CSV_PARSER_BUFFER_SIZE = 8 * 1024;
	
	private boolean tryAllSchemas = false;
	
	/**
	 * Each processor holds a set of schemas in memory for processing.
	 * IMP this could be scaled to a persistent repository. 
	 */
	private Map<String, Schema> schemas = new HashMap<String, Schema>();

	/**
	 * @return the tryAllSchemas
	 */
	public boolean isTryAllSchemas() {
		return tryAllSchemas;
	}


	/**
	 * @param tryAllSchemas the tryAllSchemas to set
	 */
	public void setTryAllSchemas(boolean tryAllSchemas) {
		this.tryAllSchemas = tryAllSchemas;
	}

	private CsvParserSettings getCsvParserSettings(Schema schema) {
		
		if(schema == null) throw new IllegalArgumentException("Schema schema is null.");
		
		CsvParserSettings settings = new CsvParserSettings();
		
		// line separator, MacOS uses '\r'; and Windows uses '\r\n'.
		if(schema.getLineSeparator() != null) settings.getFormat().setLineSeparator(schema.getLineSeparator());
		else settings.setLineSeparatorDetectionEnabled(true);
		
		// define empty value fill
		if(schema.getEmptyValueFill() != null) settings.setEmptyValue(schema.getEmptyValueFill());
		else settings.setEmptyValue(""); // or empty value will keep being empty string.
		
		// define missing value fill
		if(schema.getMissingValueFill() != null) settings.setNullValue(schema.getMissingValueFill());
		else {} // default to 'null' string when printing.
		
		// header extraction, anyone?
		settings.setHeaderExtractionEnabled(false);
		
		return settings;
	}
	
	
	public void loadSchemas(String[] schemaPaths) {
		for(String path : schemaPaths) {
			loadSchema(path);
		}
	}
	
	public void loadSchema(String schemaPath) {
		Schema s = new Schema(schemaPath);
		schemas.put(s.getId(), s);
	}
	
	/**
	 * Check if input csv ID matches with any of target csv in all schema(s).
	 * Note that this does not guarantee that the csv is valid for the matched schema.
	 * @param csvId
	 * @return String of schema ID that has target csv matched or null if none matched.  
	 */
	public String findMatchSchema(String csvId) {
		
		if(csvId == null || schemas.size() == 0) return null;
		
		// check for each schema
		for(Schema s : schemas.values()) {
			
			if(s.getTargetCsvs() != null) { // check if this schema has csv target defined
				
				for(String targetCsvId : s.getTargetCsvs()) { // for each target
					
					if(targetCsvId.equals(csvId)) { // if target csv matched csv id, return matched schema id
						System.out.println("Found csv id specified in a schema id : " + s.getId());
						return s.getId();
					} else continue; // if not matched (yet), continue checking the next target

				} // end loop through all csv target(s)
				
			} else { // there is no target csv defined in this schema
				continue; // check next schema
			}
			
		} // end for each schema	
		
		return null;
	}
	
	/**
	 * 
	 * @param csvPath
	 * @param schema
	 * @return Object of Java type sepcified by the schema or Dataset as default. Return null is error in parsing.
	 */
	public Object parseWithSchema(String csvPath, Schema schema) {

		// init the parser settings according to schema
		CsvParserSettings settings;
		if(schema == null) throw new IllegalArgumentException("Schema schema is null.");
		settings = getCsvParserSettings(schema);
		
		// IMP according to map datatype in schema, choose applicable row processor..
		// Ex. CSV - SSN OWL2 mapping 

		// configure RowProcessor to process the values of each parsed row.
		DatapointRowSetProcessor rowProc = new DatapointRowSetProcessor(schema);
		settings.setRowProcessor(rowProc);

		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);

		// the 'parse' method will parse the file and delegate each parsed row to the RowProcessor
		try {
			parser.parse(new BufferedReader(new FileReader(csvPath), CSV_PARSER_BUFFER_SIZE));
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find CSV file.");
			e.printStackTrace();
			return null;
		}

		// List<Datapoint[]> rows = (List<Datapoint[]>) rowProc.getRows();
		//
		// for(Datapoint[] d : rows) {
		// System.out.println(Arrays.toString(d));
		// }		
		
		return rowProc.getData();
	}
		
	public Set<DataSet> getDatasets(String csvPath, String csvId, String[] schemaPaths) { 
		
		// load schema(s)
		loadSchemas(schemaPaths);

		// check if CSV-X schema has its target CSV specified and is matched with the csv ID 
		String sId = (tryAllSchemas)?  null : findMatchSchema(csvId);
		
		Object data = null;
		
		if(sId != null) { // if matched schema ID is known, parse with the schema			
			Schema schema = schemas.get(sId);
			assert(schema != null) : "Impossible case of unrecognized schema ID : " + sId;
			data = parseWithSchema(csvPath, schema);			
		} else { 			
			// The processor loops through known schemas until it successfully parse the CSV.
			for(Schema schema : schemas.values()) {				
				data = parseWithSchema(csvPath, schema);				
			} 			
		}
		
		if(data != null) {
			return (Set<DataSet>) data;
		} else {
			System.err.println("The parse bears no fruit: check out errors log.");
			return null;
		}
				
	}	
	
	
	public void parseCsvStream() {
		// IMP by leveraging this, we can implement CSV stream parsing!
		
//	    // Way to parse CSV directly from String
//		CsvSchemaParser parser = new CsvSchemaParser(new CsvSchemaParserSettings());
//	    parser.beginParsing(new BufferedReader(new StringReader("CSV in String format"), CsvSchemaParser.BUFFER_SIZE));
//	    String[] row;
//	    for(int i = 0; (row = parser.parseNext()) != null ; i++) {
//	        for(int j = 0; j < row.length; j++) {
//	        	// ???
//	        }
//	    }	    
//	    parser.stopParsing();		
	}
	

}
