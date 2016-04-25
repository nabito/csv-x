package com.dadfha.lod.csv;

import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;

public class TokyoAirRowListProcessor extends DatapointRowListProcessor {	
	
	private String reEmpty = "[^\\S\r\n]*?"; // Regular Expression for any whitespace characters except newline	
	
	// IMP better representation of schema would be to have an array of JSON objects each representing a Field and its metadata like regEx, assign variable, name, label, type, datatype and so on.
	// The field must be described in order from row, col [0,0] then [0, 1] and [0, 2] so on until new line which start [1, 0] then [1, 1] until the end of schema.
	// In other word, there could be 2 types of JSON-based schema representation, one described by field selection and the other described in field-by-field in ascendant order.
	// The 2-Dimension of JSON object must also reflect the row, col structure of the CSV schema too.
	
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
		
	    // TODO make schema file with fieldSelection each applied with a set of properties
	    // parsed it and apply all properties from the selection to a Section object having
	    // common properties and unique properties for each Field. 
	    
	    // Store field's info individually in ArrayList<Field[]>, because the search for a field at [row, col]
	    // will have O(1) vs O(n) using HashSet<FieldSelect> (Even not counting the case when a field may be in more 
	    // than one FieldSelect region). Hash needs implementation of hashCode() && equals() as well while CSV schema is 
	    // index-based by nature just like ArrayList<E>.
	    
	    // TODO But we must find a way to describe "repeat" pattern of a row, either by Section or a 
	    // property of FieldRow object.		
		
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
