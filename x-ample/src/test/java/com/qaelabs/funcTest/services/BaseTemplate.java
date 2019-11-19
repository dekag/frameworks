package com.qaelabs.funcTest.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author dekag BaseTemplate class to get the base XML document from template
 */
public class BaseTemplate {

	private Document document;
	private JsonNode jsonNode;

	/**
	 * @return the jsonNode
	 */
	public JsonNode getJsonNode() {
		return jsonNode;
	}

	/**
	 * @param jsonNode the jsonNode to set
	 */
	public void setJsonNode(JsonNode jsonNode) {
		this.jsonNode = jsonNode;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * Set document object from the given XML template
	 * 
	 * @param templateName
	 * @param baseTemplatPath
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void getXMLDocumentFromTemplate(String templateName, String baseTemplatPath) throws SAXException, IOException, ParserConfigurationException {
		File templateFile = new File(baseTemplatPath + "/" + templateName + ".xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		document = docBuilder.parse(templateFile);
	}

	/**
	 * @return String object from the given XML document
	 * @throws TransformerException
	 */
	public Object getInpuRequestFromBaseTemplate() throws TransformerException {
		if (document != null) {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(document), new StreamResult(sw));
			return sw.toString();
		}
		return null;
	}

	/**
	 * @param templateName
	 * @param baseTemplatPath
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void getJsonRequestFromTemplate(String templateName, String baseTemplatPath) throws FileNotFoundException, IOException {
		File templateFile = new File(baseTemplatPath + "/" + templateName + ".json");
		ObjectMapper objectMapper = new ObjectMapper();
		jsonNode = objectMapper.readTree(new FileInputStream(templateFile));
	}

	public Object getJsonInpuRequestFromBaseTemplate() throws TransformerException {
		if (jsonNode != null) {
			return jsonNode;
		}
		return null;
	}

}
