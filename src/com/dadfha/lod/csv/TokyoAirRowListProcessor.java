package com.dadfha.lod.csv;

import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;

public class TokyoAirRowListProcessor extends DatapointRowListProcessor {	
	
	private String reEmpty = "[^\\S\r\n]*?"; // Regular Expression for any whitespace characters except newline	
	
	public TokyoAirRowListProcessor() {
		// init section
		schemas.add(new Schema("airp-csvx.json"));
		//schemas.add(new Schema("airpolt-schema.jsonld"));
		
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
	
	@Override
	public void processDatapoint(Datapoint dp, String val, ParsingContext context) {
		
		// TODO add some data conversion here, or else everything will be stored as String
		// This can be automatically detected from value or explicitly declared in schema file.
		// It also must be noted in datapoint's datatype as one of xml/datatype.
		// Identify some other properties like max/min all together here too..
		// also do dataCleanup() for ***** to a null value would be preferrable.		
		
	}

}
