package com.dadfha.mimamo.air;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dadfha.lod.csv.SchemaCell;
import com.dadfha.lod.csv.SchemaProcessor;
import com.dadfha.lod.csv.SchemaRow;
import com.dadfha.lod.csv.SchemaTable;

public class ExposureDose {
	
	private static Set<DataSet> dataSets;
	
	// declare dose (mg/kg/day), contaminate concentration (mg/m^3), intake rate (m^3/day), exposure factor (unitless), body weight (kg) 
	double d, c, ir, ef, bw;
	
	/**
	 * Calculation of exposure factor (ratio of how long one exposed to the pollution over a time period, unitless)
	 * @param frequency the frequency of exposure (days/year)
	 * @param duration the exposure duration (years)
	 * @param averagingTime the time period one like to average over (days)
	 * @return
	 */
	public double exposureFactor(double frequency, double duration, double averagingTime) {
		return (frequency * duration) / averagingTime;
	}
	
	/**
	 * Calculation of air inhaling exposure dose 
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
	
	public void dumpDatasets() {
		for(DataSet ds : dataSets) {
			Map<String, Datapoint> datapoints = ds.getDatapoints();
			for(Entry<String, Datapoint> e : datapoints.entrySet()) {
				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
	}
	
	public static void dumpSchemaTables(List<SchemaTable> dataTables) {
		for(SchemaTable dTable : dataTables) {
			System.out.println("Table : " + dTable);
			for(Map.Entry<Integer, SchemaRow> rowsEnt : dTable.getSchemaRows().entrySet()) {
				Integer rowNum = rowsEnt.getKey();
				SchemaRow dRow = rowsEnt.getValue();
				System.out.print("Row: " + rowNum);
				//System.out.println(dRow);
				for(Map.Entry<Integer, SchemaCell> cellEnt : dRow.getSchemaCells().entrySet()) {
					Integer colNum = cellEnt.getKey();
					SchemaCell dCell = cellEnt.getValue();
					//System.out.print("Col: " + colNum + " ");
					System.out.print(dCell.getValue() + ", ");
				}
				System.out.println();
			}
		}
	}	
	
	public void computeMatch() {
		
	}
	

	public static void main(String[] args) {
		
		System.out.println("Hello Exposure Dose Equation");
		
		ExposureDose ed = new ExposureDose();
		
		SchemaProcessor sp = new SchemaProcessor();
		//String[] schemaPaths = {"airp-csvx1.json", "airp-csvx2.json"};
		String[] schemaPaths = {"airp-csvx-var-only.json"};
		//dataSets = sp.getDatasets("oxidant.csv", null, schemaPaths);
		List<SchemaTable> result = sp.getDatasets("oxidant.csv", null, schemaPaths);
		System.out.println(result);
		
		dumpSchemaTables(result);
		
		//ed.dumpDatasets();
		
		//ed.computeMatch();
	}

}
