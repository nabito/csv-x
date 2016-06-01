package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;


/**
 * 
 * IMP context.currentColumn() is always 0, becoz rowProcessed(..) get called when all column of a row are processed!
 * but in this for_loop we "could" call to columnProcessed() for each column.
 * Add a flag option to parser whether should it call columnProcessed(..) or not
 * Or we can modify AbstractParser so that it supports col by col parsing to increase granularity, 
 * with may be performance trade-off.
 * 
 * @author Wirawit
 *
 */
public class DatapointRowProcessor implements RowProcessor {
	
	protected final Schema schema;
	
	protected final SchemaProcessor sp;
	
	/**
	 * Waiting list for post-processing Datapoints, including:
	 * - @cell[x,y] value replacement.
	 */
	protected List<String> postProcDps = new ArrayList<String>(); 
	
	//protected String currTable = "default";

	protected int currRow = -1;
	
	protected boolean isInRepeatingRow = false;
	
	protected int repeatTimes = 0;
	
	protected int currSubRow = -1;
	
	protected int currCol = -1;
	
	protected String currVal;
	
	protected boolean schemaMatched = false;
	
	/**
	 * Constructor needs a CSV-X schema. 
	 * @param schema
	 */
	public DatapointRowProcessor(Schema schema, SchemaProcessor sp) {
		this.schema = schema;
		this.sp = sp;
		
		
	}

	@Override
	public void rowProcessed(String[] row, ParsingContext context) {

		if(isInRepeatingRow) {
			currSubRow++;
		} else {
			currRow = (int) context.currentLine();
		}
	
		// match each incoming row of CSV against CSV-X schema
		for(currCol = 0; currCol < row.length; currCol++) {
			// get current cell value	
			currVal = row[currCol];
			
								
			// validate parsed CSV record against CSV-X schema properties				
			if(st.validate(currRow, currCol, currVal)) {
				
				SchemaRow sRow = schema.getRow(currRow);
				
				// check if this is a repeating row
				if(sRow.isRepeat()) {
					isInRepeatingRow = true;
					repeatTimes = sRow.getRepeatTimes();
					currSubRow = 0;
				}
				
				Cell c = schema.getCell(currRow, currCol);
					
				// value replacement 
				currVal = schema.replaceValue(currVal);
				

					
					// create datapoint if specified so by schema					
					if(c.getType().compareToIgnoreCase("Datapoint") == 0) {
						Datapoint dp = new Datapoint();
						
						// FIXME check what goes into dp object, and what need post processing
						// Basically, what is NOT meta-property (starting with '@') will be passed on to DP's property
						
						
						//dp.setId(context.currentLine() + "," + currCol); // should follow IETF addressing for Provenance!?? 
						

						
						
						dp.setProperties(c.getProperties()); 
						
						dp.setDatatype(c.getDatatype()); // FIXME this is the datatype of cell's value, thus it should be converted to just a datatype of property(ies) referring to the value. 
						
						// the idea of "Datapoint" is that it must represent exactly one nominal value which means one thing 
						// and can be referred through 'hasValue' property making it possible to utilize data without 
						// knowing application specific name for the property.
						//
						// there can be other peripheral/context properties describing the datapoint, which cannot avoid 
						// using domain specific name. In this case, the extensible property definition can provide useful hint 
						// for software agent to recognize its structure and meaning, a.k.a. knowing how to consume it, 
						// using URI, title, description, or any other form of semantic representation.
						//
						// This dataset and datapoint model leverage conventions and patterns in LinkedDataPlatform specification 
						// to represent a list of data...2B Continue..
						dp.setValue(currVal); 
						
						
						// hand over to custom process in subclass for each datapoint 
						processDatapoint(dp, currVal, context);
						
					} else {
						
					}
					
					
					
					continue;
				} else {
					// TODO remove all objects created as a result of parsing against this schema (i.e. rollback) 
					// & try other schema from last succeed parsed.
					break;					
				}

			} // end of row processing
			

			
			// if the schema has only has just one more cell definition, it's dimension mismatched
			if(s.getCell(currRow, currCol) != null) break;
			
		

		
				
		
		// hand over to any subclass wanting to do some after processing on Datapoint[]?
		rowProcessed(datapointRow, context);						
	}
	
	/**
	 * For custom process in subclass for each datapoint.
	 * @param dp
	 * @param val
	 * @param context
	 */
	public void processDatapoint(Datapoint dp, String val, ParsingContext context) {}
	
	// TODO add some data conversion here, or else everything will be stored as String
	// This can be automatically detected from value or explicitly declared in schema file.
	// It also must be noted in datapoint's datatype as one of xml/datatype.
	// Identify some other properties like max/min all together here too..
	// also do dataCleanup() for ***** to a null value would be preferrable.		
	
	/**
	 * For subclass wanting to do some after processing on a row of Datapoint[].
	 * @param row
	 * @param context
	 */
	public void rowProcessed(Datapoint[] row, ParsingContext context) {}
	
	/**
	 * to get some data out of this parsing.
	 * @return some data form or null is the parse bear no fruit.
	 */
	public Object getData() { return null; }

	@Override
	public void processStarted(ParsingContext context) {
	}

	@Override
	public void processEnded(ParsingContext context) {
	}
	

}
