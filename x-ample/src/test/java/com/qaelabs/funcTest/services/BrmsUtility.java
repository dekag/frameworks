package com.qaelabs.funcTest.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import com.qaelabs.connector.RestConnector;
import com.qaelabs.connector.SOAPConnector;
import com.qaelabs.connector.SOAPRequest;
import com.qaelabs.connector.SOAPRequest.SOAPRequestBuilder;
import com.qaelabs.funcTest.utils.ScenarioContext;
import com.qaelabs.utils.CommonConstants;
import com.qaelabs.utils.WebServiceUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author dekag Implementation class for Brms web services (Sample Service for
 *         test)
 *
 */
public class BrmsUtility {

	private BaseTemplate baseTemplate;

	@Autowired
	WebServiceUtil util;

	public BrmsUtility(BaseTemplate baseTemplate) {
		this.baseTemplate = baseTemplate;
	}

	/**
	 * @param opType
	 * @param sc
	 * @param restConnector
	 * @param requestType
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void executeRestServiceRequest(String opType, ScenarioContext sc, RestConnector restConnector)
			throws TransformerException, ParserConfigurationException, SAXException, IOException {
		Object inputRequest = baseTemplate.getInpuRequestFromBaseTemplate();
		sc.setContext(CommonConstants.TXN_TYPE, opType.toUpperCase());
		System.out.println("Request " + inputRequest);
		String response = null;
		Map<String, String> uriParams = new HashMap<>();

		switch (opType.toUpperCase()) {
		case "LOANS_BRMS":
			uriParams.put("transaction-type", "LOANS_BRMS");
			response = restConnector.postData("loans.brms", uriParams, null, CommonConstants.TXN_TYPE, (String) sc.getContext("guid"), null, inputRequest, String.class);
			break;
		}
		System.out.println("Response " + response);
		sc.setContext(CommonConstants.RESPONSE, response);
		sc.setContext(CommonConstants.RESPONSE_XML, response);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document document = docBuilder.parse(new InputSource(new StringReader(response)));
		sc.setContext(CommonConstants.RESPONSE_DOC, document);

	}

	/**
	 * @param opType
	 * @param sc
	 * @param soapConnector
	 * @throws TransformerException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void executeSoapServiceRequest(String opType, ScenarioContext sc, SOAPConnector soapConnector)
			throws TransformerException, SAXException, IOException, ParserConfigurationException {
		Object inputRequest = baseTemplate.getInpuRequestFromBaseTemplate();
		sc.setContext(CommonConstants.TXN_TYPE, opType.toUpperCase());
		String response = null;
		HttpHeaders httpHeaders = null;
		SOAPRequest soapRequest = null;

		// System.out.println("Request \n"+inputRequest.toString());

		switch (opType.toUpperCase()) {
		case "WITHDRAWALS_BRMS":
			httpHeaders = util.createHttpHeaders("withdrawals.brms", "", "", "");
			soapRequest = new SOAPRequestBuilder("withdrawals.brms", inputRequest).setcorrelationID(String.valueOf(httpHeaders.get(CommonConstants.QAELABS_GUID)))
					.setuserRef(String.valueOf(httpHeaders.get(CommonConstants.QAELABS_USER_REF))).setxInfReqId(String.valueOf(httpHeaders.get(CommonConstants.QAELABS_USER_REF)))
					.buildRequest();
			response = (String) soapConnector.callWebService(soapRequest);
			break;

		}

		// System.out.println("Response "+response);
		sc.setContext(CommonConstants.RESPONSE, response);
		sc.setContext(CommonConstants.RESPONSE_XML, response);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document document = docBuilder.parse(new InputSource(new StringReader(response)));
		sc.setContext(CommonConstants.RESPONSE_DOC, document);

	}

	/**
	 * @param opType
	 * @param sc
	 * @param restConnector
	 * @throws Exception
	 */
	public void executeJsonRestServiceRequest(String opType, ScenarioContext sc, RestConnector restConnector) throws Exception {

		Object inputRequest = baseTemplate.getJsonInpuRequestFromBaseTemplate();
		sc.setContext(CommonConstants.TXN_TYPE, opType.toUpperCase());
		String response = null;
		Map<String, String> uriParams = new HashMap<>();

		System.out.println(inputRequest.toString());

		switch (opType.toUpperCase()) {
		case "MULECONTACT":
			uriParams.put("transaction-type", "MULECONTACT");
			try {
				response = restConnector.postData("mule.contact", uriParams, null, CommonConstants.TXN_TYPE, (String) sc.getContext("guid"), null, inputRequest.toString(),
						String.class);
			} catch (HttpClientErrorException e) {
				sc.setContext(CommonConstants.RESPONSE_CODE, e.getRawStatusCode());
				response = e.getResponseBodyAsString();
			}
			break;

		case "SENDCLIENTNOTIFICATIONEMAIL":
			uriParams.put("transaction-type", "SENDCLIENTNOTIFICATIONEMAIL");
			try {
				response = restConnector.postData("sendClientNotificationEmail", uriParams, null, CommonConstants.TXN_TYPE, (String) sc.getContext("guid"), null,
						inputRequest.toString(), String.class);
			} catch (HttpClientErrorException e) {
				sc.setContext(CommonConstants.RESPONSE_CODE, e.getRawStatusCode());
				response = e.getResponseBodyAsString();
			}
			break;
		}
		sc.setContext(CommonConstants.RESPONSE, response);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responsNode = mapper.readTree(response);
		sc.setContext(CommonConstants.RESPONSE_JSON, responsNode);
		System.out.println(response);

	}

	/**
	 * @param opType
	 * @param sc
	 * @param restConnector
	 */
	public void executeGetRequest(String opType, ScenarioContext sc, RestConnector restConnector) {
		sc.setContext(CommonConstants.TXN_TYPE, opType.toUpperCase());
		String response = null;
		Map<String, String> uriParams = new HashMap<>();

		uriParams.put("transaction-type", "dummy");
		try {
			response = restConnector.getData("dummy", uriParams, null, "Test", "Test", "Test", String.class);
			System.out.println(response);	
		} catch (HttpClientErrorException e) {
			sc.setContext(CommonConstants.RESPONSE_CODE, e.getRawStatusCode());
			response = e.getResponseBodyAsString();
		}
		
	}

}
