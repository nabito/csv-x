package com.dadfha.lod.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dadfha.lod.JSONMinify;
import com.dadfha.lod.csv.HeaderField.ApplyScope;
import com.github.jsonldjava.utils.JsonUtils;

/**
 * A CSV-X schema are metadata describing unique syntactic, structural, contextual, and semantic information 
 * for contents described in a CSV file. A data parsed w.r.t. a schema is regarded as a dataset. 
 * A CSV file may contain more than one dataset of the same schema, or more than one schema for multiple dataset.   
 *     
 * @author Wirawit
 */
public class Schema {
	
	public class CellIndex {
		// Integer.MAX_VALUE represents the last index a.k.a. '*' symbol
		// -1 indicates uninited state which can be used to determine is there is subIndex or not.
		int index; 
		int subIndex;
	}
	
	public class CellIndexRange {
		CellIndex floor = new CellIndex();
		CellIndex ceiling = new CellIndex(); // should be set to null when not using.
	}	
	
	/**
	 * ID of the schema with the same definition as in JSON-LD.
	 */
	private String id;
	
	/**
	 * SchemaTable stores metadata for tabular structure.
	 */
	private SchemaTable sTable = new SchemaTable();
	
	/**
	 * Associate the dataset with an RDF-based schema via context as in JSON-LD
	 */
	List<Object> context; // TODO just use JSON-LD Java object!	
	
	/**
	 * Map of the value(s) to replace. It'll be used by the parser.
	 */
	Map<String, String> replaceValueMap;
	
	private String delimiter;
	
	private List<String> lineTerminators; 
	
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
		
	public Schema(String filePath) {
		
		// read in the csv schema file and strip out all comments
		StringBuilder jsonStrBld = new StringBuilder(1000);
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;		    
		    while ((line = br.readLine()) != null) {
		    	jsonStrBld.append(JSONMinify.minify(line));
		    }
		    
			Map csvSchemaMap = (LinkedHashMap) JsonUtils.fromString(jsonStrBld.toString());
			Iterator<Map.Entry<String, Object>> it = csvSchemaMap.entrySet().iterator();			
			
			while(it.hasNext()) {
				Map.Entry<String, Object> e = (Map.Entry<String, Object>) it.next();				
				String key = e.getKey();
				switch(key) {				
				case "@delimiter":
					delimiter = (String) e.getValue();
					break;
				case "@lineTerminators":
					lineTerminators = (ArrayList<String>) e.getValue();
					break;
				case "@commentPrefix":
					commentPrefix = (String) e.getValue();					
					break;
				case "@quoteChar":
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
				case "@headerRowCount":
					Integer hrc = (Integer) e.getValue();
					if(hrc < 0) throw new RuntimeException("@headerRowCount must be greater than 0.");
					headerRowCount = hrc;
					break;
				case "@doubleQuote":
					doubleQuote = (boolean) e.getValue();
					break;
				case "@skipBlankRow":
					skipBlankRow = (boolean) e.getValue();
					break;
				case "@skipColumns":
					Integer sc = (Integer) e.getValue();
					if(sc < 0) throw new RuntimeException("@skipColumns must be greater than 0.");
					skipColumns = sc; 
					break;
				case "@skipInitialSpace":
					skipInitialSpace = (boolean) e.getValue();
					break;
				case "@skipRows":
					Integer sr = (Integer) e.getValue();
					if(sr < 0) throw new RuntimeException("@skipRows must be greater than 0.");
					skipRows = sr;
					break;
				case "@trim":
					trim = (boolean) e.getValue();
					break;					
				case "@embedHeader":
					embedHeader = (boolean) e.getValue();
					break;
				case "@replaceValueMap":
					replaceValueMap = (LinkedHashMap<String, String>) e.getValue();
					// IMP check if deepcopy is needed  
					//replaceValueMap = new LinkedHashMap<String, String>( (LinkedHashMap<String, String>) e.getValue()); 
					break;					
				default:
					if(key.startsWith("@cell")) {
						String marker = key.substring(5);
						processCellMarker(marker, (LinkedHashMap<String, String>) e.getValue());
						//processRows((ArrayList<Map<String, Object>>) e.getValue());	
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
	private void processCellMarker(String marker, Map<String, String> cellProperty) {	
				
		String s = marker.replaceAll("\\[|\\]", ""); // remove '[' and ']'
		s = s.replaceAll("\\s+", ""); // remove whitespaces
		String[] pos = s.split(","); // split value by ','
		if(pos.length != 2) throw new RuntimeException("Illegal format for @cell[RowRange,ColRange].");
		else {

			CellIndexRange rowRange = processCellIndexRange(pos[0]);
			CellIndexRange colRange = processCellIndexRange(pos[1]);
									
			// create cell representation with its properties for every intersection of row and col
			// and put into schema table!
			forEveryRowAndCol(rowRange, colRange, cellProperty);
						
			
		}

	}
	
	private void forEveryRowAndCol(CellIndexRange rowRange, CellIndexRange colRange, Map<String, String> cellProperty) {
		
		int rowLimit = 0, colLimit = 0;
		if(rowRange.ceiling != null) rowLimit = rowRange.ceiling.index;  
		else rowLimit = rowRange.floor.index;
		
		for(int i = rowRange.floor.index; i <= rowLimit; i++) {

			if(colRange.ceiling != null) colLimit = colRange.ceiling.index;  
			else colLimit = colRange.floor.index;			
			
			for(int j = colRange.floor.index; j <= colLimit; j++) {
				sTable.addCell(new Cell(i, j, cellProperty));
			}
			
		}
		
		// TODO check for subindex at every cases and store in its SchemaRow		
		
		if(rowRange.floor.subIndex != -1) sTable.getRow(rowRange.floor.index);
		if(rowRange.ceiling.subIndex != -1);
		if(colRange.floor.subIndex != -1);
		if(colRange.ceiling.subIndex != -1);		
		
		
	}
	
	private CellIndexRange processCellIndexRange(String rangeEx) {
		
		CellIndexRange cir = new CellIndexRange();
		
		if(rangeEx.indexOf("-") != -1) { // check if there is range span symbol '-'
			String[] range = rangeEx.split("-"); // then split range by '-'				
			if(range.length != 2) throw new RuntimeException("Illegal format for Range: must be in Floor-Ceiling format.");
			
			processCellIndex(range[0], cir.floor);
			processCellIndex(range[1], cir.ceiling);			
			
			assert(cir.floor.index != -1 && cir.ceiling.index != -1);
			if(cir.floor.index == Integer.MAX_VALUE) throw new RuntimeException("Illegal Floor value: * is not allowed.");
			if(cir.floor.index > cir.ceiling.index) throw new RuntimeException("Illegal format for Range: Floor value >= Ceiling value.");
			if(cir.floor.index == cir.ceiling.index) {
				if(cir.floor.subIndex > cir.ceiling.subIndex) throw new RuntimeException("Illegal format for Range: Floor subindex value >= Ceiling subindex value.");
			}
		} else { // if there is no range span symbol '-'
			processCellIndex(rangeEx, cir.floor);
			assert(cir.floor.index != -1);
			cir.ceiling = null; // to ensure no one use ceiling value for this range.
		}
		
		return cir;
	}
	
	private void processCellIndex(String indexEx, CellIndex ci) {
		// check if the index is '*'
		if(indexEx.compareTo("*") == 0) {
			ci.index = Integer.MAX_VALUE;
		} else if(indexEx.indexOf(".") != -1) { // check if there is a subindex '.' trailing
			String[] subIdx = indexEx.split("\\."); 
			// FIXME recheck the abstract syntax notation in RuntimeException msg. 
			if(subIdx.length != 2) throw new RuntimeException("Illegal format for SubIndex: must be Int.[Int(*) | *].");
			ci.index = Integer.parseInt(subIdx[0]);
			if(subIdx[1].compareTo("*") == 0) ci.subIndex = Integer.MAX_VALUE;
			else ci.subIndex = Integer.parseInt(subIdx[1]);
		} else { // in case there is no subindex a.k.a. '.' and is not '*'
			ci.index = Integer.parseInt(indexEx);
		}
	}

	
	/**
	 * Process each row object until the very last row.
	 * @param rows
	 * @param it
	 */
	private void processRows(List<Map<String, Object>> rows) {
		int rowNum = 0;
		// For each row
		for(Map<String, Object> row : rows) {			
			SchemaRow sr = new SchemaRow(rowNum);

			// For each row property
			for(Map.Entry<String, Object> e : row.entrySet()) {
				switch(e.getKey()) {
				case "dataCols" :		
					processCols((ArrayList<Map<String, Object>>) e.getValue(), sr);
					break;
				case "repeatTimes" :
					sr.setRepeatTimes((Integer) e.getValue()); 
					break;
				default:
					sr.addProperty(e.getKey().toString(), e.getValue());
					break;
				}
			}
			
			sTable.addRow(sr);
			rowNum++;
		}		
	}
	
	/**
	 * Process all column objects of a row. 
	 * @param cols
	 * @param row
	 */
	private void processCols(List<Map<String, Object>> cols, SchemaRow row) {
		int colNum = 0;
		// For each column within a row, i.e. field
		for(Map<String, Object> col : cols) {
			Cell f = new Cell(row.getRowNum(), colNum);
			// For each field property
			for(Map.Entry<String, Object> e : col.entrySet()) {
				switch(e.getKey()) {
				case "type":
					switch((String) e.getValue()) {
					case "Empty":
						f = f.setType(EmptyField.class);
						break;
					case "Header":
						f = f.setType(HeaderField.class);
						break;
					case "Datapoint":
						// do nothing here coz' default type is Datapoint
						break;
					default:
						throw new RuntimeException("Unknown field type! " + e.getKey());
					}					
					break;
				case "applyScope":
					if(f instanceof HeaderField) {
						HeaderField hf = (HeaderField) f;
						ApplyScope scope = ApplyScope.valueOf((String) e.getValue());
						hf.setApplyScope(scope);
					} else { // if the field is not header type, just regard this as user's defined property
						f.addProperty(e.getKey().toString(), e.getValue());
					}
					break;
				case "effectiveRange":
					if(f instanceof HeaderField) {
						HeaderField hf = (HeaderField) f;
						hf.setEffectiveRange((Integer) e.getValue());
					} else { // if the field is not header type, just regard this as user's defined property
						f.addProperty(e.getKey().toString(), e.getValue());
					}
					break;
				case "relations":
					processRelations((ArrayList) e.getValue(), f);
					break;
				case "inwardRelation":
					if(f instanceof HeaderField) {
						HeaderField hf = (HeaderField) f;
						hf.setInwardRelation((String) e.getValue());
					} else { // if the field is not header type, just regard this as user's defined property
						f.addProperty(e.getKey().toString(), e.getValue());
					}
					break;
				case "outwardRelation":
					if(f instanceof HeaderField) {
						HeaderField hf = (HeaderField) f;
						hf.setOutwardRelation((String) e.getValue());
					} else { // if the field is not header type, just regard this as user's defined property
						f.addProperty(e.getKey().toString(), e.getValue());
					}
					break;					
				case "regex":
					f.setRegEx((String) e.getValue());
					break;					
				default:
					f.addProperty(e.getKey().toString(), e.getValue());
					break;
				}
			} // End field property loop
			
			row.addCell(f);
			colNum++;			
			
			// Before moving on to the next field, check if it has repeat flag
			Integer ru = (Integer) col.get("repeatUntil");
			if(ru != null) {				
				for(;colNum <= ru; colNum++) {
					f = new Cell(f);
					f.setCol(colNum);
					row.addCell(f);
				}
			}

		} // End column iteration loop
	}
	
	void processRelations(List<Map<String, Object>> relations, Cell f) {
		// For each relation
		for(Map<String, Object> relation : relations) {
			// For each property inside a relation
			for(Map.Entry<String, Object> e : relation.entrySet()) {
				switch(e.getKey()) {
				case "name":
					break;
				case "label":
					break;
				case "direction":
					break;
				case "fieldSelect":
					break;
				case "field":
					break;
				default:
				
					break;
				}
			}
		}
	}
	
	
	
//	/**
//	 * Validate each parsed CSV column in a row against CSV schema's regular expression(s)
//	 * @param row a row from CSV parser
//	 * @return boolean
//	 */
//	public boolean validate(String[] row) {
//		boolean res = true;
//		for(int i = 0; i < schema.length; i++) {		    
//		    Pattern p = Pattern.compile(schema[i], Pattern.CASE_INSENSITIVE);
//		    String colVal = row[i];
//		    if(colVal == null) colVal = "";
//		    Matcher m = p.matcher(colVal);
//		    if(!m.find()) return false;
//		}
//		return res;
//	}
	
	
	public void setFieldProperties(int row, int col, String name, Class<? extends Cell> type) {		
		sTable.getRow(row).getCol(col).setName(name);		
		if(type != null) sTable.getRow(row).getCol(col).setType(type);
	}

	/**
	 * Set field name for each [row, col] coordinate in a section.
	 * @param row
	 * @param col
	 * @param name
	 */
	public void setFieldName(int row, int col, String name) {
		sTable.getRow(row).getCol(col).setName(name);
	}
	
	public String getFieldName(int row, int col) {
		return sTable.getRow(row).getCol(col).getName();
	}
	
	public String getFieldLabel(int row, int col) {
		return sTable.getRow(row).getCol(col).getLabel();
	}
	
	public void setFieldType(int row, int col, Class<? extends Cell> type) {
		if(type != null) sTable.getRow(row).getCol(col).setType(type);
	}
	
	public Class<? extends Cell> getFieldType(int row, int col) {
		return sTable.getRow(row).getCol(col).getType();
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

}
