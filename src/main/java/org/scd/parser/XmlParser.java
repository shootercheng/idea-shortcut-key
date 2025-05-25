package org.scd.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XmlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlParser.class);

    private Map<String, List<String>> result = new LinkedHashMap<>();

    private String filePath;

    public XmlParser(String filePath) {
        this.filePath = filePath;
    }

    public void parse() {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists");
        }
        if (file.isDirectory() && file.listFiles() == null) {
            throw new IllegalArgumentException("file dir is empty");
        }
        if (file.isFile() && !filePath.endsWith(".xml")) {
            throw new IllegalArgumentException("file not xml");
        }
        if (file.isDirectory()) {
            File[] xmlFileArr = file.listFiles(item -> item.getName().endsWith(".xml"));
            for (File xmlFile : Objects.requireNonNull(xmlFileArr)) {
                parseOneXmlFile(xmlFile);
            }
        } else if (file.isFile()) {
            parseOneXmlFile(file);
        }
    }

    private void parseOneXmlFile(File xmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            // 获取所有 action 节点
            NodeList actionNodes = doc.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element action = (Element) actionNodes.item(i);
                String id = action.getAttribute("id");
                // 获取当前 action 下所有 keyboard-shortcut
                NodeList shortcuts = action.getElementsByTagName("keyboard-shortcut");
                List<String> keystrokes = new ArrayList<>();

                for (int j = 0; j < shortcuts.getLength(); j++) {
                    Element shortcut = (Element) shortcuts.item(j);
                    keystrokes.add(shortcut.getAttribute("first-keystroke"));
                }
                result.put(id, keystrokes);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<String>> getResult() {
        return result;
    }
}
