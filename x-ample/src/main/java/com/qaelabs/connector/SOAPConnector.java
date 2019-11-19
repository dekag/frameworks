package com.qaelabs.connector;

import static com.qaelabs.utils.XMLUtil.marshal;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.InvalidParameterException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import com.qaelabs.utils.WebServiceUtil;

/**
 * @author dekag This API calls the web service as mentioned in the URI of the
 *         Soap Request
 */
@Component
@Scope("prototype")
public class SOAPConnector extends WebServiceGatewaySupport {
	@Autowired
	private WebServiceUtil webServiceUtil;

	private static final Logger LOG = LogManager.getLogger(SOAPConnector.class);

	/***
	 * This API calls the web service as mentioned in the URI of the
	 * {@code soapRequest} {@link SOAPRequest}
	 * 
	 * @param soapRequest
	 * @return {@code Object} This Object should be cast down to expected response
	 *         type.
	 * @throws TransformerException
	 */
	public Object callWebService(SOAPRequest soapRequest) throws TransformerException {
		if (webServiceUtil.isSOAPRequestValid(soapRequest)) {
			String uri = webServiceUtil.getServiceURI(soapRequest.getServiceName());
			WebServiceTemplate soapWebServiceTemplate = getWebServiceTemplate();
			webServiceUtil.addSecurityInterceptorToSOAPTemplate(soapWebServiceTemplate);
			webServiceUtil.configureSendersForTimeout(soapWebServiceTemplate);
			LOG.trace("Calling Webservice : " + soapRequest.getServiceName());
			LOG.trace("URI : " + uri);
			LOG.trace("Request Payload:{}", () -> marshal(soapRequest.getRequestPayload()));
			ByteArrayOutputStream bytArrayOutputStream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(bytArrayOutputStream);
			StreamSource source = new StreamSource(new StringReader(soapRequest.getRequestPayload().toString()));
			soapWebServiceTemplate.sendSourceAndReceiveToResult(uri, source, result);

			return new String(bytArrayOutputStream.toByteArray());
		}
		throw new InvalidParameterException();
	}

}
