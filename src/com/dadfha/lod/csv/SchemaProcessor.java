package com.dadfha.lod.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

		// configure RowProcessor to process the values of each parsed row.
		DatapointRowSetProcessor rowProc = new DatapointRowSetProcessor(schema);
		settings.setRowProcessor(rowProc);

		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);

		// the 'parse' method will parse the file and delegate each parsed row to the RowProcessor
		try {
			parser.parse(new BufferedReader(new FileReader(csvPath), CSV_PARSER_BUFFER_SIZE));
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find CSV file.");
			e.printStackTrace();
			return null;
		}

		// List<Datapoint[]> rows = (List<Datapoint[]>) rowProc.getRows();
		//
		// for(Datapoint[] d : rows) {
		// System.out.println(Arrays.toString(d));
		// }		
		
		return rowProc.getData();
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
			} 			
		}
		
		if(data != null) {
			return (Set<DataSet>) data;
		} else {
			System.err.println("The parse bears no fruit: check out errors log.");
			return null;
		}
				
	}	
	
	
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
				s.addVar(tableName, st);
				break;
			case "@commonprops":
				Map<String, String> commonProps = (Map<String, String>) e.getValue();
				for(Entry<String, String> prop : commonProps.entrySet()) {		
					st.addCommonProp(prop.getKey(), processLiteral(prop.getValue(), st));
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
				// save variable name-value mapping
				s.addVar(e.getValue(), cell);
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
				String newLit = processLiteral(e.getValue(), cell);
				cell.addProperty(e.getKey(), newLit);
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
				String newLit = processLiteral(e.getValue(), sRow);
				sRow.addProperty(e.getKey(), newLit);
				break;
			}
		}
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

		Schema s = se.getParentSchema();
		
		// identify referenced by parsing all occurrences of @cell[row, col] and save it for the time of CSV parsing
	    SchemaTable st = se.getSchemaTable();
	    String cellRegEx = "(@cell)(\\[)([^,]+?)(,)([^,]+?)(\\])";
	    Pattern p = Pattern.compile(cellRegEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(literal);
	    while(m.find()) {
	        String rowEx = m.group(3);
	        String colEx = m.group(5);
	        
	        CellIndexRange rowRange = processCellIndexRange(rowEx);
	        CellIndexRange colRange = processCellIndexRange(colEx);
	        
	        // since the actual value of cell is not known at this time, null is passed for value to saveRefCell()
	        forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o, SchemaTable sTable) -> st.saveRefCell(i, j, null), null, st);
	    }
	    	    
	    return literal;
		
	}
	
	/**
	 * Replace any variable expression {var} and {var.prop} in literal with expanded form of Schema Entity 
	 * Reference Expression (SERE) e.g. (@table[name].@cell[x,y]). 
	 * 
	 * This method is supposed to be called during CSV parsing phase where all variable declarations has 
	 * been registered in memory. It must be called before processing of SERE since a variable itself 
	 * represents a schema entity and will be translated to SERE.
	 * Note that {var} will be interpreted as {var.@value} within a literal.
	 * 
	 * @param exp literal containing {var} expression
	 * @param se schema entity being processed.
	 * @return String of {var} substituted by SERE.
	 */
	public String processVarEx(String exp, SchemaEntity se) {
		String retVal = exp;
		Schema s = se.getParentSchema();
	    // detect {var} and {var.prop} expression
	    String varRegEx = "(\\{)([a-zA-Z_]+[a-zA-Z0-9_]*)(?:(\\.)([a-zA-Z_]+[a-zA-Z0-9_]*))?(\\})";
	    Pattern p = Pattern.compile(varRegEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(exp);
	    StringBuffer sb = new StringBuffer();
	    while(m.find()) {
	    	String varName = m.group(2);
	    	String dot = m.group(3);
	    	String varProperty = m.group(4);
	    	
    		// check if there is var definition declared or it's a special context var
	    	switch(varName) {
	    	case "row":
	    		// TODO replace with context row/col
	    		// this has to do with type of SchemaEntity
	    		break;
	    	case "subrow":
	    		break;
	    	case "col":
	    		break;
	    	default:
	    		if(!s.hasVar(varName)) { // if the var is not recognized, throw an error
	    			throw new RuntimeException("Reference to unknown variable: " + varName);
	    		}	    		
	    		break;
	    	}
    		
    		SchemaEntity varSe = s.getVarSchemaEntity(varName);
			assert (varSe != null) : "Variable must always mapped with a Schema Entity.";
	    	
	    	if(dot == null || dot.equals("")) { // if there is no 'dot', just process variable name {var}
	    		// replace this {var} with Schema Entity Reference Expression (SERE)    			
    			m.appendReplacement(sb, varSe.getRefEx() + ".@value");
	    	} else { // {var.prop} processing
	    		
	    		
	    		// get variable property value	    		
	    		//String propVal = varSe.getProperty(varProperty);	    		
	    		// Check only 1st level circular reference A->B->A though higher level circular reference is also
	    		// prohibited, says A->B->C->A, but it's become costly to check in a recursive chain.
	    		// ...This should be done at SERE processing step after all var<->SERE substitution is done.
//	    		String referrerVarName = se.getName();
//	    		
//	    		if((propName.equals("@value") && propVal.contains("{" + referrerVarName + "}")) || 
//	    				(propVal.contains("{" + referrerVarName + "." + propName + "}")) ) {
//	    			throw new RuntimeException("The literal value of the referenced property MUST NOT contain {var} reference to the referrer property to prevent infinite loop.");
//	    		}	    		
	    		
	    		
	    		// replace {var.prop} with SERE
	    		m.appendReplacement(sb, varSe.getRefEx() + "." + varProperty);
	    	}	    	
	    	
	    	m.appendTail(sb);
	    	retVal = sb.toString();
	    	m = p.matcher(retVal);
	    	sb.setLength(0);	    	
	    }
	    return retVal;
	}
	
	/**
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
		
		// Steps to resolve all SEREs to actual value
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
	    	
	        
	        CellIndexRange rowRange = processCellIndexRange(rowEx);
	        CellIndexRange colRange = processCellIndexRange(colEx);
	        
	        // since the actual value of cell is not known at this time, null is passed for value to saveRefCell()
	        forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o, SchemaTable sTable) -> st.saveRefCell(i, j, null), null, st);
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
	
	/**
	 * To validate a CSV cell against schema at its corresponding row, col.
	 * @param row
	 * @param col
	 * @param val
	 * @param sTable
	 * @return
	 */
	public boolean validate(int row, int col, String val, SchemaTable sTable) {
		
		// use 'default' table if none is provided.
		if(sTable == null) throw new IllegalArgumentException("sTable must not be null.");
		
		Cell c = sTable.getCell(row, col);
		
		if(c == null) {
			System.err.println("Error: Dimension Mismatched - There is no schema definition at: [" + row + "," + col + "]"); 
			return false;
		}
		
		// TODO check datatype and restrictions according to XML Schema Datatype 1.1 (http://www.w3.org/TR/xmlschema11-2/)
		// also the syntax for constraints must be referred from CSVW specs
		String datatype = c.getDatatype();
		if(datatype != null) {			
			switch(datatype) {
			// mapping between XML datatype and Java datatype in defined in JAXB standard (http://docs.oracle.com/javaee/5/tutorial/doc/bnazq.html)
			case "string":
				break;			
			case "integer":
				break;
			case "int":
				break;
			case "long":
				break;
			case "short":
				break;
			case "decimal":
				break;
			case "float":
				break;
			case "double":
				break;
			case "boolean":
				break;
			case "byte":
				break;
			case "QName":
				break;
			case "dateTime":
				break;
			case "base64Binary":
				break;
			case "hexBinary":
				break;
			case "unsignedInt":
				break;
			case "unsignedShort":
				break;
			case "unsignedByte":
				break;
			case "time":
				break;
			case "date":
				break;
			case "g":
				break;
			case "anySimpleType":
				break;
			case "duration":
				break;
			case "NOTATION":
				break;				
			default:
				throw new RuntimeException("Unsupported datatype: " + datatype);
			}
			// TODO later import XML datatype API and do the validation..
			// our key point is to design CSV-X schema syntax to be able to express all datatype & restrictions
			// then translate that into XML model for native-XML validation.
			
		}
		
		//String reEmpty = "[^\\S\r\n]*?"; // Regular Expression for any whitespace characters except newline
		
		// TODO check empty cell too.. back to creation of empty cell.
		
		// validate parsed CSV record against CSV-X cell schema's regular expression.
		String regEx = c.getRegEx();
		if(regEx != null) {
		    Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    Matcher m = p.matcher(val);
		    if(!m.find()) {
		    	System.err.println("Error: Regular Expression Mismatched at: [" + row + "," + col + "]");
		    	return false;			
		    }
		}
		
		return true;
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
