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
		SchemaProcessor sp = new SchemaProcessor();
		String[] schemaPaths = {"data/airp.csvx"}; 		
		Schema dSchema = sp.getDataSchema("data/airp.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/airp.csv");
		
		//System.out.println(dSchema.serializeTtl());
		//SchemaProcessor.generateRdfFromTemplate(dSchema);		
	}
	
	@Test
	public void ukTelecom() {
		SchemaProcessor sp = new SchemaProcessor();
		String[] schemaPaths = {"data/uktelecom.csvx"};
		Schema dSchema = sp.getDataSchema("data/uktelecom.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/uktelecom.csv");		
	}
	
	@Test
	public void csvTriple() {
		SchemaProcessor sp = new SchemaProcessor();
		String[] schemaPaths = {"data/csvtriple.csvx"};
		Schema dSchema = sp.getDataSchema("data/csvtriple.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/csvtriple.csv");		
	}	
	
}
