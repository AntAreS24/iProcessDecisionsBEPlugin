package com.tibco.cep.be.iprocessdecisions.vocabulary.pojo;

public class Association {
	private String name;
	private String targetType;
	private String multiplicity;
	
	public Association(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTargetType(String value) {
		this.targetType = value;
	}
	
	public String getTargetType(){
		return targetType;
	}

	public void setMultiplicity(String multiplicity) {
		this.multiplicity = multiplicity;
	}
	
	public String getMultiplicity() {
		return multiplicity;
	}

	
}
