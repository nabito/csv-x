package com.dadfha.lod.csv;

public class FieldSelect {
	
	/**
	 * Selection mode.
	 * 
	 * SINGLE		select only a single field 
	 * TODO MULTIPLE
	 * DATASET		select all field in a dataset
	 * ROW_SPAN		span selection from start field to end field by row (Max(row) = end field's row)
	 * 				this is a common form of field selection as it follows natural order of csv format
	 * COL_SPAN		span selection from start field to end field by column (Max(col) = end field's column)
	 * 
	 * 	Note: for ROW_SPAN and COL_SPAN: if end field row or col is -1, it means the end is infinite
	 * 
	 * ROW_ONLY		only select the whole row from start field's row to end field's row	
	 * COL_ONLY		only select the whole column from start field's column to end field's column
	 * GTE_FIELD	select anything greater than or equal to start field
	 * LTE_FIELD	select anything lesser than or equal to start field
	 * GTE_ROW		select all rows greater than or equal to start field's row
	 * GTE_COL		select all columns greater than or equal to start field's column
	 * LTE_ROW		select all rows lesser than or equal to start field's row
	 * LTE_COL		select all columns lesser than or equal to start field's column 
	 * 
	 * Note: All selection modes are inclusive, meaning it includes the reference field(s) into selection.
	 * For any mode requiring only one reference point, the "Field start" [row, col] coordinate will be used.
	 * @author Wirawit
	 *
	 */
	public enum Mode {
		SINGLE, MULTIPLE, DATASET, ROW_SPAN, COL_SPAN, ROW_ONLY, COL_ONLY, GTE_FIELD, LTE_FIELD, GTE_ROW, GTE_COL, LTE_ROW, LTE_COL 
	}
	
	/**
	 * Start field.
	 */
	private Field start;
	
	/**
	 * End field.
	 */
	private Field end;
	
	/**
	 * Mode of selection.
	 */
	private Mode mode;	
	
	/**
	 * Constructor.
	 * @param row
	 * @param col
	 */
	public FieldSelect(int row, int col) {
		this(row, col, row, col, Mode.SINGLE);
	}
	
	/**
	 * Constructor.
	 * @param row
	 * @param col
	 * @param mode
	 */
	public FieldSelect(int row, int col, Mode mode) {
		this(row, col, row, col, mode);
	}
	
	/**
	 * Constructor.
	 * @param row1
	 * @param col1
	 * @param row2
	 * @param col2
	 * @param mode
	 */
	public FieldSelect(int row1, int col1, int row2, int col2, Mode mode) {
		
		if(mode == null) mode = Mode.SINGLE;
		
		// normalize -1 value
		row1 = normalizeRange(row1);
		col1 = normalizeRange(col1);
		row2 = normalizeRange(row2);
		col2 = normalizeRange(col2);
		
		int startRow, endRow, startCol, endCol;
		
		// check which row,col pair come first from (0,0) at top left corner
		if( (row1 < row2) || ((row1 == row2) && (col1 <= col2)) ) {
			startRow = row1;
			startCol = col1;
			endRow = row2;		
			endCol = col2;
		} else { 
			//if( (row1 > row2) || (row1 == row2) && (col1 > col2) ) {
			startRow = row2;
			startCol = col2;
			endRow = row1;		
			endCol = col1;
		}
		
		start = new Field(startRow, startCol);
		end = new Field(endRow, endCol);
		this.mode = mode;
	}
	
	/**
	 * Normalize value of a field range.
	 * -1 indicates infinite thus will be converted to maximum integer value.
	 * Other original values will return as is otherwise.
	 * @param range
	 * @return int
	 */
	private int normalizeRange(int range) {
		if(range == -1) return Integer.MIN_VALUE;
		else return range;
	}
	
	/**
	 * Check if a column in within range of the selection.
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean isSelected(int row, int col) {
		
		switch(mode) {
		case SINGLE:
			if( (row == start.getRow()) && (col <= start.getCol()) ) return true;
			break;
		case DATASET:
			return true;
		case ROW_SPAN:
			if( (row > start.getRow()) && (row < end.getRow()) ) return true;
			else if( (row == start.getRow()) && (col >= start.getCol()) ) return true;
			else if( (row == end.getRow()) && (col <= end.getCol()) ) return true;
			break;
		case COL_SPAN:
			if( (row >= start.getRow()) && (row <= end.getRow()) && (col >= start.getCol()) && (col <= end.getCol()) ) return true;
			break;
		case ROW_ONLY:
			if( (row >= start.getRow()) && (row <= end.getRow()) ) return true;
			break;
		case COL_ONLY:
			if( (col >= start.getCol()) && (col <= end.getCol()) ) return true;
			break;			
		case GTE_FIELD:
			if( (row > start.getRow()) || ((row == start.getRow()) && (col >= start.getCol())) ) return true;
			break;			
		case LTE_FIELD:
			if( (row < start.getRow()) || ((row == start.getRow()) && (col <= start.getCol())) ) return true;
			break;			
		case GTE_ROW:
			if( (row >= start.getRow()) ) return true;
			break;			
		case GTE_COL:
			if( (col >= start.getCol()) ) return true;
			break;
		case LTE_ROW:
			if( (row <= start.getRow()) ) return true;
			break;
		case LTE_COL:
			if( (col <= start.getCol()) ) return true;
			break;
		default:
			break;
		}
		
		return false;
	}

	public Field getStart() {
		return start;
	}

	public void setStart(Field start) {
		this.start = start;
	}

	public Field getEnd() {
		return end;
	}

	public void setEnd(Field end) {
		this.end = end;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

}
