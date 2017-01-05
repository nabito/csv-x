package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class CsvxCmd {	
	
	public static final String programName = "csvx";
	public static final String version = "0.10.0";

	//@Parameter(description = "input csv and csvx path respectively.", arity = 2)
	//private List<String> files = new ArrayList<>();

	@Parameter(names = { "-v", "--version" }, description = "Version.")
	private boolean isVersionOpt = false;

	@Parameter(names = { "-h", "--help" }, description = "Show some help and usage.", help = true)
	private boolean isHelpOpt = false;
	
	@Parameter(names = { "-log" }, description = "Log level (off, all,fatal,error,warn,info,debug,trace).")
	private String logLevel;	
	
	private static String getHelp(JCommander jc) {
		StringBuilder sb = new StringBuilder();
		jc.usage(sb);
		String nl = System.lineSeparator();
		sb.append("  Example:" + nl
				+ "    csvx -log=info validate input.csv schema.csvx" + nl
				+ "    csvx serialize input.csv schema.csvx" + nl
				+ "    csvx -log=off transform input.csv schema.csvx > output.ttl");
		return sb.toString();
	}

	public static void main(String... args) {
		
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
		
		try {
			jc.parse(args);
		} catch(MissingCommandException ex) {
			JCommander.getConsole().println("[Error] Command not support: " + ex.getMessage());
		} catch(ParameterException ex) {
			JCommander.getConsole().println("[Error] " + ex.getMessage());
		}
		
		SchemaProcessor sp = null;
		String csvPath = null;
		String csvxPath = null;
		Schema dSchema = null;
		
		if(jc.getParsedCommand() != null) {
			
			if(cmd.logLevel != null) {
				Level logLevel = null;
				switch(cmd.logLevel.toLowerCase()) {
				case "all":
					logLevel = Level.ALL;
					break;				
				case "fatal":
					logLevel = Level.FATAL;
					break;				
				case "error":
					logLevel = Level.ERROR;
					break;				
				case "warning":
					logLevel = Level.WARN;
					break;
				case "info":
					logLevel = Level.INFO;
					break;
				case "debug":
					logLevel = Level.DEBUG;
					break;
				case "trace":
					logLevel = Level.TRACE;
					break;
				case "off":
					logLevel = Level.OFF;
					break;
				default:
					JCommander.getConsole().println("[Error] Unknown log level: " + cmd.logLevel);
					System.exit(1);
				}
				assert(logLevel != null);
				sp = new SchemaProcessor(logLevel);
			} else {
				sp = new SchemaProcessor();
			}
			
			switch(jc.getParsedCommand()) {
			case "validate":				
				csvPath = vCmd.files.get(0);
				csvxPath = vCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});						
				break;
			case "serialize":			
				csvPath = sCmd.files.get(0);
				csvxPath = sCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});
				try {
					JCommander.getConsole().println(dSchema.serializeTtl());
				} catch (Exception e) {
					JCommander.getConsole().println("[Error] There's a problem serializing: ");
					e.printStackTrace();
				}
				break;
			case "transform":
				csvPath = tCmd.files.get(0);
				csvxPath = tCmd.files.get(1);									
				dSchema = sp.getDataSchema(csvPath, null, new String[] {csvxPath});			
				SchemaProcessor.generateRdfFromTemplate(dSchema);
				break;
			default:			
				assert(false) : "Must never enter here.";
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
