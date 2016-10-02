package com.dadfha.lod.csv.testng;

import org.testng.annotations.Test;

import com.dadfha.lod.csv.Log4jConfig;
import com.dadfha.lod.csv.Schema;
import com.dadfha.lod.csv.SchemaProcessor;

import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.testng.annotations.BeforeMethod;

public class SchemaProcessorTest {

	@BeforeMethod
	public void beforeMethod() {
		ConfigurationFactory.setConfigurationFactory(new Log4jConfig());		
	}

	@Test
	public void airP() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/airp.csvx"}; 		
		Schema dSchema = sp.getDataSchema("data/airp.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/airp.csv");
		
		//System.out.println(dSchema.serializeTtl());
		//SchemaProcessor.generateRdfFromTemplate(dSchema);		
	}
	
	@Test
	public void ukTelecom() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/uktelecom.csvx"};
		Schema dSchema = sp.getDataSchema("data/uktelecom.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/uktelecom.csv");		
	}
	
	@Test
	public void csvTriple() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/csvtriple.csvx"};
		Schema dSchema = sp.getDataSchema("data/csvtriple.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/csvtriple.csv");		
	}	
	
	@Test
	public void pdb() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/pdb.csvx"};
		Schema dSchema = sp.getDataSchema("data/pdb.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/pdb.csv");		
	}
	
	@Test
	public void ukstat() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/ukstat.csvx"};
		Schema dSchema = sp.getDataSchema("data/ukstat.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/ukstat.csv");		
	}	
	
	@Test
	public void thpetition() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/thpetition.csvx"};
		Schema dSchema = sp.getDataSchema("data/thpetition.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/thpetition.csv");		
	}
	
	@Test
	public void uscrime() {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/uscrime.csvx"};
		Schema dSchema = sp.getDataSchema("data/uscrime.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/uscrime.csv");		
	}	
	
}
