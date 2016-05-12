package com.dadfha.lod.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dadfha.lod.JSONMinify;
import com.github.jsonldjava.utils.JsonUtils;

/**
 * A CSV-X schema are metadata describing unique syntactic, structural, contextual, and semantic information 
 * for contents described in a CSV file. A data parsed w.r.t. a schema is regarded as a dataset. 
 * A CSV file may contain more than one dataset of the same or different schema.   
 * 
 * There can only be one CSV-X file per one CSV file, but a CSV-X may have more than one pattern (schema table).
 * By giving each table a name via '@name' meta property one can give parser a hint through CSV comment annotation 
 * to reduce trial-n-errors, hence increase parsing performance.
 *     
 * @author Wirawit
 */
public class Schema {
	
	// IMP move some methods to SchemaProcessor where it handles all parsing/controller logics for both schema and csv 
	// and it should allow for retrieval of Schema of a CSV-X file separately.
	
	public interface CellProcess {
		public void process(int row, int col, Object obj);
	}
	
	public class CellIndexRange {
		// -1 indicates uninitialized state or not available.
		int floor = -1;
		int ceiling = -1;
	}	
	
	/**
	 * ID of the schema with the same definition as in JSON-LD.
	 */
	private String id;
	
	/**
	 * SchemaTable stores metadata for tabular structure.
	 */
	private SchemaTable sTable = new SchemaTable();
	// TODO private Map<String, SchemaTable> sTables = new HashMap<String, SchemaTable>();
	// map between table name and schema table
	
	/**
	 * Associate the dataset with an RDF-based schema via context as in JSON-LD
	 */
	private List<Object> context; // TODO just use JSON-LD Java object!	
	
	/**
	 * Map of the value(s) to replace.
	 * Note that empty value has key of empty string ("").
	 */
	private Map<String, String> replaceValueMap;
	
	/**
	 * The value to substitute when the reading is missing. 
	 * Note that empty value ("") is not equal to missing value.
	 */
	private String missingValueFill;
	
	private List<String> targetCsvs;
	
	private String encoding;
	
	private String lang;
	
	private String delimiter;
	
	private String lineSeparator; 
	
	private String commentPrefix;
	
	private String quoteChar;
	
	private boolean header;
	
	private Integer headerRowCount;
	
	private boolean skipBlankRow;
	
	private Integer skipColumns;
	
	private boolean doubleQuote;
	
	private boolean skipInitialSpace;
	
	private Integer skipRows;
	
	private boolean trim;
	
	private boolean embedHeader; 
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();	
	
	/**
	 * we want to store a collection of referenced cells, which can be searched
	 * based on <row,col> index instantly, coz' during each csv cell iteration,
	 * we need to check if that cell is being referred in the schema so that we
	 * can selectively save the cell value for later use (e.g. variable-value
	 * substitution). if we're to preserve all csv value for later use, it may
	 * cause out-of-memory problem. if we're to re-parse the value in referred
	 * position, it may be well too expensive operation.
	 * 
	 * private Map<Integer, Map<Integer, String>> refCells; is good construct to
	 * store mapping of cell's <row, col> index with its value. However, nested
	 * collection may need a class wrapper to be able to smoothly operated.
	 * 
	 * Rather, using Map<IntPair, String> where IntPair is a class representing
	 * just <row, col> index with proper hashvalue may serves as more memory
	 * efficient option with less hassles in coding.
	 */
	private Map<CellIndex, String> refCells = new HashMap<CellIndex, String>();
	
	/**
	 * Mapping between variable name and schema entity. 
	 * IMP add var-value mapping as well..
	 */
	private Map<String, SchemaEntity> varMap = new HashMap<String, SchemaEntity>();	
	
	/**
	 * Check if a cell is referenced in the schema.
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean isCellRef(int row, int col) {
		return refCells.containsKey(new CellIndex(row, col));
	}
	
	/**
	 * This method won't save unreferenced cell.
	 * @param row
	 * @param col
	 * @param val
	 */
	public void saveRefCellVal(int row, int col, String val) {
		refCells.replace(new CellIndex(row,col), val);
	}

	public Schema(String filePath) {
		
		// read in the csv schema file and strip out all comments
		StringBuilder jsonStrBld = new StringBuilder(1000);
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
					id = (String) e.getValue();
					break;
				case "@targetcsvs":
					targetCsvs = new ArrayList<String>(); 
					break;
				case "@encoding":
					encoding = (String) e.getValue();
					break;
				case "@lang":
					lang = (String) e.getValue();
					break;
				case "@delimiter":
					delimiter = (String) e.getValue();
					break;
				case "@lineseparator":
					lineSeparator = (String) e.getValue();
					break;
				case "@commentprefix":
					commentPrefix = (String) e.getValue();					
					break;
				case "@quotechar":
					quoteChar = (String) e.getValue();
					break;
				case "@header":
					header = (boolean) e.getValue();
					// check if headerRowCount is already defined
					if(headerRowCount == null) {
						if(header == true) headerRowCount = 1;
						else headerRowCount = 0;
					}
					break;
				case "@headerrowcount":
					Integer hrc = (Integer) e.getValue();
					if(hrc < 0) throw new RuntimeException("@headerRowCount must be greater than 0.");
					headerRowCount = hrc;
					break;
				case "@doublequote":
					doubleQuote = (boolean) e.getValue();
					break;
				case "@skipblankrow":
					skipBlankRow = (boolean) e.getValue();
					break;
				case "@skipcolumns":
					Integer sc = (Integer) e.getValue();
					if(sc < 0) throw new RuntimeException("@skipColumns must be greater than 0.");
					skipColumns = sc; 
					break;
				case "@skipinitialspace":
					skipInitialSpace = (boolean) e.getValue();
					break;
				case "@skiprows":
					Integer sr = (Integer) e.getValue();
					if(sr < 0) throw new RuntimeException("@skipRows must be greater than 0.");
					skipRows = sr;
					break;
				case "@trim":
					trim = (boolean) e.getValue();
					break;					
				case "@embedheader":
					embedHeader = (boolean) e.getValue();
					break;
				case "@replacevaluemap":
					replaceValueMap = (LinkedHashMap<String, String>) e.getValue();
					// IMP check if deepcopy is needed  
					//replaceValueMap = new LinkedHashMap<String, String>( (LinkedHashMap<String, String>) e.getValue()); 
					break;
				case "@data":
					// TODO finish me! (and below)
					break;
				case "@property":
					break;
				default:
					if(key.startsWith("@cell")) {
						String marker = key.substring(5);
						processCellMarker(marker, (LinkedHashMap<String, String>) e.getValue());
						//processRows((ArrayList<Map<String, Object>>) e.getValue());	
					} else if(key.startsWith("@row")) {
						processRowMarker(key.substring(4), (LinkedHashMap<String, String>) e.getValue());
					} else if(key.startsWith("@")) {
						throw new RuntimeException("Unrecognized meta property: " + key);
					} else {
						// Others are add to extra properties map for later processing.						
						addProperty(key, e.getValue());	
					}
					break;
				}
			}  			
		    
		    
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		
	}
	
	/**
	 * Recognize \@cell syntax and create cell schema based on its properties.  
	 * @param marker
	 * @param cellProperty
	 */
	private void processCellMarker(String marker, Map<String, String> cellProperties) {	
				
		String s = marker.replaceAll("\\[|\\]", ""); // remove '[' and ']'
		s = s.replaceAll("\\s+", ""); // remove whitespaces
		String[] pos = s.split(","); // split value by ','
		if(pos.length != 2) throw new RuntimeException("Illegal format for @cell[RowRange,ColRange].");
		else {

			CellIndexRange rowRange = processCellIndexRange(pos[0]);
			CellIndexRange colRange = processCellIndexRange(pos[1]);
									
			// create cell representation with its properties for every intersection of row and col
			// and put into schema table!			
			forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o) -> cellCreation(i, j, (Map<String, String>) o) , cellProperties);
		}

	}
	
	/**
	 * Recognize \@row syntax and create row schema based on its properties
	 * @param marker
	 * @param rowProperties
	 */
	private void processRowMarker(String marker, Map<String, String> rowProperties) {
		String s = marker.replaceAll("\\[|\\]", ""); // remove '[' and ']'
		s = s.replaceAll("\\s+", ""); // remove whitespaces
		int rowNum = Integer.parseInt(s);
		SchemaRow sr = sTable.getRow(rowNum);
		if(sr == null) {
			sr = new SchemaRow(rowNum);
			sTable.addRow(sr);
		}
		processRowProps(sr, rowProperties);
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
	 * Create cell schema for [row, col], assign each cell cellProperty, and add to the schema table.
	 * @param row
	 * @param col
	 * @param cellProps
	 */
	private void cellCreation(int row, int col, Map<String, String> cellProps) {		
		Cell c = new Cell(row, col);
		processCellProps(c, cellProps);
		sTable.addCell(c);	
	}
	
	/**
	 * Loop through rows and columns specified in rowRange and colRange and perform cell process.
	 * @param rowRange
	 * @param colRange
	 * @param cp
	 * @param obj
	 */
	private void forMarkedRowAndCol(CellIndexRange rowRange, CellIndexRange colRange, CellProcess cp, Object obj) {
		int rowLimit = 0, colLimit = 0;
		if(rowRange.ceiling != -1) rowLimit = rowRange.ceiling;  
		else rowLimit = rowRange.floor;
		
		for(int i = rowRange.floor; i <= rowLimit; i++) {
			if(colRange.ceiling != -1) colLimit = colRange.ceiling;  
			else colLimit = colRange.floor;			
			
			for(int j = colRange.floor; j <= colLimit; j++) {				
				cp.process(i, j, obj);
			}			
		}		
	}
	
	/**
	 * Process special properties inside cell.
	 * @param cell
	 * @param cellProps
	 */
	private void processCellProps(Cell cell, Map<String, String> cellProps) {
		for(Entry<String, String> e : cellProps.entrySet()) {
			switch(e.getKey().toLowerCase()) {
			case "@name":
				// save variable name-value mapping
				varMap.put(e.getValue(), cell);
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
				// process the value
				String newLit = processLiteral(e.getValue());
				// add to user-defined properties
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
				// process the value
				String newLit = processLiteral(e.getValue());
				// add to user-defined properties
				sRow.addProperty(e.getKey(), newLit);
				break;
			}
		}
	}
	
	/**
	 * To validate a CSV cell against schema at its corresponding row, col.
	 * @param row
	 * @param col
	 * @param val
	 * @return boolean
	 */
	public boolean validate(int row, int col, String val) {
		
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
	
	/**
	 * In CSV-X the support for i18n and alternative string literal have been dropped. By allowing an expression of 
	 * any literal to have multiple language and possibly alternative terms requires that the data model accommodating
	 * them must has such a unique structure to hold i18n/alternative values as in RDF. 
	 * 
	 * Since one of CSV-X's objective is to be able to describe relations between cells in non-uniform CSV, in a generic 
	 * way, so it can be easily and flexibly mapped to an arbitrary data model. Therefore, making support for i18n and 
	 * alternative string literal by default (as in RDF literal) will impose such structure onto target data model or 
	 * making it less generic, hence more difficult, to convert it to other structure.
	 * 
	 * On the contrary, the support for i18n literal is still possible via '@lang' meta property where alternative string
	 * may be explicitly defined as another property like 'altTitle'.  
	 *    
	 * @param literal
	 * @return
	 */
	public String processLiteral(String literal) {		
		
		// identify referenced by parsing all occurrences of @cell[row, col] and save it for the time of CSV parsing
		String regEx = "(@cell)(\\[)([^,]+?)(,)([^,]+?)(\\])";
		
	    Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    Matcher m = p.matcher(literal);
	    while(m.find()) {
	        String rowEx = m.group(3);
	        String colEx = m.group(5);
	        
	        CellIndexRange rowRange = processCellIndexRange(rowEx);
	        CellIndexRange colRange = processCellIndexRange(colEx);
	        
	        forMarkedRowAndCol(rowRange, colRange, (int i, int j, Object o) -> saveRefCell(i, j), null);
	    }
	    	    
	    return literal;
	    
		// OPT do the same for {var.xx} but keep separated map of {var.xx} and cell's schema ID that 
	    // couldn't complete value replacement at the time. Then after the whole schema is processed
	    // we go though that list again to fill up value for referred variable(s).
		// This is done at CSV-X schema parsing stage.
		
	}
	
	/**
	 * Record a reference to cell.
	 * @param row
	 * @param col
	 */
	private void saveRefCell(int row, int col) {
		// since the actual value of cell is not known at this time, null is added.
		refCells.put(new CellIndex(row, col), null);
	}
	
	/**
	 * Replace cell's value if it's targeted in replace value map.
	 * @param val the original cell's value.
	 * @return String of the mapped value.
	 */
	public String replaceValue(String val) {		
		return replaceValueMap.get(val); 
	}

	Cell getCell(int row, int col) {
		return sTable.getCell(row, col);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}	
	
	public void addProperty(String key, Object val) {
		properties.put(key, val);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isRepeatingRow(int row) {
		return sTable.getRow(row).isRepeat();
	}
	
	public String getLineSeparator() {
		return lineSeparator;
	}
	
	/**
	 * Get empty value filling.
	 * @return the String value meant to replace empty value ("") or null if not specified.
	 */
	public String getEmptyValueFill() {
		return replaceValueMap.get("");
	}
	
	/**
	 * Missing value filling.
	 * @return String to replace missing value or null if not specified.
	 */
	public String getMissingValueFill() {
		return missingValueFill;
	}
	
	/**
	 * Get target CSV(s)
	 * @return list of target CSV(s) or null if none defined.
	 */
	public List<String> getTargetCsvs() {
		return targetCsvs;
	}

}
