package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

import com.dadfha.mimamo.air.DataSet;
import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;

public class DatapointRowListProcessor extends DatapointRowProcessor {
	
	public DatapointRowListProcessor(Schema schema) {
		super(schema);
		// TODO Auto-generated constructor stub
	}

	protected List<Datapoint[]> datapointRows;
	protected List<DataSet> dataSets;

	@Override
	public void processStarted(ParsingContext context) {
		datapointRows = new ArrayList<Datapoint[]>(128); // this is just an initial capacity of the list
	}

	@Override
	public void processEnded(ParsingContext context) {
	}

	@Override
	public void processDatapoint(Datapoint dp, String val, ParsingContext context) {
	}

	@Override
	public void rowProcessed(Datapoint[] row, ParsingContext context) {
		datapointRows.add(row);
	}
	
	public List<Datapoint[]> getRows() {
		return datapointRows;
	}
	
	public List<DataSet> getDataSets() {
		return dataSets;
	}

	@Override
	public Object getData() {
		return null;
	}

}
