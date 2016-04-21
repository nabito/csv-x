package com.dadfha.lod.csv;

import java.util.HashSet;

import com.dadfha.lod.LodHelper;
import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;

public abstract class DatapointRowProcessor implements RowProcessor {	
	
	/**
	 *  Each schema stores headers (field names) at each CSV's data row, col
	 *  (both starting from 0 but aren't text's row/col number but rather parsed row/col number) 
	 */
	protected HashSet<Schema> schemas = new HashSet<Schema>();

	protected int currSecNum = 0;
	protected int prevSecNum = 0;
	protected int currSecRow = 0;
	protected int currCol = 0;
	protected boolean justEnterSec = false;

	@Override
	public void rowProcessed(String[] row, ParsingContext context) {
				
		Datapoint[] datapointRow = new Datapoint[row.length];
		
		// check cue for matching pattern in each CSV's section		
		currSecNum = identifySection(row, context);
		
		// check if we've just enter new section
		if(currSecNum != prevSecNum) {
			prevSecNum = currSecNum;
			justEnterSec = true;
		}
		
		for(int i = 0; i < row.length; i++) {
			
			if(justEnterSec == true) {
				currSecRow = 0;
				justEnterSec = false;
			} else {
				currSecRow += 1;
			}
			currCol = i;
			
			datapointRow[i] = new Datapoint();
			// IMP context.currentColumn() is always 0, becoz rowProcessed(..) get called when all column of a row are processed!
			// we may want to help generalizing custom CSV parsing by introducing CSV schema file as CSV format template
			// also, add a flag option to parser whether should it call columnProcessed(..) or not
			
			//datapointRow[i].setId(context.currentRecord() + "," + currCol);
			datapointRow[i].setId(context.currentLine() + "," + currCol);
			datapointRow[i].setValue(row[i]);
			datapointRow[i].setDatatype(LodHelper.identifyDatatype(row[i]));
			datapointRow[i].setLabel(schemas[currSecNum].getFieldLabel(currSecRow, currCol));
			
			// hand over to custom process in subclass for each datapoint 
			processDatapoint(datapointRow[i], row[i], context);
		}		
		
		// hand over to any subclass wanting to do some after processing on Datapoint[]
		rowProcessed(datapointRow, context);						
	}
	
	/**
	 * Check cue for matching pattern in each CSV's section.
	 * Note that the function will validate in the order of how section is declared in the implementation.
	 * It returns immediately when it's found first match.
	 * @param row
	 * @param context
	 * @return section number starting from 0. -1 means sections[] is null. 
	 */
	public int identifySection(String[] row, ParsingContext context) {		
		int secNum = -1;
		if(schemas == null) return secNum;
		for(int i = 0; i < schemas.length; i++) {
			if(schemas[i].validate(row)) {
				secNum = i;
				break;
			}
		}
		return secNum;
	}
	
	/**
	 * For custom process in subclass for each datapoint.
	 * @param dp
	 * @param val
	 * @param context
	 */
	abstract public void processDatapoint(Datapoint dp, String val, ParsingContext context);
	
	/**
	 * For subclass wanting to do some after processing on a row of Datapoint[].
	 * @param row
	 * @param context
	 */
	abstract public void rowProcessed(Datapoint[] row, ParsingContext context);
	

}
