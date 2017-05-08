package com.tibco.cep.be.iprocessdecisions.vocabulary.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DomainModel {
	private HashMap<String,String> entries;
	
	public DomainModel() {
		this.entries = new HashMap<>();
	}

	/**
	 * Associates the specified value with the specified description. 
	 * If the entries previously contained a mapping for the description, the old value is replaced.
	 * @param description
	 * @param value
	 * @return 
	 */
	public String addEntry(String description, String value){
		return entries.put(description, value);
	}
	
	public List<String> getEntries(){
		return new ArrayList<>(entries.values());
	}
	
}
