package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static final String DEFAULT_TABLE_NAME = "@defaultTable";
	
	/**
	 * ID of the schema with the same definition as in JSON-LD.
	 */
	private String id;
	
	/**
	 * Map between table name and schema table.
	 */
	private Map<String, SchemaTable> sTables = new HashMap<String, SchemaTable>();
	
	/**
	 * Associate the dataset with an RDF-based schema via context as in JSON-LD
	 */
	//private List<Object> context; // IMP may use JSON-LD Java object! to map with LinkedData
	
	private List<String> targetCsvs = new ArrayList<String>();
	
	private String encoding;
	
	private String lang;
	
	private String delimiter;
	
	private String lineSeparator; 
	
	private String commentPrefix;
	
	private String quoteChar;
	
	private boolean header;
	
	private int headerRowCount;
	
	private boolean skipBlankRow;
	
	private int skipColumns;
	
	private boolean doubleQuote;
	
	private boolean skipInitialSpace;
	
	private int skipRows;
	
	private boolean trim;
	
	private boolean embedHeader; 
	
	/**
	 * Other extra/user-defined properties.
	 */
	private Map<String, Object> properties = new HashMap<String, Object>();	

	SchemaCell getCell(int row, int col, SchemaTable table) {
		if(table == null) table = sTables.get("default");
		return table.getCell(row, col);
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

	public boolean isRepeatingRow(int row, SchemaTable table) {
		return table.getRow(row).isRepeat();
	}
	
	public String getLineSeparator() {
		return lineSeparator;
	}
	
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator; 
	}
	
	/**
	 * Get target CSV(s)
	 * @return list of target CSV(s) or null if none defined.
	 */
	public List<String> getTargetCsvs() {
		return targetCsvs;
	}
	
	public boolean addTargetCsv(String csvId) {
		return targetCsvs.add(csvId);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getCommentPrefix() {
		return commentPrefix;
	}

	public void setCommentPrefix(String commentPrefix) {
		this.commentPrefix = commentPrefix;
	}

	public String getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}

	public boolean isHeader() {
		return header;
	}
	
	public boolean getHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public int getHeaderRowCount() {
		return headerRowCount;
	}

	public void setHeaderRowCount(int headerRowCount) {
		this.headerRowCount = headerRowCount;
	}

	public boolean isSkipBlankRow() {
		return skipBlankRow;
	}

	public void setSkipBlankRow(boolean skipBlankRow) {
		this.skipBlankRow = skipBlankRow;
	}

	public int getSkipColumns() {
		return skipColumns;
	}

	public void setSkipColumns(int skipColumns) {
		this.skipColumns = skipColumns;
	}

	public boolean isDoubleQuote() {
		return doubleQuote;
	}

	public void setDoubleQuote(boolean doubleQuote) {
		this.doubleQuote = doubleQuote;
	}

	public boolean isSkipInitialSpace() {
		return skipInitialSpace;
	}

	public void setSkipInitialSpace(boolean skipInitialSpace) {
		this.skipInitialSpace = skipInitialSpace;
	}

	public int getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(int skipRows) {
		this.skipRows = skipRows;
	}

	public boolean isTrim() {
		return trim;
	}

	public void setTrim(boolean trim) {
		this.trim = trim;
	}

	public boolean isEmbedHeader() {
		return embedHeader;
	}

	public void setEmbedHeader(boolean embedHeader) {
		this.embedHeader = embedHeader;
	}
	
	/**
	 * Get schema table by its name.
	 * @param name
	 * @return SchemaTable of the input name or null if table with the name does not exist.
	 */
	public SchemaTable getSchemaTable(String name) {
		return sTables.get(name);
	}
	
	/**
	 * Get the map collection between table's name and schema table object.
	 * @return Map<String, SchemaTable>
	 */
	public Map<String, SchemaTable> getSchemaTables() {
		return sTables;
	}
	
	/**
	 * Add input schema table to the map. 
	 * Replacing existing schema table with the same name.  
	 * @param table
	 */
	public void addSchemaTable(SchemaTable table) {
		sTables.put(table.getName(), table);
	}

}
