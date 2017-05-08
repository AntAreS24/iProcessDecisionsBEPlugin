package com.tibco.cep.be.iprocessdecisions.vocabulary.pojo;

import java.util.ArrayList;
import java.util.List;

public class Vocabulary {
	private String name;
	private List<Attribute> attributes;
	
	public boolean addAttribute(Attribute att){
		if(attributes == null){
			initialiseAttributes();
		}
		return attributes.add(att);
	}
	
	private void initialiseAttributes(){
		if(attributes == null){
			attributes = new ArrayList<Attribute>();
		}
	}
}
