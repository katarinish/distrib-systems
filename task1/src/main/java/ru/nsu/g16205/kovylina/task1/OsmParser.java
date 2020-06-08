package ru.nsu.g16205.kovylina.task1;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OsmParser {
    private static final Logger LOGGER = LogManager.getLogger(OsmParser.class.getName());
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private static final QName USER_ATTRIBUTE = new QName("user");
    private static final QName KEY_ATTRIBUTE = new QName("k");

    private static final String TAG = "tag";
    private static final String NODE = "node";

    private  XMLEventReader xmlEventReader = null;

    private OsmResultsKeeper osmResultsKeeper;

    public OsmParser(InputStream is) throws XMLStreamException {
        xmlEventReader = FACTORY.createXMLEventReader(is);
    }

    public void process() throws XMLStreamException {
        osmResultsKeeper = new OsmResultsKeeper();

        try {
            while (xmlEventReader.hasNext()) { // while not end of XML
                XMLEvent event = xmlEventReader.nextEvent(); // read next event

                if (event.getEventType() == XMLEvent.START_ELEMENT) {
                    StartElement startElement = event.asStartElement();

                    if (startElement.getName().getLocalPart().equals(NODE)) {
                        String userAttributeValue = startElement.getAttributeByName(USER_ATTRIBUTE).getValue();
                        if (userAttributeValue != null) {
                            osmResultsKeeper.addUserChange(userAttributeValue);
                        }

                        while (eventReader.hasNext()) { // processing tags
                            event = xmlEventReader.nextEvent();

                            if (event.getEventType() == XMLEvent.END_ELEMENT && event.asEndElement().getName().getLocalPart().equals(NODE)) {
                                break;
                            }

                            if (event.getEventType() == XMLEvent.START_ELEMENT) {
                                startElement = event.asStartElement();

                                if (startElement.getName().getLocalPart().equals(TAG)) {
                                    String keyAttributeValue = startElement.getAttributeByName(KEY_ATTRIBUTE).getValue();
                                    if (keyAttributeValue != null) {
                                        osmResultsKeeper.addKeyCount(keyAttributeValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            LOGGER.info("Finished processing file...");
        } finally {
            if (xmlEventReader != null) {
                xmlEventReader.close();
            }
        }
    }

    public void printResults() {
        osmResultsKeeper.printUserChanges();
        osmResultsKeeper.printKeys();
    }

    private static class OsmResultsKeeper {
        private Map<String, Integer> userChangesMap = new HashMap<>();
        private Map<String, Integer> uniqKeysCountMap = new HashMap<>();

        private void printMap(Map<String, Integer> map) {
            map
                    .entrySet()
                    .stream()
                    .sorted((el1, el2) -> el2.getValue() - el1.getValue())
                    .forEach(el -> System.out.println(el.getKey() + ": " + el.getValue()));
        }

        public void printUserChanges() {
            printMap(userChangesMap);
        }

        public void printKeys() {
            printMap(uniqKeysCountMap);
        }

        public void addUserChange(String value){
            userChangesMap.compute(value, (user, count) -> count == null ? 1 : count + 1);
        }

        public void addKeyCount(String value){
            uniqKeysCountMap.compute(value, (key, count) -> count == null ? 1 : count + 1);
        }
    }
}
