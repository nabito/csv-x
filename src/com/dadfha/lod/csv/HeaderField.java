package com.dadfha.lod.csv;

public class HeaderField extends Cell {
		
	/**
	 * The scope in which the header field is applied.
	 * 
	 * TODO consider removing this class altogether, as we're not using Header metaphor anymore....
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
	public HeaderField(Cell f) {
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
	
	public HeaderField(int row, int col) {
		super(row, col);
	}
	
	public HeaderField(int row, int col, ApplyScope scope, int effectiveRange) {
		super(row, col);
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
