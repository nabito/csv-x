package com.dadfha.lod.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchemaTable {
	
	/**
	 * Set this to two times the expected number of row per section. 
	 */
	public static final int INIT_ROW_NUM = 100;	
	
	/**
	 * List of CellRow for the schema.
	 * 
	 * IMP this could also be stored using Map<Integer, SchemaRow> where inside SchemaRow should has Map<Integer, Cell>
	 * which eliminates the counter measure for out of order list insertion. 
	 * 
	 */	
	private List<SchemaRow> schemaRows = new ArrayList<SchemaRow>(INIT_ROW_NUM);

	public SchemaTable() {
		// initialization
		schemaRows.addAll(Collections.nCopies(INIT_ROW_NUM, (SchemaRow) null));

		// stream way of init object 
//		List<Person> persons = Stream.generate(Person::new)
//                .limit(60)
//                .collect(Collectors.toList());
	}
	
	public void addRow(SchemaRow sr) {
		if(schemaRows.size() <= sr.getRowNum()) {
			int count = sr.getRowNum() - schemaRows.size();
			for(int i = 0; i < count; i++) {
				schemaRows.add(null);
			}
			schemaRows.add(sr);
		} else {
			schemaRows.set(sr.getRowNum(), sr);
		}
	}
	
	public SchemaRow getRow(int rowNum) {
		return schemaRows.get(rowNum);
	}
	
	public void addCell(Cell cell) {
		SchemaRow sr;		
		if(cell.getRow() >= schemaRows.size()) { // if there is no row in the schema just yet
			sr = new SchemaRow(cell.getRow());
			this.addRow(sr);
		} else {
			sr = schemaRows.get(cell.getRow());	
		}		
		sr.addCell(cell);
	}

}
