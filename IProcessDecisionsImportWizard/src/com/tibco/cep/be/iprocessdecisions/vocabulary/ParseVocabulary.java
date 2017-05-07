package com.tibco.cep.be.iprocessdecisions.vocabulary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import com.tibco.cep.designtime.core.model.element.Concept;
import com.tibco.cep.designtime.core.model.element.ElementFactory;
import com.tibco.cep.designtime.core.model.element.PropertyDefinition;

public class ParseVocabulary {

	public ParseVocabulary() {
	}

	public ArrayList<String> getListOfVocabularyObjects(String filePath) {
		return processFile(filePath);
	}

	public void parseVocabularyObject(String filePath, String objectName) {
		if (filePath == null) {
			return;
		}
		if (objectName == null) {
			return;
		}

		try {
			ZipFile zipFile = new ZipFile(filePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				// System.out.println("Name: " + entry.getName());
				if (entry.getName() != null && entry.getName().equalsIgnoreCase("VOCAB_BASE.cvo")) {
					InputStream stream = zipFile.getInputStream(entry);
					processFileEntries(stream, objectName);
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

	}
	
	private Concept createConcept(String parentFolder, String name, String description, List<PropertyDefinition> attributes){
		Concept con = ElementFactory.eINSTANCE.createConcept();
        con.setFolder("/Concepts");
        con.setName(name);
        con.getProperties().addAll(attributes);
        
        XMIResourceImpl res = new XMIResourceImpl(URI.createFileURI(parentFolder+"/Concepts/"+name+".concept"));
        res.getContents().add(con);
        try {
            res.save(new HashMap<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	private List<PropertyDefinition> createConceptProperties(){
		List<PropertyDefinition> out = new ArrayList<>();
		out.add(ElementFactory.eINSTANCE.createPropertyDefinition());
		return out;
	}

	private void processFileEntries(InputStream stream, String objectName) throws XMLStreamException {
		boolean startRecording = false;
		int level = 0;
		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = xmlFactory.createXMLEventReader(stream);

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				String qName = startElement.getName().getLocalPart();
				if (qName.equalsIgnoreCase("Class")) {
					processClassElement(startElement, objectName);
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				break;
			case XMLStreamConstants.END_ELEMENT:
				// End the recording for this object
				if (startRecording) {
					if (level == 0) {
						startRecording = false;
					} else {
						level--;
					}
				}
				break;
			}
		}
	}

	private void processClassElement(StartElement startElement, String objectName) {
		Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
		if (!name.getValue().equalsIgnoreCase(objectName)) {
			return;
		}
		System.out.println("Object : " + name.getValue());
		// TODO
	}

	private ArrayList<String> processFile(String filePath) {
		ArrayList<String> objectList = new ArrayList<String>();
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

					while (eventReader.hasNext()) {
						XMLEvent event = eventReader.nextEvent();
						switch (event.getEventType()) {
						case XMLStreamConstants.START_ELEMENT:
							StartElement startElement = event.asStartElement();
							String qName = startElement.getName().getLocalPart();
							if (qName.equalsIgnoreCase("Class")) {
								Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
								System.out.println("Object : " + name.getValue());
								objectList.add(name.getValue());
							}
							break;
						case XMLStreamConstants.CHARACTERS:
							break;
						case XMLStreamConstants.END_ELEMENT:
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
		String filename = "D:\\Work\\Projects\\ANZ\\2017-03-22 - BE Decisions Workshop\\src\\OneCare_Express_Prototype.cvj";
		instance.processFile(filename);

		instance.parseVocabularyObject(filename, "Person");
		
		instance.createConcept("D:\\Work\\Workspaces\\BE-5.4\\CustomerOrderManagement","Test","Test Description",null);

	}
}
