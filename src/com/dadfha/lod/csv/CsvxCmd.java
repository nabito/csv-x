package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.config.ConfigurationFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class CsvxCmd {	
	
	public static final String programName = "csvx";
	public static final String version = "0.9.3";

	//@Parameter(description = "input csv and csvx path respectively.", arity = 2)
	//private List<String> files = new ArrayList<>();

	@Parameter(names = { "-v", "--version" }, description = "Version.")
	private boolean isVersionOpt = false;

	@Parameter(names = { "-h", "--help" }, description = "Show some help and usage.", help = true)
	private boolean isHelpOpt = false;
	
	private static String getHelp(JCommander jc) {
		StringBuilder sb = new StringBuilder();
		jc.usage(sb);
		sb.append("  Example:" + System.lineSeparator()
				+ "    csvx validate input.csv schema.csvx");
		return sb.toString();
	}

	public static void main(String... args) {
		// init log4j config
		ConfigurationFactory.setConfigurationFactory(new Log4jConfig());
		
		CsvxCmd cmd = new CsvxCmd();
		ValidateCmd vCmd = new ValidateCmd();
		SerializeCmd sCmd = new SerializeCmd();
		TransformCmd tCmd = new TransformCmd();
		JCommander jc = new JCommander(cmd);
		jc.setProgramName(programName);
		jc.setCaseSensitiveOptions(false);
		jc.setAllowAbbreviatedOptions(true);
		jc.addCommand("validate", vCmd);
		jc.addCommand("serialize", sCmd);
		jc.addCommand("transform", tCmd);
		jc.parse(args);
		
		SchemaProcessor sp = null;
		String csvPath = null;
		String csvxPath = null;
		Schema dSchema = null;
		
		if(jc.getParsedCommand() != null) {
			
			switch(jc.getParsedCommand()) {
			case "validate":
				sp = new SchemaProcessor();
				csvPath = vCmd.files.get(0);
				csvxPath = vCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});						
				break;
			case "serialize":			
				sp = new SchemaProcessor();
				csvPath = sCmd.files.get(0);
				csvxPath = sCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});
				JCommander.getConsole().println(dSchema.serializeTtl());
				break;
			case "transform":
				sp = new SchemaProcessor();
				csvPath = tCmd.files.get(0);
				csvxPath = tCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});			
				SchemaProcessor.generateRdfFromTemplate(dSchema);
				break;
			default:			
				JCommander.getConsole().println("Error: command not support.");
				break;
			} // switch			
			
		} else { // no command specified, check main options			
			if(cmd.isHelpOpt == true) {
				JCommander.getConsole().println(CsvxCmd.getHelp(jc));
			} else if(cmd.isVersionOpt == true) {
				JCommander.getConsole().println(version);
			} else { // nothing is specified, give some hints
				jc.usage();
				JCommander.getConsole().println("Type 'csvx -h' for full usage guide.");	
			}
		}
	} // main

} // CsvxCmd class

@Parameters(separators = "=", commandDescription = "Validate csv against csvx schema.")
class ValidateCmd {

	@Parameter(description = "input csv and csvx path respectively.", arity = 2)
	List<String> files = new ArrayList<>();

}

@Parameters(separators = "=", commandDescription = "Serialize csv into rdf according to model described in csvx schema.")
class SerializeCmd {

	@Parameter(description = "input csv and csvx path respectively.", arity = 2)
	List<String> files = new ArrayList<>();		

}

@Parameters(separators = "=", commandDescription = "Transform csv into rdf according to mapped template(s) in csvx.")
class TransformCmd {

	@Parameter(description = "input csv and csvx path respectively.", arity = 2)
	List<String> files = new ArrayList<>();

}	
