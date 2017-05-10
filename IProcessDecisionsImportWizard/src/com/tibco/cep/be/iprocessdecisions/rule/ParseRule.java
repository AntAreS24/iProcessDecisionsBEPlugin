package com.tibco.cep.be.iprocessdecisions.rule;

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
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.tibco.cep.be.iprocessdecisions.rule.pojo.Rule;

public class ParseRule {
	private HashMap<String, ArrayList<Rule>> processedFile;
	
	
	public ParseRule() {
		processedFile = new HashMap<>();
	}
	
	private HashMap<String, Rule> processFile(String filePath) {
		HashMap<String, Rule> objectList = new HashMap<String, Rule>();
		if (filePath == null) {
			return objectList;
		}

		try {
			ZipFile zipFile = new ZipFile(filePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				// System.out.println("Name: " + entry.getName());
				if (entry.getName() != null && entry.getName().equalsIgnoreCase("RULESET_BASE.cc")) {
					// This file will list the table and their order of execution.
					InputStream stream = zipFile.getInputStream(entry);
					XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
					XMLEventReader eventReader = xmlFactory.createXMLEventReader(stream);

					Rule rule = null;
					while (eventReader.hasNext()) {
						XMLEvent event = eventReader.nextEvent();
						switch (event.getEventType()) {
						case XMLStreamConstants.START_ELEMENT:
							StartElement startElement = event.asStartElement();
							String qName = startElement.getName().getLocalPart();
							if (qName.equalsIgnoreCase("Class")) {
								Attribute name = startElement.getAttributeByName(QName.valueOf("Name"));
								//TODO check if the name is not in the restricted list
								rule = new Rule();
								//System.out.println("Object : " + voc);
								objectList.put(rule.getName(), rule);
							}
							break;
						case XMLStreamConstants.CHARACTERS:
							break;
						case XMLStreamConstants.END_ELEMENT:
							EndElement endElement = event.asEndElement();
							qName = endElement.getName().getLocalPart();
							
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

	private List<Rule> getListOfRuleObjects(String filePath, boolean forceReload) {
		if(!forceReload && processedFile.containsKey(filePath)){
			return processedFile.get(processedFile);
		}else{
			processedFile.put(filePath, new ArrayList<>(processFile(filePath).values()));
			return processedFile.get(filePath);
		}
	}
	
	private void createDecisionTable(String string, Rule rule) {
		
	}
	
	public static void main(String[] args) {
		ParseRule instance = new ParseRule();
		String filename = "D:\\temp\\sample.cvj";
		List<Rule> list = instance.getListOfRuleObjects(filename, true);
		for (Rule rule : list) {
			instance.createDecisionTable("D:\\workspace\\be\\Test",rule);
		}
	}

	


}
