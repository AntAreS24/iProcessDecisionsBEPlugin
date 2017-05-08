package com.tibco.cep.be.iprocessdecisions.vocabulary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Association;
import com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.DomainModel;
import com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Vocabulary;
import com.tibco.cep.designtime.core.model.element.Concept;
import com.tibco.cep.designtime.core.model.element.ElementFactory;

public class ParseVocabulary {

	private HashMap<String, ArrayList<Vocabulary>> processedFile;
	
	public ParseVocabulary() {
		processedFile = new HashMap<>();
	}

	/**
	 * Returns the list of Vocabulary objects from the file specified.
	 * The list is cached for efficiency.
	 * @param filePath
	 * @param forceReload flag to force the reparsing of the file
	 * @return
	 */
	public ArrayList<Vocabulary> getListOfVocabularyObjects(String filePath, boolean forceReload) {
		if(!forceReload && processedFile.containsKey(filePath)){
			return processedFile.get(processedFile);
		}else{
			processedFile.put(filePath, new ArrayList<>(processFile(filePath).values()));
			return processedFile.get(filePath);
		}
	}

	public Concept createConcept(String parentFolder, Vocabulary vocabulary){
		Concept con = ElementFactory.eINSTANCE.createConcept();
        con.setFolder("/Concepts");
        con.setName(vocabulary.getName());
        con.setGUID(vocabulary.getGUID());
        con.getProperties().addAll(vocabulary.getAttributesAsPropertyDefinition(parentFolder));
        con.getProperties().addAll(vocabulary.getAssociationsAsPropertyDefinition());
        
        XMIResourceImpl res = new XMIResourceImpl(URI.createFileURI(parentFolder+"/Concepts/"+vocabulary.getName()+".concept"));
        ((XMLResource) res).setEncoding("UTF-8");
        res.getContents().add(con);
        try {
            res.save(new HashMap<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return null;
	}

	private HashMap<String, Vocabulary> processFile(String filePath) {
		HashMap<String, Vocabulary> objectList = new HashMap<String, Vocabulary>();
		if (filePath == null) {
			return objectList;
		}

		try {
			ZipFile zipFile = new ZipFile(filePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				// System.out.println("Name: " + entry.getName());
				if (entry.getName() != null && entry.getName().equalsIgnoreCase("VOCAB_BASE.cvo")) {
					InputStream stream = zipFile.getInputStream(entry);
					XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
					XMLEventReader eventReader = xmlFactory.createXMLEventReader(stream);

					Vocabulary voc = null;
					Association assoc = null;
					com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Attribute att = null;
					int associationEndIndex = 0;
					while (eventReader.hasNext()) {
						XMLEvent event = eventReader.nextEvent();
						switch (event.getEventType()) {
						case XMLStreamConstants.START_ELEMENT:
							StartElement startElement = event.asStartElement();
							String qName = startElement.getName().getLocalPart();
							if (qName.equalsIgnoreCase("Class")) {
								Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
								//TODO check if the name is not in the restricted list
								voc = new Vocabulary(name.getValue());
								System.out.println("Object : " + voc);
								objectList.put(voc.getName(), voc);
							}
							if(qName.equalsIgnoreCase("Attribute")){
								if(voc != null){
									Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
									String nameValue = null;
									// replace ID with something else as it's a reserved work
									if(name != null && name.getValue().equalsIgnoreCase("id")){
										nameValue = name.getValue()+"_field";
									}else{
										nameValue = name.getValue();
									}
									Attribute type = startElement.getAttributeByName(QName.valueOf("Type"));
									att = new com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Attribute(nameValue, type.getValue());
									voc.addAttribute(att);
									
									System.out.println("\t [a]:"+name+ " | "+type);
									
									Attribute attrEnum = startElement.getAttributeByName(QName.valueOf("EnumerationValueSet"));
									if(attrEnum != null){
										Pattern pattern = Pattern.compile("['](.*?)[']");
										Matcher matcher = pattern.matcher(attrEnum.getValue());
										DomainModel model = new DomainModel();
										while(matcher.find()){
											String description = matcher.group().split("'")[1];
											model.addEntry(description, description);
										}
										att.setDomainModel(model);
									}
								}
							}
							if(qName.equalsIgnoreCase("Association")){
								if(voc != null){
									Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
									String nameValue = null;
									// replace ID with something else as it's a reserved work
									if(name != null && name.getValue().equalsIgnoreCase("id")){
										nameValue = name.getValue()+"_field";
									}else{
										nameValue = name.getValue();
									}
									assoc = new com.tibco.cep.be.iprocessdecisions.vocabulary.pojo.Association(nameValue);
								}
							}
							if(qName.equalsIgnoreCase("AssociationEnd")){
								Attribute type = startElement.getAttributeByName(QName.valueOf("Type"));
								// If the first AssociationEnd element type match the name of the Class, then the association
								// is in the right direction and we're interesting in the target type.
								if(voc.getName().equalsIgnoreCase(type.getValue())){
									associationEndIndex++;
									break;
								}
								
								if(associationEndIndex > 0 && !voc.getName().equalsIgnoreCase(type.getValue())){
									System.out.println("\t [ref]:"+assoc.getName() + " | "+type.getValue());
									Attribute multiplicity = startElement.getAttributeByName(QName.valueOf("Multiplicity"));
									assoc.setTargetType(type.getValue());
									assoc.setMultiplicity(multiplicity.getValue());
									
									voc.addAssociation(assoc);
								}
								
							}
							break;
						case XMLStreamConstants.CHARACTERS:
							break;
						case XMLStreamConstants.END_ELEMENT:
							EndElement endElement = event.asEndElement();
							qName = endElement.getName().getLocalPart();
							if (qName.equalsIgnoreCase("Class")) {
								voc = null;
							}
							if(qName.equalsIgnoreCase("Attribute")) {
								att = null;
							}
							if(qName.equalsIgnoreCase("Association")) {
								assoc = null;
								associationEndIndex = 0;
							}
							break;
						}
					}

				}
			}
			zipFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return objectList;
	}

	public static void main(String[] args) {
		ParseVocabulary instance = new ParseVocabulary();
		String filename = "D:\\temp\\sample.cvj";
		List<Vocabulary> list = instance.getListOfVocabularyObjects(filename, true);
		for (Vocabulary vocabulary : list) {
			instance.createConcept("D:\\workspace\\be\\Test",vocabulary);
		}
	}
}
