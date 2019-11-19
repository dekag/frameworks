package com.qaelabs.funcTest.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qaelabs.connector.RestConnector;
import com.qaelabs.connector.SOAPConnector;
import com.qaelabs.funcTest.utils.ScenarioContext;
import com.qaelabs.funcTest.utils.TestUtility;
import com.qaelabs.utils.CommonConstants;

/**
 * @author dekag Generic utility class for generic steps
 *
 */
public class GenericStepUtility {

	private BaseTemplate baseTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(GenericStepUtility.class);

	@Autowired
	TestUtility testUtil;

	@Autowired
	BrmsUtility brmsUtility;

	@Autowired
	RestConnector restConnector;

	@Autowired
	SOAPConnector soapConnector;

	public GenericStepUtility() {
		// TODO Auto-generated constructor stub
	}

	public GenericStepUtility(BaseTemplate baseTemplate) {
		this.baseTemplate = baseTemplate;
	}

	public BaseTemplate getBaseTemplate() {
		return baseTemplate;
	}

	public void setBaseTemplate(BaseTemplate baseTemplate) {
		this.baseTemplate = baseTemplate;
	}

	/**
	 * Creates XML representation for the input request template
	 * 
	 * @param templateName
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void createXMLDoc(String templateName, String baseTemplatPath) throws SAXException, IOException, ParserConfigurationException {
		baseTemplate.getXMLDocumentFromTemplate(templateName, baseTemplatPath);
	}

	/**
	 * method to set Tag value for all the attribute This works when
	 * //coverinElement/elementName tags satisfy your limits
	 * 
	 * @param key
	 * @param coveringElement
	 * @param valArr
	 * @throws Exception
	 */
	public void setElementValue(String key, String coveringElement, String values) throws Exception {
		String[] valArr = values.split(";");
		Document doc = baseTemplate.getDocument();
		NodeList nodeList = getNodeList(doc, key, coveringElement);

		for (int i = 0; i < nodeList.getLength(); i++) {
			String value = testUtil.retrieveValue(valArr, i);
			if (value.equalsIgnoreCase("null")) {
				nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
			} else if (value.toLowerCase().contains("~null")) {
				nodeList.item(i).getParentNode().getParentNode().removeChild(nodeList.item(i).getParentNode());
				break;
			} else
				nodeList.item(i).setTextContent(value);

		}
		if (nodeList.getLength() == 0)
			throw new Exception("Unable to set the given node value:  Tag - " + key + " Covering Element " + coveringElement + " Value " + values);
	}

	/**
	 * @param doc
	 * @param key
	 * @param coveringElement
	 * @return
	 * @throws XPathExpressionException
	 */
	public NodeList getNodeList(Document doc, String key, String coveringElement) throws XPathExpressionException {
		NodeList nodeList = null;

		XPath xpath = XPathFactory.newInstance().newXPath();
		String coveringElementsStr = "";
		String[] element = coveringElement.split("-");

		for (int i = 0; i < element.length; i++) {
			coveringElementsStr = coveringElementsStr + "/*[local-name()='" + element[i].trim() + "']";
		}
		if (key.toLowerCase().trim().contains("count") && key.toLowerCase().trim().contains("-")) {
			String[] nodeElement = key.split("@")[1].split("-");
			nodeList = (NodeList) xpath.compile("/" + coveringElementsStr + "/*[local-name()='" + nodeElement[0].trim() + "'][text()='" + nodeElement[1].trim() + "']")
					.evaluate(doc, XPathConstants.NODESET);
		} else if (key.toLowerCase().trim().contains("count@")) {
			String[] nodeElement = key.split("@");
			nodeList = (NodeList) xpath.compile("/" + coveringElementsStr + "/*[local-name()='" + nodeElement[1].trim() + "']").evaluate(doc, XPathConstants.NODESET);
		} else if (key.toLowerCase().trim().contains("value@")) {
			String[] nodeElement = key.split("@")[1].split("~");
			nodeList = (NodeList) xpath.compile("/" + coveringElementsStr + "/*[local-name()='" + nodeElement[0].trim() + "'][text()='" + nodeElement[1].trim()
					+ "']/../*[local-name()='" + key.split("@")[2] + "']").evaluate(doc, XPathConstants.NODESET);
			System.out.println(nodeList.getLength());
		} else {
			nodeList = (NodeList) xpath.compile("/" + coveringElementsStr + "/*[local-name()='" + key.trim() + "']").evaluate(doc, XPathConstants.NODESET);
		}
		return nodeList;
	}

	/**
	 * Compare values without maintaining the order
	 * 
	 * @param key
	 * @param coveringElement
	 * @param values
	 * @param sc
	 * @throws XPathExpressionException
	 */
	public void validateElementValue(String key, String coveringElement, String values, ScenarioContext sc) throws XPathExpressionException {

		Document doc = (Document) sc.getContext(CommonConstants.RESPONSE_DOC);
		NodeList nodeList = getNodeList(doc, key, coveringElement);
		String[] valArr = values.split(";");
		List<String> actualValues = nodeListValues(key, nodeList);

		if (key.toLowerCase().contains("count@")) {
			Assert.assertTrue("Expected " + values + " actual " + nodeList.getLength(), nodeList.getLength() == Integer.parseInt(values));
		} else if (values.length() == 0) {
			Assert.assertTrue("Expected " + values + " Actual :" + nodeList.getLength(), nodeList.getLength() == 0);
		} else if (key.toLowerCase().contains("value@")) {
			List<String> expectedValues = new ArrayList<>();
			for (int i = 0; i < valArr.length; i++) {
				if (!valArr[i].startsWith("!"))
					expectedValues.add(valArr[i]);
			}
			LOG.info("Expected " + expectedValues + " actual - " + actualValues);
			Assert.assertTrue("Expected " + expectedValues + " actual - " + actualValues, actualValues.size() == expectedValues.size());
			Assert.assertTrue("Expected: " + expectedValues + " actual: " + actualValues, actualValues.equals(expectedValues));
		} else {
			for (int i = 0; i < valArr.length; i++) {
				if (valArr[i].startsWith("!"))
					Assert.assertFalse("Expected: " + valArr[i] + " actual: " + actualValues, testUtil.checkExpectedValue(actualValues.get(i), valArr[i].substring(1)));
				else
					Assert.assertTrue("Expected: " + valArr[i] + " actual: " + actualValues, testUtil.checkExpectedValue(actualValues.get(i), valArr[i]));
			}
		}
	}

	/**
	 * @param key
	 * @param nodeList
	 * @param actualValues
	 * @return
	 */
	private List<String> nodeListValues(String key, NodeList nodeList) {
		List<String> actualValues = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (key.toLowerCase().contains("date"))
				actualValues.add(nodeList.item(i).getTextContent().substring(0, 10));
			else
				actualValues.add(nodeList.item(i).getTextContent());
		}
		return actualValues;
	}

	/**
	 * @param templateName
	 * @param property
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void createJsonRequest(String templateName, String baseTemplatPath) throws FileNotFoundException, IOException {
		baseTemplate.getJsonRequestFromTemplate(templateName, baseTemplatPath);

	}

	/**
	 * @param key
	 * @param coveringElement
	 * @param values
	 */
	public void setJsonElementValue(String key, String coveringElement, String values) {
		ArrayNode array = null;
		if (values.contains("~")) {
			ObjectMapper mapper = new ObjectMapper();
			array = mapper.valueToTree(values.split("~"));
		}

		JsonNode jsonNode = baseTemplate.getJsonNode();
		ObjectNode objectNode = null;
		String[] valArr = values.split(";");
		for (String parent : coveringElement.split("-")) {
			JsonNode childNode = null;
			if (key.equals(coveringElement))
				childNode = jsonNode;
			else
				childNode = jsonNode.path(parent);

			if (childNode.isMissingNode())
				continue;

			if (childNode.isArray()) {
				for (int i = 0; i < childNode.size(); i++) {
					String value = testUtil.retrieveValue(valArr, i);
					objectNode = (ObjectNode) childNode.get(i);
					objectNode.put(key, value);
				}
			} else {
				Iterator<Map.Entry<String, JsonNode>> fields = childNode.fields();
				while (fields.hasNext()) {
					Map.Entry<String, JsonNode> entry = fields.next();
					if (entry.getKey().equals(key)) {
						JsonNode childArrayCheck = childNode.path(entry.getKey());
						objectNode = (ObjectNode) childNode;
						if (childArrayCheck.isArray()) {
							objectNode.putArray(entry.getKey()).addAll(array);
						} else {
							objectNode.put(entry.getKey(), values);
						}

					}
				}
			}
			jsonNode = childNode;
		}
	}

	/**
	 * @param key
	 * @param coveringElement
	 * @param values
	 * @param sc
	 */
	public void validateJsonNodeValue(String key, String coveringElement, String values, ScenarioContext sc) {
		JsonNode jsonNode = (JsonNode) sc.getContext(CommonConstants.RESPONSE_JSON);
		ObjectNode objectNode = null;
		String[] valArr = values.split(";");
		for (String parent : coveringElement.split("-")) {
			JsonNode childNode = jsonNode.path(parent);
			if (childNode.isArray()) {
				for (int i = 0; i < childNode.size(); i++) {
					String value = testUtil.retrieveValue(valArr, i);
					objectNode = (ObjectNode) childNode.get(i);
					Assert.assertEquals(value, objectNode.get(key).asText());
				}
			} else {
				objectNode = (ObjectNode) jsonNode;
				Assert.assertEquals("Expected " + values + " Actual : " + objectNode.toString(), values, objectNode.get(key).asText());
			}
		}

	}

}
