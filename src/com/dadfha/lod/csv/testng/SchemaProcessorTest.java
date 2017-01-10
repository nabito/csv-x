package com.dadfha.lod.csv.testng;

import org.testng.annotations.Test;

import com.dadfha.lod.csv.Log4jConfig;
import com.dadfha.lod.csv.Schema;
import com.dadfha.lod.csv.SchemaProcessor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.testng.annotations.BeforeMethod;

public class SchemaProcessorTest {

	@BeforeMethod
	public void beforeMethod() {
		ConfigurationFactory.setConfigurationFactory(new Log4jConfig());		
	}
	
	@Test
	public void testApplyRdfTemplateWithBase() throws Exception {	
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/test_template_with_base.csvx"};
		Schema dSchema = sp.getDataSchema("data/test_template.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/test_template.csv");	
		SchemaProcessor.generateRdfFromTemplate(dSchema);
	}	
	
	@Test
	public void testApplyRdfTemplate() throws Exception {	
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/test_template.csvx"};
		Schema dSchema = sp.getDataSchema("data/test_template.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/test_template.csv");	
		SchemaProcessor.generateRdfFromTemplate(dSchema);
	}

	@Test
	public void airP() throws Exception {
		SchemaProcessor sp = new SchemaProcessor();
		String[] schemaPaths = {"data/airp.csvx"}; 		
		Schema dSchema = sp.getDataSchema("data/airp.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/airp.csv");		
		System.out.println(dSchema.serializeTtl());
		SchemaProcessor.generateRdfFromTemplate(dSchema);		
	}
	
	@Test
	public void ukTelecom() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/uktelecom.csvx"};
		Schema dSchema = sp.getDataSchema("data/uktelecom.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/uktelecom.csv");
		System.out.println(dSchema.serializeTtl());
	}
	
	@Test
	public void csvTriple() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/csvtriple.csvx"};
		Schema dSchema = sp.getDataSchema("data/csvtriple.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/csvtriple.csv");
		System.out.println(dSchema.serializeTtl());
	}	
	
	@Test
	public void pdb() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/pdb.csvx"};
		Schema dSchema = sp.getDataSchema("data/pdb.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/pdb.csv");
		System.out.println(dSchema.serializeTtl());
	}
	
	@Test
	public void ukstat() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/ukstat.csvx"};
		Schema dSchema = sp.getDataSchema("data/ukstat.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/ukstat.csv");
		System.out.println(dSchema.serializeTtl());
	}	
	
	@Test
	public void thpetition() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/thpetition.csvx"};
		Schema dSchema = sp.getDataSchema("data/thpetition.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/thpetition.csv");
		System.out.println(dSchema.serializeTtl());
	}
	
	@Test
	public void uscrime() throws Exception {
		SchemaProcessor sp = new SchemaProcessor(true);
		String[] schemaPaths = {"data/uscrime.csvx"};
		Schema dSchema = sp.getDataSchema("data/uscrime.csv", null, schemaPaths);
		if(dSchema == null) throw new RuntimeException("Error Processing: " + "data/uscrime.csv");
		System.out.println(dSchema.serializeTtl());
	}	
	
}
