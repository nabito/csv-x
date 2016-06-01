package com.dadfha.lod.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dadfha.lod.JSONMinify;
import com.dadfha.mimamo.air.DataSet;
import com.github.jsonldjava.utils.JsonUtils;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class SchemaProcessor {
	
	@SuppressWarnings("serial")
	public class SchemaNotMatchedException extends Exception {
		public SchemaNotMatchedException() { super(); }
		  public SchemaNotMatchedException(String message) { super(message); }
		  public SchemaNotMatchedException(String message, Throwable cause) { super(message, cause); }
		  public SchemaNotMatchedException(Throwable cause) { super(cause); }		
	}
	
	public interface CellProcess {
		public void process(int row, int col, Object obj, SchemaTable sTable);
	}
	
	public class CellIndexRange {
		// -1 indicates uninitialized state or not available.
		int floor = -1;
		int ceiling = -1;
	}
	
	/**
	 * Class representing CSV-X schema parsing context. 
	 * 
	 * Rationale:
	 * You either pass the SchemaTable around, which make it easier to track where and what being
	 * passed but cluttering many method signatures or use global state vars for what schema/schemaTable 
	 * is being processed at the time. Though seems more convenient, but missing in an update of such 
	 * state var at a point would result in hard to trace bug.
	 * 
	 * Moreover, using parameter passing to identify processing context decouple the method from relying 
	 * on global variables to operate. On the other hand, without global state variables it's difficult 
	 * to know in real-time what's the processing context at the time. 
	 * 
	 * For CSV-X processor, there are needs to reuse method(s) with different context. Therefore, the method
	 * must stay generic, accepting context parameter(s) as needed. However, rather than passing arbitrary 
	 * number of variables around, it's better to wrap it as one "Context" variable where the states update 
	 * criteria are fixed and no global access is allowed to prevent false assumption on possibly staled context info.
	 * 
	 * @author Wirawit
	 */
	public static final class Context {
		private static final Context obj = new Context();
		private Context() {}
		public static Context getContext() {
			return obj;
		}
		Schema currSchema;
		SchemaTable currSchemaTable;
	}
	
	/**
	 * The only (static) context object as per SchemaProcessor object.
	 */
	public static final Context context = Context.getContext(); 
	
	/**
	 * Parser buffer size in KB unit (multiple of 1024 bytes) for disk IO performance. 
	 */
	public static final int CSV_PARSER_BUFFER_SIZE = 8 * 1024;
	
	/**
	 * RegEx for variable expression, {var} and {var.attr} 
	 */
	private static final String VAR_REGEX = "(\\{)([a-zA-Z_][a-zA-Z0-9_]*)(?:(\\.)([a-zA-Z_][a-zA-Z0-9_]*))?(\\})";
	
	/**
	 * RegEx for context variable expression, {row}, {col}, and {subrow}
	 */
	private static final String CONTEXT_VAR_REGEX = "(\\{)(row|col|subrow)(\\})";
	
	/**
	 * Setting flag whether or not to try all known schemas when parsing csv.
	 */
	private boolean tryAllSchemas = false;
	
	/**
	 * Each processor holds a set of schemas in memory for processing.
	 * IMP this could be scaled to a persistent repository. 
	 */
	private Map<String, Schema> schemas = new HashMap<String, Schema>();

	/**
	 * @return the tryAllSchemas
	 */
	public boolean isTryAllSchemas() {
		return tryAllSchemas;
	}

	/**
	 * @param tryAllSchemas the tryAllSchemas to set
	 */
	public void setTryAllSchemas(boolean tryAllSchemas) {
		this.tryAllSchemas = tryAllSchemas;
	}

	private CsvParserSettings getCsvParserSettings(Schema schema) {
		
		if(schema == null) throw new IllegalArgumentException("Schema schema is null.");
		
		CsvParserSettings settings = new CsvParserSettings();
		
		// line separator, MacOS uses '\r'; and Windows uses '\r\n'.
		if(schema.getLineSeparator() != null) settings.getFormat().setLineSeparator(schema.getLineSeparator());
		else settings.setLineSeparatorDetectionEnabled(true);
		
		// define empty value fill
		if(schema.getEmptyValueFill() != null) settings.setEmptyValue(schema.getEmptyValueFill());
		else settings.setEmptyValue(""); // or empty value will keep being empty string.
		
		// define missing value fill
		if(schema.getMissingValueFill() != null) settings.setNullValue(schema.getMissingValueFill());
		else {} // default to 'null' string when printing.
		
		// header extraction, anyone?
		settings.setHeaderExtractionEnabled(false);
		
		return settings;
	}
	
	
	public void loadSchemas(String[] schemaPaths) {
		for(String path : schemaPaths) {
			loadSchema(path);
		}
	}
	
	public void loadSchema(String schemaPath) {
		Schema s = parseCsvXSchema(schemaPath); 
		schemas.put(s.getId(), s);
	}
	
	/**
	 * Check if input csv ID matches with any of target csv in all schema(s).
	 * Note that this does not guarantee that the csv is valid for the matched schema.
	 * @param csvId
	 * @return String of schema ID that has target csv matched or null if none matched.  
	 */
	public String findMatchSchema(String csvId) {
		
		if(csvId == null || schemas.size() == 0) return null;
		
		// check for each schema
		for(Schema s : schemas.values()) {
			
			if(s.getTargetCsvs() != null) { // check if this schema has csv target defined
				
				for(String targetCsvId : s.getTargetCsvs()) { // for each target
					
					if(targetCsvId.equals(csvId)) { // if target csv matched csv id, return matched schema id
						System.out.println("Found csv id specified in a schema id : " + s.getId());
						return s.getId();
					} else continue; // if not matched (yet), continue checking the next target

				} // end loop through all csv target(s)
				
			} else { // there is no target csv defined in this schema
				continue; // check next schema
			}
			
		} // end for each schema	
		
		return null;
	}
	
	/**
	 * 
	 * @param csvPath
	 * @param schema
	 * @return Object of Java type sepcified by the schema or Dataset as default. Return null is error in parsing.
	 */
	public Object parseWithSchema(String csvPath, Schema schema) {

		// init the parser settings according to schema
		CsvParserSettings settings;
		if(schema == null) throw new IllegalArgumentException("Schema schema is null.");
		settings = getCsvParserSettings(schema);
		
		// IMP according to map datatype in schema, choose applicable row processor..
		// Ex. CSV - SSN OWL2 mapping 
		
		
		//DatapointRowProcessor rowProc;		
		// configure RowProcessor to process the values of each parsed row
		// rowProc = new DatapointRowSetProcessor();
		//rowProc = new DatapointRowProcessor(schema, this);
		//settings.setRowProcessor(rowProc);


		Reader csvReader;
		
		try {
			csvReader = new BufferedReader(new FileReader(csvPath), CSV_PARSER_BUFFER_SIZE);
		} catch (FileNotFoundException e) {
			System.err.println("Cannot find CSV file.");
			e.printStackTrace();
			return null;
		}
		
		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);
		
		
		// parsing context vars
	    String[] row;
	    int currRow = 0, currCol = 0;			
		String currVal = null;
		List<String[]> buffer = new ArrayList<String[]>();
				
		parser.beginParsing(csvReader);
		
		// for each schmea table within a schema, try parsing
		// IMP In case when there are more than one pattern (schema table) inside a CSV,  
		// CSV comment should have directive annotation to which schema table it's applicable to
		// to reduce trial'n'error.
		for(SchemaTable st : schema.getSchemaTables().values()) {
			
			try {				
				// match each incoming row of CSV against CSV-X schema
			    while ((row = parser.parseNext()) != null) {
			    	
			    	// buffering parsed value
			    	buffer.add(row);
			        
					// for each column
					for(currCol = 0; currCol < row.length; currCol++) {
						// get current cell value	
						currVal = row[currCol];
						
						// validate parsed CSV record against CSV-X schema properties				
						if(st.validate(currRow, currCol, currVal)) {
							
							// get cell schema
							Cell c = st.getCell(currRow, currCol);
							
							// for all property values, process literal for context {var}
							for(Entry<String,String> propEntry : c.getProperties().entrySet()) {
								String propName = propEntry.getKey();
								String propVal = propEntry.getValue();
								// TODO move all meta-prop into collection too..
								// also get subRow if this is a cell in subrow...
								propVal = processContextVarLiteral(propVal, currRow, currCol, null);	
							}

							// declare variable
							
							// create actual data model & save value
							
							// if successfully parsed until last cell for a schema table
								// release value buffer
								// break;								
						
						} else { // if validation failed, throw an error
							throw new SchemaNotMatchedException("Cannot matched with schema table: " + st);
						}	
						
					} // end for each column
			    	
			        currRow++;
			    } // end of  while ((row = parser.parseNext()) != null)..				
				
			} catch(SchemaNotMatchedException ex) {				
				// try another schema
				continue;
			}
		    
		    // stop parsing when done
		    parser.stopParsing();
			
			
		} // end for each schema table		


		// List<Datapoint[]> rows = (List<Datapoint[]>) rowProc.getRows();
		//
		// for(Datapoint[] d : rows) {
		// System.out.println(Arrays.toString(d));
		// }		
		
		return null;
	}
	
	public Set<DataSet> getDatasets(String csvPath, String csvId, String[] schemaPaths) { 
		
		// load schema(s)
		loadSchemas(schemaPaths);

		// check if CSV-X schema has its target CSV specified and is matched with the csv ID 
		String sId = (tryAllSchemas)?  null : findMatchSchema(csvId);
		
		Object data = null;
		
		if(sId != null) { // if matched schema ID is known, parse with the schema			
			Schema schema = schemas.get(sId);
			assert(schema != null) : "Impossible case of unrecognized schema ID : " + sId;
			data = parseWithSchema(csvPath, schema);			
		} else { 			
			// The processor loops through known schemas until it successfully parse the CSV.
			for(Schema schema : schemas.values()) {				
				data = parseWithSchema(csvPath, schema);
				if(data != null) break;
			} 			
		}
		
		if(data != null) {
			return (Set<DataSet>) data;
		} else {
			System.err.println("The parse bears no fruit: check out errors log.");
			return null;
		}
				
	}	
	
	/**
	 * Parse in CSV-X Schema file.
	 * @param schemaPath
	 * @return Schema
	 */
	public Schema parseCsvXSchema(String schemaPath) {
		
		Schema s = new Schema();
		
		// read in the csv schema file and strip out all comments
		StringBuilder jsonStrBld = new StringBuilder(1000);
		try (BufferedReader br = new BufferedReader(new FileReader(schemaPath))) {
		    String line;		    
		    while ((line = br.readLine()) != null) {
		    	jsonStrBld.append(JSONMinify.minify(line));
		    }
		    
			Map<String, Object> csvSchemaMap = (LinkedHashMap) JsonUtils.fromString(jsonStrBld.toString());
			Iterator<Map.Entry<String, Object>> it = csvSchemaMap.entrySet().iterator();		
			
			while(it.hasNext()) {
				Map.Entry<String, Object> e = (Map.Entry<String, Object>) it.next();				
				String key = e.getKey();
				switch(key.toLowerCase()) {		
				case "@id":
					s.setId((String) e.getValue());
					break;
				case "@targetcsvs":
					for(String csvId : (ArrayList<String>) e.getValue()) {
						s.addTargetCsv(csvId);	
					}
					break;
				case "@encoding":
					s.setEncoding((String) e.getValue());
					break;
				case "@lang":
					s.setLang((String) e.getValue());
					break;
				case "@delimiter":
					s.setDelimiter((String) e.getValue());
					break;
				case "@lineseparator":
					s.setLineSeparator((String) e.getValue());
					break;
				case "@commentprefix":
					s.setCommentPrefix((String) e.getValue());					
					break;
				case "@quotechar":
					s.setQuoteChar((String) e.getValue());
					break;
				case "@header":
					s.setHeader((boolean) e.getValue());
					// check if headerRowCount is not yet defined, else leave it as it is
					if(s.getHeaderRowCount() <= 0) {
						if(s.getHeader() == true) s.setHeaderRowCount(1);
						else s.setHeaderRowCount(0);
					}
					break;
				case "@headerrowcount":
					Integer hrc = (Integer) e.getValue();
					if(hrc < 0) throw new RuntimeException("@headerRowCount must be greater than 0.");
					s.setHeaderRowCount(hrc);
					break;
				case "@doublequote":
					s.setDoubleQuote((boolean) e.getValue());
					break;
				case "@skipblankrow":
					s.setSkipBlankRow((boolean) e.getValue());
					break;
				case "@skipcolumns":
					Integer sc = (Integer) e.getValue();
					if(sc < 0) throw new RuntimeException("@skipColumns must be greater than 0.");
					s.setSkipColumns(sc);
					break;
				case "@skipinitialspace":
					s.setSkipInitialSpace((boolean) e.getValue());
					break;
				case "@skiprows":
					Integer sr = (Integer) e.getValue();
					if(sr < 0) throw new RuntimeException("@skipRows must be greater than 0.");
					s.setSkipRows(sr);
					break;
				case "@trim":
					s.setTrim((boolean) e.getValue());
					break;					
				case "@embedheader":
					s.setEmbedHeader((boolean) e.getValue());
					break;
				case "@replacevaluemap":
					Map<String, String> rvm = s.getReplaceValueMap();
					rvm.putAll((LinkedHashMap<String, String>) e.getValue()); 
					break;
				case "@data":
					// TODO finish me! (and below)
					break;
				case "@property":
					break;
				case "@table":
					// process table internal structure.. e.g. cell, row, etc.
					SchemaTable sTable = new SchemaTable(null, s);
					processTableContent((Map<String, Object>) e.getValue(), sTable);
					s.addSchemaTable(sTable);					
					break;
				default:
					if(key.startsWith("@cell")) {
						// for cell definition outside table scope, it'll be added to 'default' schema table
						String marker = key.substring(5);
						processCellMarker(marker, (LinkedHashMap<String, String>) e.getValue(), s.getSchemaTable(Schema.DEFAULT_TABLE_NAME));
					} else if(key.startsWith("@row")) {
						// row definition outside table scope will also be added to 'default' schema table
						processRowMarker(key.substring(4), (LinkedHashMap<String, String>) e.getValue(), s.getSchemaTable(Schema.DEFAULT_TABLE_NAME));
					} else if(key.startsWith("@")) {
						System.err.println("Unrecognized meta property, ignoring key : " + key);
					} else {
						// Others are add to extra/user-defined properties map for later processing.
						s.addProperty(key, e.getValue());
					}
					break;
				}
			} // end while loop for 1st level schema	
		    
		    
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return s;
		
	}

	/**
	 * Process schema table contents and update the table. 
	 * @param map
	 * @param st
	 */
	private void processTableContent(Map<String, Object> map, SchemaTable st) {		
		Schema s = st.getParentSchema();
		for(Entry<String, Object> e : map.entrySet()) {
			String key = e.getKey().toLowerCase();
			switch(key) {
			case "@type":
				// TODO check if it's recognized type
				st.setType((String) e.getValue());
				break;
			case "@name":
				String tableName = (String) e.getValue();
				st.setName(tableName);
				//s.addVar(tableName, st); << for our policy now (2016/6/28) var must be declared at run-time as per dataset!?  
				break;
			case "@commonprops":
				Map<String, String> commonProps = (Map<String, String>) e.getValue();
				for(Entry<String, String> prop : commonProps.entrySet()) {		
					st.addCommonProp(prop.getKey(), prop.getValue());
				}				
				break;
			default:
				if(key.startsWith("@cell")) {
					String marker = key.substring(5);
					processCellMarker(marker, (Map<String, String>) e.getValue(), st);
				} else if(key.startsWith("@row")) {
					processRowMarker(key.substring(4), (LinkedHashMap<String, String>) e.getValue(), st);
				} else if(key.startsWith("@")) {
					System.err.println("Unrecognized meta property, ignoring key : " + key);
				} else {
					// Others are add to extra/user-defined properties map for later processing.
					st.addProperty(key, (String) e.getValue());
				}			
				break;
			} // end switch
		} // end foreach entry inside @table
	}
	
	/**
	 * Recognize \@cell syntax and create cell schema based on its properties.  
	 * @param marker
	 * @param cellProperty
	 */
	private void processCellMarker(String marker, Map<String, String> cellProperties, SchemaTable sTable) {	
				
		String s = marker.replaceAll("\\[|\\]", ""); // remove '[' and ']'
		s = s.replaceAll("\\s+", ""); // remove whitespaces
		String[] pos = s.split(","); // split value by ','
		if(pos.length != 2) throw new RuntimeException("Illegal format for @cell[RowRange,ColRange].");
		else {

			CellIndexRange rowRange = processCellIndexRange(pos[0]);
			CellIndexRange colRange = processCellIndexRange(pos[1]);
									
			// create cell representation with its properties for every intersection of row and col and put into schema table!			
			forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o, SchemaTable st) -> cellCreation(i, j, (Map<String, String>) o, st) , cellProperties, sTable);
		}

	}
	
	/**
	 * Recognize \@row syntax and create row schema based on its properties
	 * IMP implement processing of row index range to support repeating chunk of rows
	 * @param marker
	 * @param rowProperties
	 * @param sTable
	 */
	private void processRowMarker(String marker, Map<String, String> rowProperties, SchemaTable sTable) {
		String s = marker.replaceAll("\\[|\\]", ""); // remove '[' and ']'
		s = s.replaceAll("\\s+", ""); // remove whitespaces
		int rowNum = Integer.parseInt(s);
		SchemaRow sr = sTable.getRow(rowNum);
		if(sr == null) {
			sr = new SchemaRow(rowNum, sTable);
			sTable.addRow(sr);
		}
		processRowProps(sr, rowProperties);
	}	
	
	/**
	 * Create cell schema for [row, col], assign each cell cellProperty, and add to input schema table.
	 * @param row
	 * @param col
	 * @param cellProps
	 * @param sTable
	 */
	private void cellCreation(int row, int col, Map<String, String> cellProps, SchemaTable sTable) {		
		Cell c = new Cell(row, col, sTable);
		processCellProps(c, cellProps);
		sTable.addCell(c);	
	}	
	
	/**
	 * Process special properties inside cell.
	 * @param cell
	 * @param cellProps
	 */
	private void processCellProps(Cell cell, Map<String, String> cellProps) {
		Schema s = cell.getParentSchema();
		for(Entry<String, String> e : cellProps.entrySet()) {
			switch(e.getKey().toLowerCase()) {
			case "@name":
				String cellName = e.getValue();
				cell.setName(cellName);
				//s.addVar(cellName, cell); << for our policy now (2016/6/28) var must be declared at run-time as per dataset!?
				break;		
			case "@regex":
				cell.setRegEx(e.getValue());
				break;
			case "@type":
				String type = e.getValue();
				// TODO check if it's a recognized type (Datapoint or user-defined)
				cell.setType(type);
				break;
			case "@datatype":				
				String datatype = e.getValue();
				// TODO check if it's a recognized XML datatype.
				cell.setDatatype(datatype);
				break;
			case "@lang":
				cell.setLang(e.getValue());
				break;
			case "@value":
				cell.setValue(e.getValue());
				break;
			default:
				// process the value then add to user-defined properties
				cell.addProperty(e.getKey(), e.getValue());
				break;
			}
		}
	}
	
	private void processRowProps(SchemaRow sRow, Map<String, String> rowProps) {
		for(Entry<String, String> e : rowProps.entrySet()) {
			switch(e.getKey().toLowerCase()) {
			case "@repeattimes":
				sRow.setRepeatTimes(Integer.parseInt(e.getValue()));
				break;
			default:
				// process the value then add to user-defined properties
				sRow.addProperty(e.getKey(), e.getValue());
				break;
			}
		}
	}
	
	/**
	 * Process context variable inside a literal.
	 * There are 3 types of context variable:
	 * {row} 
	 * {col}
	 * {subrow} <--- you can't replace this at the time of schema parsing, since we still don't know 
	 * how many subrow there will be at run-time. But there's a need to declare var name using this context var
	 * for cells within a repeating row. Therefore, var declaration for cells within a repeating row,
	 * should be processed at CSV parsing time. 
	 * 
	 * At the moment, {var} replacement happens during CSV parsing time while its declaration (association
	 * a variable name with a schema entity) is done at schema parsing time so processor know what to replace
	 * during CSV parsing time. 
	 * 
	 * But now that some {var} declaration can't be done at schema parsing time for the stated reason, {var} 
	 * dereferenced to SERE will become more cumbersome too (~O(2n) at worst case performance).
	 * 
	 * However, let's say if we still want to keep SERE, we should NOT resolve {var} to SERE at CSV parsing time 
	 * just because we can. But a better solution is to regard {var} and SERE as the same thing, a.k.a. 
	 * a mean to refer to schema object, and treat with the same process and same level of importance. 
	 * They're just differ in expression.  
	 * 
	 * Let's take a look at data & process flow from schema to data model to utilization:  
	 * 
	 * Schema Model for a CSV --CSV parsed--> CSV-X Actual Data Model (with all vars & SERE intact BUT 
	 * context vars resolved) --Data Transform/Serialization(JSON/RDF/JSON-LD/CSVW,XML,etc.)/Utilization(Mapped Model)--> 
	 * Actual Data
	 * 
	 * We can see that Schema Model is like a "Blueprint" for resulting data model after CSV parsing. 
	 * If there are more than one dataset, however, there could be subtle differences in actual data model 
	 * because of "dynamic" repeating row definition. For ex. there could be more subrow in one dataset, hence
	 * more objects and more variables, than in other dataset. 
	 * 
	 * Therefore, variable-schema entity object mapping is ultimately the property of each resulting data model after
	 * CSV parsing!  This means that variable declaration (mapping) should be done at run-time for each dataset, 
	 * NOT at schema parsing time! and be saved at each schema file.
	 * 
	 * On a side note: 
	 * 
	 * What {var} & SERE actually is? A variable, pointer, macro, or symlink?
	 * 
	 * In short, {var} is a symlink if we want to resolve it to SERE and is a variable if we use it to refer to schema
	 * entity object directly. SERE is a schema entity object reference scheme, it's functionally equivalent to 
	 * variable without association with a name.
	 * 
	 * {var} & SERE are not pointer, because it's interpreted directly as object. It's not pointing to the "location" 
	 * of the object nor it can point to other pointer. 
	 * 
	 * {var} is NOT macro for SERE as long as it does not reduced itself to be just "find and replace" {var}-->SERE.
	 * In fact, another reason to drop the SERE substitution approach is that no one will get to see replaced SERE in
	 * the literal anyway, since all that matter is the "reference" to a schema object not expression. Moreover, 
	 * there's a need to check for validity of the whole literal again after substitution, further complicate the matter.
	 * 
	 * Though {var} can be think of as symlink for SERE, canonical representation of schema entity object,
	 * just like symlink is for file. So a {var} get resolved to an actual SERE when being used, rather than substituting 
	 * everything from the beginning. The problem of this approach is the hidden circular link which is hard to be 
	 * detected as often manifests in linux file system (copy, delete, etc.). In addition, it's also inefficient since 
	 * variable name is already associated with schema object internally when declared, translating it to SERE, which
	 * is merely just an expression, and then parse it back to match its target object is redundant.
	 * 
	 * Even when we treat {var} as variable pointing to same schema object as SERE, the problem of circular ref still
	 * persist. The code need to handle circular checking also has to taken into account SERE parsing.
	 * 
	 * In summary, both {var} and SERE can both achieve the same goal and they are mutually exclusive, 
	 * but {var} gives better comprehension of schema file if meaningful names are used.
	 *  
	 * Expressivity of SERE is more powerful in the sense that it provides reference to object without explicitly 
	 * declaring variable name. We can introduce syntax (e.g. \@cell[3.3,7]) to refer to a cell inside a subrow 
	 * of a repeating row without relying on variable name with context var of {subrow} and {col} inside its name.
	 * However, parsing SERE is quite a task to complete for now, and introducing both {var}
	 * & SERE will complicate the value dereferencing & circular reference checking.  
	 * 
	 * In the future, adding support for SERE is not a bad option.
	 *  
	 * @param literal
	 * @param currRow
	 * @param currCol
	 * @param currSubRow
	 * @return String of context {var} replaced literal.
	 */
	private String processContextVarLiteral(String literal, Integer currRow, Integer currCol, Integer currSubRow) {
		String retVal = literal;		
	    // detect context {var} expression	    
	    Pattern p = Pattern.compile(CONTEXT_VAR_REGEX, Pattern.DOTALL);
	    Matcher m = p.matcher(literal);
	    StringBuffer sb = new StringBuffer();
	    while(m.find()) {
	    	String varName = m.group(2);
	    	
    		switch(varName) {
    		case "row":
    			if(currRow != null) m.appendReplacement(sb, currRow.toString());
    			else throw new IllegalArgumentException("Referring to null value for currRow.");
    			break;
    		case "col":
    			if(currCol != null) m.appendReplacement(sb, currCol.toString());
    			else throw new IllegalArgumentException("Referring to null value for currCol.");
    			break;
    		case "subrow":
    			if(currSubRow != null) m.appendReplacement(sb, currSubRow.toString());
    			else throw new RuntimeException("Referring to null value for currSubRow.");
    			break;
    		default:
    			assert(false) : "non-context var shouldn't get matched here: " + varName; 
    			break;
    		}	    	

//	    	if(se.getClass().equals(Cell.class)) {
//	    		Cell cell =  (Cell) se;
//	    		switch(varName) {
//	    		case "row":
//	    			m.appendReplacement(sb, Integer.toString(cell.getRow()));
//	    			break;
//	    		case "col":
//	    			m.appendReplacement(sb, Integer.toString(cell.getCol()));
//	    			break;
//	    		case "subrow":
//	    			if(cell.isInRepeatingRow() && cell.getSubRow() != -1) m.appendReplacement(sb, Integer.toString(cell.getSubRow()));
//	    			else throw new RuntimeException("Referring to {subrow} in non-applicable cell context: " + cell);
//	    			break;
//	    		default:
//	    			assert(false) : "non-context var shouldn't get matched here: " + varName; 
//	    			break;
//	    		}
//	    		
//	    	} else if(se.getClass().equals(SchemaRow.class)) {
//	    		SchemaRow sr =  (SchemaRow) se;
//	    		switch(varName) {
//	    		case "row":
//	    			if(sr.getRowNum() != -1) m.appendReplacement(sb, Integer.toString(sr.getRowNum()));
//	    			else throw new RuntimeException("Row number of SchemaRow object is NOT initialized.");
//	    			break;
//	    		case "col":
//	    		case "subrow":
//	    			throw new RuntimeException("Reference to context var '" + varName + "' in non-applicable context: " + sr);
//	    		default:
//	    			assert(false) : "non-context var shouldn't get matched here: " + varName;
//	    			break;
//	    		}
//	    	} else {
//	    		assert(false) : "there's no context var literal to process for other type of entity: " + se;
//	    	}
	    
	    	m.appendTail(sb);
	    	retVal = sb.toString();
	    	m = p.matcher(retVal);
	    	sb.setLength(0);	    		    		    	
	    } // END OF  while(m.find()) {..	    	    
	    return retVal;
	}
	
	/**
	 * Check if there is variable expression, {var} or {var.attr}, inside the literal or not. 
	 * @param literal
	 * @return boolean
	 */
	public boolean hasVarInLiteral(String literal) {
	    Pattern p = Pattern.compile(VAR_REGEX, Pattern.DOTALL);	    
	    Matcher m = p.matcher(literal);
	    return m.find();
	}
	
	/**
	 * On i18n literal:
	 * 
	 * In CSV-X the native support for expression of i18n and alternative string literal have been dropped. 
	 * 
	 * 	i.e. "key" : [ { "en" : "test", "ja" : "テスト" }, { "en" : "Alt val", "ja" : "代わり" } ]
	 * 
	 * By allowing an expression of any literal to have multiple language and possibly alternative terms 
	 * requires that the data model accommodating them must has such a unique structure to hold i18n/alternative 
	 * values as in RDF. 
	 * 
	 * Since one of CSV-X's objective is to be able to describe relations between cells in non-uniform CSV, in a generic 
	 * way, so it can be easily and flexibly mapped to an arbitrary data model. Therefore, making support for i18n and 
	 * alternative string literal by default (as in RDF literal) will impose such structure onto target data model or 
	 * making it less generic, hence more difficult, to convert it to other structure.
	 * 
	 * On the contrary, the explicit declaration for i18n value is still supported via '@lang' meta-property where 
	 * alternative string may be specifically defined as another property like 'altTitle'.  
	 *    
	 * @param literal
	 * @param se
	 * @return String
	 */
	public String processLiteral(String literal, SchemaEntity se) {

		// (6/30) We won't save references to schema objects anymore, all literal containing SERE or {var} 
		// shouldn't get resolved to actual value until the time it's utilized/serialized.
		
		// identify referenced by parsing all occurrences of @cell[row, col] and save it for the time of CSV parsing
//	    SchemaTable st = se.getSchemaTable();
//	    String cellRegEx = "(@cell)(\\[)([^,]+?)(,)([^,]+?)(\\])";
//	    Pattern p = Pattern.compile(cellRegEx, Pattern.DOTALL);
//	    Matcher m = p.matcher(literal);
//	    while(m.find()) {
//	        String rowEx = m.group(3);
//	        String colEx = m.group(5);
//	        
//	        CellIndexRange rowRange = processCellIndexRange(rowEx);
//	        CellIndexRange colRange = processCellIndexRange(colEx);
//	        
//	        // since the actual value of cell is not known at this time, null is passed for value to saveRefCell()
//	        forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o, SchemaTable sTable) -> st.saveRefCell(i, j, null), null, st);
//	    }
	    	    
	    return literal;
		
	}
	
	/**
	 * Process Variable Expression, {var} and {var.prop} (from now refer to as {var}).
	 * 
	 * Substitute all {var} in the input literal with the actual value from mapped object of each variable.
	 * 
	 * This method must be called after CSV parsing phase where all variable declarations 
	 * and its value has been registered in the memory. It's supposed to be used during serialization or other 
	 * form of utilization that need to resolve variable value.
	 * 
	 * By resolving variable expression and replacing {var} with its actual value, the variable expression 
	 * will be dereferenced, meaning losing its reference to the object. Though the association between variable 
	 * name and schema entity object still persists in the memory, there is no way to update the dereferenced value 
	 * to reflects present value of the object or trace back what variable the value is resolved from.
	 * 
	 * However, one has a choice to just render (display) or utilize the resolved literal to user without overwriting 
	 * the original one, thus preserving reference to the object.
	 * 
	 * Note that {var} will be interpreted as {var.@value} within a literal.
	 * 
	 * The algorithm can be summarized as follows:
	 * 
	 * 1. identify {var} from literal
	 * 2. check RRS for circular ref, throw error if found
	 * 3. if not, dereference schema entity property value
	 * 4. do recursive call of this method for possible nested {var}
	 * 5. replace {var} with its ultimate value
	 * 6. go on process other {var} (if any)
	 * 7. after all {var} is deref, remove the calling {var} from RRS
	 * 8. return substituted literal	  
	 * 
	 * @param literal containing {var} expression.
	 * @param se schema entity of the literal being processed.
	 * @param propName property name of the literal being processed.
	 * @param rrs Recursive Ref Stack (RRS) of LinkedHashSet<String> storing SERE as String.
	 * @return String of {var} substituted by its recursively resolved actual value.
	 */
	public String processVarEx(String literal, SchemaEntity se, String propName, LinkedHashSet<String> rrs) {
		String retVal = literal;		
		SchemaTable st = se.getSchemaTable();
				
		// add calling schema property to the Recursive Ref Stack (RRS) of LinkedHashSet<String> where String = SERE
		if(rrs == null) rrs = new LinkedHashSet<String>();
		rrs.add(se.getRefEx() + "." + propName);
		
	    // detect {var} and {var.prop} expression	    
	    Pattern p = Pattern.compile(VAR_REGEX, Pattern.DOTALL);
	    Matcher m = p.matcher(literal);
	    StringBuffer sb = new StringBuffer();
	    while(m.find()) { // foreach {var}:
	    	String varName = m.group(2);
	    	String dot = m.group(3);
	    	String varProp = m.group(4);
	    	
    		// check if there is var definition declared
	    	if(!st.hasVar(varName)) { // if the var is not recognized, throw an error
	    		throw new RuntimeException("Reference to unknown variable: " + varName + " in scope of schema table: " + se.getSchemaTable());
	   		}
    		
	    	// Get mapped schema entity object
    		SchemaEntity varSe = st.getVarSchemaEntity(varName);
			assert (varSe != null) : "Variable must always associate with a Schema Entity.";
	    	
	    	// check for circular ref reference, e.g. A->B->A as well as higher level circular reference 
			// A->B->C->A, by keeping track of what properties of what schema entities have been referenced from 
    		// the beginning of {var} processing.			
	    	if(rrs.contains(varSe.getRefEx() + "." + varProp)) {
	    		throw new RuntimeException("Circular reference detected: {" + varName + "." + varProp + "} is already referenced in: " + rrs.toString());
	    	}			
			
	    	// dereference schema entity property value
			String propVal = null;
	    	if(dot == null || dot.equals("")) { // if there is no 'dot' in variable expression, a.k.a. just {var}
	    		// get variable property value (here is '@value')
	    		propVal = varSe.getValue();	    		
	    	} else { // {var.prop} processing	    			    		
	    		// get variable property value	    		
	    		propVal = varSe.getProperty(varProp);
	    	}
	    	
	    	// do recursive call of this method to dereferenced any available nested {var}
	    	propVal = processVarEx(propVal, varSe, varProp, rrs);
	    	
    		// replace {var} with its ultimate value
			m.appendReplacement(sb, propVal);	    	  	
	    	
	    	m.appendTail(sb);
	    	retVal = sb.toString();
	    	m = p.matcher(retVal);
	    	sb.setLength(0); 	
	    } // END OF.. while(m.find()) {
	    
	    // after all {var} is deref, remove the calling {var} from RRS
	    rrs.remove(se.getRefEx() + "." + propName);
	    
	    return retVal;
	}
	
	/**
	 * @deprecated SERE will be processed inside {var} expression in the future, e.g. {@cell[x,y]}.
	 * where it will be parsed to derive the object and property name it's describing.  
	 * 
	 * Process Schema Entity Reference Expression (SERE) e.g. (@table[name].@cell[x,y]).
	 * 
	 * Note that \@SchemaEntity without explicit property specified will be interpreted as 
	 * \@SchemaEntity.@value within a literal.
	 * 
	 * @param exp SERE.
	 * @param se schema entity being processed.
	 * @param propName property name holding {var} expression. 
	 * @return String
	 */
	public String processSerEx(String exp, SchemaEntity se, String propName, LinkedHashSet<String> rrs) {
	
		String retVal = null;
		if(rrs == null) rrs = new LinkedHashSet<String>();

		// there is no need to dereference SERE, which will "destroy the link" between values, until there's need
		// to serialize schema data model into an output. If not, an update to a referenced property in a schema entity
		// in a model won't be reflected anymore to dereferenced expression.
		
		// therefore, in CSV parsing stage, we only need to parse in CSV value. But in translation to other data model
		// or serializing to a data format, the SERE should be resolved using below algorithm.
		
		// Steps to resolve all SEREs, and {var} alike, to actual value
			// add calling schema property to the Recursive Ref Stack (RRS) of LinkedHashSet<String> where String = SERE
			// identify SERE (@table, @cell, etc.) from literal, foreach SERE:
				// make it a literal-ready form (expanded SERE with specific property)
				// check RRS for circular ref, throw error if found
				// if not, dereference schema entity property value
					// check if dereferenced value contains SERE,
						// if yes, then do recursive call of this fn, pass on RRS
						// if no, replace SERE with val
						// go on process other SERE, if any
			// after all SERE is deref, remove the calling SERE from RRS
			// return substituted literal.
		
		// add calling schema property to the Recursive Ref Stack (RRS) of LinkedHashSet<String> where String = SERE
		rrs.add(se.getRefEx() + "." + propName);
		
		// identify SERE (@table, @row, @cell, and @property) from literal, foreach SERE:
	    SchemaTable st = se.getSchemaTable();
	    String sereRegEx = "(?:@table\\[)([a-zA-Z_]+[a-zA-Z0-9_]*)(?:\\])|(?:@row\\[)(\\d+)(?:-(\\d+))?(?:\\])|(?:@cell\\[)(\\d+)(?:-(\\d+))?,(\\d+)(?:-(\\d+))?(?:\\])|(?:@property\\[)([a-zA-Z_]+[a-zA-Z0-9_]*)(?:\\])";
	    Pattern p = Pattern.compile(sereRegEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(exp);
	    while(m.find()) {
	    	String tableName = m.group(1);
	    	String rowIdxStart = m.group(2);
	    	String rowIdxEnd = m.group(3);
	    	String cellRowIdxStart = m.group(4);
	    	String cellRowIdxEnd = m.group(5);
	    	String cellColIdxStart = m.group(6);
	    	String cellColIdxEnd = m.group(7);
	    	String schemaPropName = m.group(8);
	    	
	        // processCellIndexRange() needs modification for this new regEx capture group
	        //CellIndexRange rowRange = processCellIndexRange(rowEx);
	        //CellIndexRange colRange = processCellIndexRange(colEx);
	    }
		
		return retVal;
	}
	
	/**
	 * Parse cell index range String into CellIndexRange object. 
	 * @param rangeEx
	 * @return CellIndexRange
	 */
	private CellIndexRange processCellIndexRange(String rangeEx) {
		
		rangeEx = rangeEx.replaceAll("\\s+", ""); // remove whitespaces just in case unprocessed string is passed		
		CellIndexRange cir = new CellIndexRange();
		
		if(rangeEx.indexOf("-") != -1) { // check if there is range span symbol '-'
			String[] range = rangeEx.split("-"); // then split range by '-'				
			if(range.length != 2) throw new RuntimeException("Illegal format for Range: must be in Floor-Ceiling format.");
			
			cir.floor = Integer.parseInt(range[0]);
			cir.ceiling = Integer.parseInt(range[1]);
			
			if(cir.floor < 0 || cir.ceiling < 0) throw new RuntimeException("Illegal format for Range: Floor value and Ceiling value cannot be negative.");
			if(cir.floor >= cir.ceiling) throw new RuntimeException("Illegal format for Range: Floor value >= Ceiling value.");
		} else { // if there is no range span symbol '-'
			cir.floor = Integer.parseInt(rangeEx);
			if(cir.floor < 0 || cir.ceiling < 0) throw new RuntimeException("Illegal format for Range: Floor value cannot be negative.");
			cir.ceiling = -1; // to ensure no one use ceiling value for this range.
		}
		
		return cir;
	}	
	
	/**
	 * Loop through rows and columns specified in rowRange and colRange and perform cell process for a schema table.
	 * @param rowRange
	 * @param colRange
	 * @param cp
	 * @param obj
	 * @param sTable
	 */
	private void forMarkedRowAndCol(CellIndexRange rowRange, CellIndexRange colRange, CellProcess cp, Object obj, SchemaTable sTable) {
		int rowLimit = 0, colLimit = 0;
		if(rowRange.ceiling != -1) rowLimit = rowRange.ceiling;  
		else rowLimit = rowRange.floor;
		
		for(int i = rowRange.floor; i <= rowLimit; i++) {
			if(colRange.ceiling != -1) colLimit = colRange.ceiling;  
			else colLimit = colRange.floor;			
			
			for(int j = colRange.floor; j <= colLimit; j++) {				
				cp.process(i, j, obj, sTable);
			}			
		}		
	}		
	
	public void parseCsvStream() {
		// IMP by leveraging this, we can implement CSV stream parsing!
		
//	    // Way to parse CSV directly from String
//		CsvSchemaParser parser = new CsvSchemaParser(new CsvSchemaParserSettings());
//	    parser.beginParsing(new BufferedReader(new StringReader("CSV in String format"), CsvSchemaParser.BUFFER_SIZE));
//	    String[] row;
//	    for(int i = 0; (row = parser.parseNext()) != null ; i++) {
//	        for(int j = 0; j < row.length; j++) {
//	        	// ???
//	        }
//	    }	    
//	    parser.stopParsing();		
	}
	

}
