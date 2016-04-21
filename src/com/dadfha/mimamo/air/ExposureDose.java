package com.dadfha.mimamo.air;

import java.util.List;
import java.util.Set;

import com.dadfha.lod.csv.CsvSchemaParser;
import com.dadfha.lod.csv.CsvSchemaParserSettings;
import com.dadfha.lod.csv.TokyoAirRowListProcessor;
import com.univocity.parsers.csv.CsvParser;

public class ExposureDose {
	
	private List<DataSet> dataSets;
	
	// declare dose (mg/kg/day), contaminate concentration (mg/m^3), intake rate (m^3/day), exposure factor (unitless), body weight (kg) 
	double d, c, ir, ef, bw;
	
	/**
	 * Calculation of exposure factor (ratio of how long one exposed to the pollution over a time period, unitless)
	 * @param frequency frequency of exposure (days/year)
	 * @param duration exposure duration (years)
	 * @param averagingTime the time period one like to average over (days)
	 * @return
	 */
	public double exposureFactor(double frequency, double duration, double averagingTime) {
		return (frequency * duration) / averagingTime;
	}
	
	/**
	 * Calculation of air inhalation exposure dose 
	 * @param concentration the contaminate concentration (mg/m^3)
	 * @param intakeRate intake rate (m^3/day)
	 * @param exposureFactor exposure factor
	 * @param bodyWeight body weight (kg)
	 * @return
	 */
	public double airInhaleExposureDose(double concentration, double intakeRate, double exposureFactor, double bodyWeight) {
		return (concentration * intakeRate * exposureFactor) / bodyWeight;
	}
	
	
	public void estimateCumulativeDose() {
		
		// retrieve travel routes history (e.g. from Google map)
		// search for opendata on air pollution by area
		// calculate doses received from each traveled area 
			// may also taking "activity <-> air intake rate" into account
			// also intake fraction based on location property
			// also other kind of pollutions (e.g. smoke, paint, etc.) sensed by indoor/other sensors
			// also may refer to self-medication/medical KB for personalized health advise
			// personalization could be extended to cover asthma, and other respiratory related syndromes/allergies 
		
	}
	
	public void loadDatasets() {
		
		// prepare parser for a specific dataset schema (for now)
	    //CsvParserSettings settings = new CsvParserSettings();
		CsvSchemaParserSettings settings = new CsvSchemaParserSettings();
	    //the file used in the example uses '\n' as the line separator sequence.
	    //the line separator sequence is defined here to ensure systems such as MacOS and Windows
	    //are able to process this file correctly (MacOS uses '\r'; and Windows uses '\r\n').
	    
	    //settings.getFormat().setLineSeparator("\n");
	    settings.setLineSeparatorDetectionEnabled(true);
	    settings.setHeaderExtractionEnabled(false);

	    // configure the parser to use a RowProcessor to process the values of each parsed row.
	    //DatapointRowProcessor rowProc = new DatapointRowProcessor();
	    TokyoAirRowListProcessor rowProc = new TokyoAirRowListProcessor();
	    settings.setRowProcessor(rowProc);
	    //settings.setNullValue("");
	    
	    // creates a CSV parser
	    //CsvParser parser = new CsvParser(settings);
	    CsvParser parser = new CsvSchemaParser(settings);
		
		
	    // readin datasets in one of the (csv, rdf, owl) format
		dataSets = DataSet.loadCsvData("oxidant.csv", parser, rowProc);
		
	}
	
	public void dumpDatasets() {
		for(DataSet ds : dataSets) {
			Set<Datapoint> datapoints = ds.getDatapoints();
			for(Datapoint dp : datapoints) {
				System.out.println(dp);
			}
		}
	}
	
	public void computeMatch() {
		
	}
	

	public static void main(String[] args) {
		
		System.out.println("Hello Exposure Dose Equation");
		
		ExposureDose ed = new ExposureDose();
		
		ed.loadDatasets();
		
		ed.dumpDatasets();
		
		ed.computeMatch();
	}

}
