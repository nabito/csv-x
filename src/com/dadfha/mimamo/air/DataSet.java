package com.dadfha.mimamo.air;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dadfha.lod.csv.CsvSchemaParser;
import com.dadfha.lod.csv.DatapointRowListProcessor;
import com.univocity.parsers.csv.CsvParser;


public class DataSet {
	
	/**
	 * Datapoints within the dataset. 
	 */
	private HashSet<Datapoint> datapoints = new HashSet<Datapoint>();
	
	/**
	 * Common properties of all datapoints in this dataset
	 */
	private HashMap<String, String> commonProperties = new HashMap<String, String>();
	
	/**
	 * Dataset title.
	 */
	private String datasetTitle;	
	
	/**
	 * Get all datapoints inside the dataset.
	 * @return Set<Datapoint>
	 */
	public Set<Datapoint> getDatapoints() {
		return datapoints;
	}
	
	public static List<DataSet> loadCsvData(String filePath, CsvParser parser, DatapointRowListProcessor rowProc) { 
	    
	    // the 'parse' method will parse the file and delegate each parsed row to the RowProcessor you defined
	    try {
			parser.parse(new BufferedReader(new FileReader(filePath), CsvSchemaParser.BUFFER_SIZE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    
//	    List<Datapoint[]> rows = (List<Datapoint[]>) rowProc.getRows();		    	    
//	    
//		for(Datapoint[] d : rows) {
//			System.out.println(Arrays.toString(d));
//		}	    
	    		
		return rowProc.getDataSets();		
	}
	
	/**
	 * A group of datapoint is said to be in the same dataset, if each and every datapoint in the group 
	 * all share at least one common property with the same value.
	 * 
	 * Therefore, for a pair of dataset to be equal, they must, first, pose same set of common properties
	 * with the same values and, second, all contained datapoints should hold identical value.  
	 * 
	 * Rules:
	 * 2 groups of datapoint with same schema (relations between datapoint), same property-value pairs, 
	 * with at least a different datapoint, are regarded as "similar" but NOT the same dataset.
	 * 2 groups of datapoint with same schema, different property-value pairs, are regarded as different dataset.
	 * 2 groups of datapoint with different schema, are always different dataset.
	 * 
	 * IMP an idea for speed-optimization using hashing 
	 * http://stackoverflow.com/questions/3341202/what-is-the-fastest-way-to-compare-two-sets-in-java
	 * 
	 * @return boolean whether the object is of type DataSet and is considered equal by definition.
	 */
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof DataSet)) return false; 
		
		DataSet ds = (DataSet) o;
		
		if(size() != ds.size()) return false;
		
		if(!commonProperties.equals(ds.getCommonProperties())) return false;
				
		return datapoints.equals(ds);
	}
	
	public int hashCode() {
		// TODO write proper hashcode for performance
		return 0;
	}
	
	/**
	 * Size of a dataset is number of datapoint it contains.
	 * @return
	 */
	public int size() {
		return datapoints.size();
	}

	public HashMap<String, String> getCommonProperties() {
		return commonProperties;
	}

	public void setCommonProperties(HashMap<String, String> commonProperties) {
		this.commonProperties = commonProperties;
	}

	public String getDatasetTitle() {
		return datasetTitle;
	}

	public void setDatasetTitle(String datasetTitle) {
		this.datasetTitle = datasetTitle;
	}

}
