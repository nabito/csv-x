package com.dadfha.lod.csv;

import java.util.HashSet;
import java.util.Set;

import com.dadfha.mimamo.air.DataSet;
import com.dadfha.mimamo.air.Datapoint;
import com.univocity.parsers.common.ParsingContext;

public class DatapointRowSetProcessor extends DatapointRowProcessor {
	
	public static final int INIT_DP_SET_SIZE = 1024;
	
	protected Set<Datapoint> datapoints;
	
	protected Set<DataSet> dataSets;	
	
	public DatapointRowSetProcessor(Schema schema) {
		//super(schema);
		datapoints = new HashSet<Datapoint>(INIT_DP_SET_SIZE);
	}

	@Override
	public void processStarted(ParsingContext context) {
		
	}

	@Override
	public void processEnded(ParsingContext context) {
	}

	@Override
	public void processDatapoint(Datapoint dp, String val, ParsingContext context) {
	}

	@Override
	public void rowProcessed(Datapoint[] row, ParsingContext context) {		
		for(int i = 0; i < row.length; i++) {
			datapoints.add(row[i]);
		}
		// TODO there must be a point to tell when to pack a dataset
	}
	
	public Set<Datapoint> getAllDatapoints() {
		return datapoints;
	}
	
	public Set<DataSet> getDataSets() {
		return dataSets;
	}

	@Override
	public Object getData() {
		return dataSets;
	}

}
