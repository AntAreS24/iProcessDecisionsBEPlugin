package com.tibco.cep.be.iprocessdecisions.vocabulary.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import com.tibco.cep.designtime.core.model.PROPERTY_TYPES;
import com.tibco.cep.designtime.core.model.domain.Domain;
import com.tibco.cep.designtime.core.model.domain.DomainEntry;
import com.tibco.cep.designtime.core.model.domain.DomainFactory;
import com.tibco.cep.designtime.core.model.domain.DomainInstance;
import com.tibco.cep.designtime.core.model.element.ElementFactory;
import com.tibco.cep.designtime.core.model.element.PropertyDefinition;

public class Vocabulary implements Comparable<Vocabulary>{
	private String name;
	private List<Attribute> attributes;
	private List<Association> associations;
	private String description;
	private String guid;
	
	public Vocabulary(String name) {
		this.name = name;
		this.guid = UUID.randomUUID().toString().toUpperCase();
		initialiseAttributes();
		initialiseAssociations();
	}
	
	public boolean addAssociation(Association assoc){
		if(associations == null){
			initialiseAssociations();
		}
		return associations.add(assoc);
	}
	
	private void initialiseAssociations(){
		if(associations == null){
			associations = new ArrayList<Association>();
		}
	}
	
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
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public List<Association> getAssociations() {
		return associations;
	}
	
	public String getGUID() {
		return guid;
	}

	
	public List<PropertyDefinition> getAttributesAsPropertyDefinition(String parentFolder){
		PropertyDefinition prop = null;
		List<PropertyDefinition> out = new ArrayList<>();
		
		for (Attribute attribute : attributes) {
			prop = ElementFactory.eINSTANCE.createPropertyDefinition();
			prop.setName(attribute.getName());
			
			if(attribute.getType().equalsIgnoreCase("String")){
				prop.setType(PROPERTY_TYPES.STRING);
			}else if(attribute.getType().equalsIgnoreCase("Integer")){
				prop.setType(PROPERTY_TYPES.INTEGER);
			}else if(attribute.getType().equalsIgnoreCase("DateTime")){
				prop.setType(PROPERTY_TYPES.DATE_TIME);
			}else if(attribute.getType().equalsIgnoreCase("Decimal")){
				prop.setType(PROPERTY_TYPES.DOUBLE);
			}else if(attribute.getType().equalsIgnoreCase("Boolean")){
				prop.setType(PROPERTY_TYPES.BOOLEAN);
			}else{
				//TODO ERROR, type not known. How do we aggregate the conversion errors to display as a summary at the end?
				System.err.println("Type not known: "+attribute.getType() +". Defaulting to String");
				prop.setType(PROPERTY_TYPES.STRING);
			}
			
			//TODO set a domain model if the attribute as one defined
			DomainModel model = attribute.getDomainModel();
			if(model != null){
				String modelName = attribute.getName()+"_model";
				//Create the domain model file
				Domain domain = DomainFactory.eINSTANCE.createDomain();
				domain.setName(modelName);
				domain.setGUID(UUID.randomUUID().toString().toUpperCase());
				domain.setNamespace("/Concepts/");
				domain.setFolder("/Concepts/");
				DomainEntry entry = null;
				for(String entryString: model.getEntries()){
					entry = DomainFactory.eINSTANCE.createDomainEntry();
					entry.setDescription(entryString);
					entry.setValue(entryString);
					System.out.println(entry);
					domain.getEntries().add(entry);
				}
				
				XMIResourceImpl res = new XMIResourceImpl(URI.createFileURI(parentFolder+"/Concepts/"+modelName+".domain"));
		        ((XMLResource) res).setEncoding("UTF-8");
		        res.getContents().add(domain);
		        try {
		            res.save(new HashMap<>());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        
				// Create the domain model instance
				DomainInstance domainInst = DomainFactory.eINSTANCE.createDomainInstance();
				domainInst.setOwnerProperty(prop);
				domainInst.setResourcePath("/Concept/"+modelName);
				// Set the model to match on this attribute
				prop.getDomainInstances().add(domainInst);
			}
			
			out.add(prop);
		}
		
		return out;
	}
	
	public List<PropertyDefinition> getAssociationsAsPropertyDefinition(){
		PropertyDefinition prop = null;
		List<PropertyDefinition> out = new ArrayList<>();
		
		for (Association association : associations) {
			prop = ElementFactory.eINSTANCE.createPropertyDefinition();
			prop.setName(association.getName());
			prop.setType(PROPERTY_TYPES.CONCEPT_REFERENCE);
			prop.setConceptTypePath("/Concept/"+association.getTargetType());
			if(association.getMultiplicity() != null && association.getMultiplicity().equalsIgnoreCase("*")){
				prop.setArray(true);
			}
			out.add(prop);
		}
		return out;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Vocabulary o) {
		if(this.name != null){
			if(o != null && o.name != null){
				return this.name.compareTo(o.name);
			}
			return -1;
		}else{ //this.name == null
			if(o != null && o.name != null){
				return 1;
			}
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (obj instanceof Vocabulary) {
			Vocabulary new_name = (Vocabulary) obj;
			return this.name.equals(new_name.name);
		}else{
			return false;
		}
	}


	
}
