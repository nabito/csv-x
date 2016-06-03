package com.dadfha.lod.csv;

/**
 * It's a cell that has relations to many other cells in a direction.
 * There is other type of header notion which is more common in RDB and tabular data model
 * in which the header cell serves as a property name between a primary key of a row to its value.
 * 
 * @deprecated
 * 
 * CSV-X Schema Model is more generic way of seeing a cell as just a cell that holds value, and the value
 * could be referenced and interpreted freely.
 * 
 * @author Wirawit
 *
 */
public class HeaderField extends SchemaCell {
		
	/**
	 * The scope in which the header field is applied.
	 */
	public enum ApplyScope {
		ABOVE, BELOW, LEFT, RIGHT, DATASET;
	}

	/**
	 * The scope in which the header is applied.
	 */
	ApplyScope scope;
	
	/**
	 * Effective range that the header is applied.
	 */
	int effectiveRange;
	
	/**
	 * Relation the header field has with its apply scope Ex. Header -- relation --> Datapoint  
	 */
	String outwardRelation;

	/**
	 * Relation the apply scope has with its header field Ex. Header <-- relation -- Datapoint  
	 */	
	String inwardRelation;
	
	/**
	 * Copy constructor for Field.
	 * @param f
	 */
	public HeaderField(SchemaCell f) {
		super(f);
	}
	
	/**
	 * Copy constructor for HeaderField.
	 * @param f
	 */
	public HeaderField(HeaderField f) {
		super(f);
		scope = f.scope;
		effectiveRange = f.effectiveRange;
		outwardRelation = f.outwardRelation;
		inwardRelation = f.inwardRelation;
	}
	
	public HeaderField(int row, int col, SchemaTable st) {
		super(row, col, st);
	}
	
	public HeaderField(int row, int col, ApplyScope scope, int effectiveRange, SchemaTable st) {
		super(row, col, st);
		this.scope = scope;
		this.effectiveRange = effectiveRange;
	}
	
	public ApplyScope getApplyScope() {
		return scope;
	}
	
	public void setApplyScope(ApplyScope scope) {
		this.scope = scope;
	}
	
	public int getEffectiveRange() {
		return effectiveRange;
	}
	
	public void setEffectiveRange(int range) {
		effectiveRange = range;
	}	
	
	/**
	 * @return the outwardRelation
	 */
	public String getOutwardRelation() {
		return outwardRelation;
	}

	/**
	 * @param outwardRelation the outwardRelation to set
	 */
	public void setOutwardRelation(String outwardRelation) {
		this.outwardRelation = outwardRelation;
	}

	/**
	 * @return the inwardRelation
	 */
	public String getInwardRelation() {
		return inwardRelation;
	}

	/**
	 * @param inwardRelation the inwardRelation to set
	 */
	public void setInwardRelation(String inwardRelation) {
		this.inwardRelation = inwardRelation;
	}	

}
