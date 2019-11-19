package com.qaelabs.utils;

import static com.qaelabs.utils.CommonConstants.APPNAME;
import static com.qaelabs.utils.CommonConstants.CORRELATION_ID;
import static com.qaelabs.utils.CommonConstants.GUID;
import static com.qaelabs.utils.CommonConstants.SENDER_MACHINE;
import static com.qaelabs.utils.CommonConstants.QAELABS_CONSUMER;
import static com.qaelabs.utils.CommonConstants.QAELABS_CORRELATION_ID;
import static com.qaelabs.utils.CommonConstants.QAELABS_DIGEST;
import static com.qaelabs.utils.CommonConstants.QAELABS_GUID;
import static com.qaelabs.utils.CommonConstants.QAELABS_SENDER_MACHINE;
import static com.qaelabs.utils.CommonConstants.QAELABS_TIMESTAMP;
import static com.qaelabs.utils.CommonConstants.QAELABS_USER_REF;
import static com.qaelabs.utils.CommonConstants.USER_REF;
import static com.qaelabs.utils.CommonConstants.X_INF_REQUEST_ID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import com.qaelabs.connector.SOAPRequest;

/**
 * @author dekag WebService Utility class for generic web service related
 *         operations
 *
 */
@Component
public class WebServiceUtil {

	@Autowired
	private Environment env;

	private String applcationName;

	private String applicationPassword;

	private SecureRandom random;

	private String systemId;

	private String systemName;

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static final Logger LOGGER = LogManager.getLogger(WebServiceUtil.class);

	public static final String ESB_APP_NAME = "esbApplicationName";

	private String password;

	public String returnPassword() {
		return password;
	}

	@PostConstruct
	private void initialize() {
		random = new SecureRandom();
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			systemId = localhost.toString();
			systemName = localhost.getHostName();
		} catch (UnknownHostException e) {
			systemId = "Unknown";
			systemName = "Unknown";
			LOGGER.info("Error occurred during host identification", e);
		}
	}

	/**
	 * @param serviceName
	 * @param userRef
	 * @param correlationId
	 * @param xInfRequestId
	 * @return HttpHeaders
	 */
	public HttpHeaders createHttpHeaders(String serviceName, String userRef, String correlationId, String xInfRequestId) {

		applcationName = env.getProperty("esb." + serviceName + ".username");
		if (null != password && !"".equals(password)) {
			LOGGER.info("Password used from CyberArk");
			applicationPassword = password;
		} else {
			if (null != env.getProperty("esb." + serviceName + ".password")) {
				applicationPassword = decrypt(env.getProperty("esb." + serviceName + ".password"));
			}
		}

		String timestamp = formatter.format(new Date());
		HttpHeaders httpHeaders = new HttpHeaders();
		MediaType mediaType = getServiceMediaType(serviceName);
		httpHeaders.setContentType(mediaType);
		httpHeaders.setAccept(Arrays.asList(getServiceAccept(serviceName)));
		httpHeaders.add(QAELABS_GUID, generateGUID());
		httpHeaders.add(QAELABS_TIMESTAMP, timestamp);
		httpHeaders.add(QAELABS_CONSUMER, applcationName);
		httpHeaders.add(QAELABS_DIGEST, createDigest(timestamp, applicationPassword));
		httpHeaders.add(QAELABS_SENDER_MACHINE, systemName);
		httpHeaders.add(QAELABS_USER_REF, userRef);
		httpHeaders.add(QAELABS_CORRELATION_ID, correlationId);
		return httpHeaders;
	}

	public String getServiceURI(String serviceOperationName) {
		return env.getProperty("esb.links." + serviceOperationName + ".uri");
	}

	public MediaType getServiceAccept(String serviceOperationName) {
		String acceptType = env.getProperty("esb.links." + serviceOperationName + ".accept");
		if (null == acceptType) {
			return getServiceMediaType(serviceOperationName);
		}
		return MediaType.valueOf(acceptType);
	}

	public String getServicePayloadMediaType(String serviceOperationName) {
		return env.getProperty("esb.links." + serviceOperationName + ".mediaType");
	}

	public int getTimeout() {
		String timeoutProp = env.getProperty("esb.timeout");
		if (null != timeoutProp) {
			return Integer.parseInt(timeoutProp);
		}
		return 0;
	}

	public String getProperty(String propertyName) {
		return env.getProperty(propertyName);
	}

	private MediaType getServiceMediaType(String serviceOperationName) {
		String[] split = serviceOperationName.split("\\.");
		String mediaType = env.getProperty("esb.links." + split[0] + ".mediaType");
		if (null == mediaType) {
			mediaType = MediaType.APPLICATION_XML_VALUE;
		}
		return MediaType.valueOf(mediaType);
	}

	public String encrypt(String value) {
		byte[] crypt = Base64.getEncoder().encode(value.getBytes());
		return new String(crypt);
	}

	public static void main(String[] args) {
		System.out.println(new WebServiceUtil().encrypt("ifaportalearpassword"));
	}

	public String decrypt(String value) {
		byte[] decrypt = Base64.getDecoder().decode(value.getBytes());
		return new String(decrypt);
	}

	private String generateGUID() {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			long time = System.currentTimeMillis();
			long rand = random.nextLong();
			StringBuilder sbValueBeforeMD5 = new StringBuilder();
			sbValueBeforeMD5.append(systemId);
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(rand));
			if (md5 != null) {
				StringBuilder sb = new StringBuilder();
				md5.update(sbValueBeforeMD5.toString().getBytes());
				byte[] array = md5.digest();
				for (byte element : array) {
					int b = element & 0xFF;
					if (b < 0x10) {
						sb.append('0');
					}
					sb.append(Integer.toHexString(b));
				}
				return convertToGUIDString(sb.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			LOGGER.info("Error occurred during generation of GUID", e);
		}
		return null;
	}

	private String convertToGUIDString(String str) {
		String raw = str.toUpperCase();
		StringBuilder sb = new StringBuilder();
		sb.append(raw.substring(0, 8));
		sb.append("-");
		sb.append(raw.substring(8, 12));
		sb.append("-");
		sb.append(raw.substring(12, 16));
		sb.append("-");
		sb.append(raw.substring(16, 20));
		sb.append("-");
		sb.append(raw.substring(20));
		return sb.toString();
	}

	private String createDigest(String timestamp, String password) {
		try {
			byte[] data = (timestamp + password).getBytes();
			MessageDigest md = null;
			md = MessageDigest.getInstance("SHA");
			md.reset();
			md.update(data, 0, data.length);
			return encode(md.digest());
		} catch (NoSuchAlgorithmException e) {
			LOGGER.info("Error occurred during digest creation", e);
		}
		return null;
	}

	private String encode(byte[] cipherText) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (OutputStream out = MimeUtility.encode(bout, "base64")) {
			out.write(cipherText);
			out.flush();
		} catch (IOException | MessagingException e) {
			LOGGER.info("Error occurred during encoding digest", e);
		}
		return bout.toString().trim();
	}

	/***
	 * Creates Security Intercepter for the user name and password . *
	 * 
	 * @param userName
	 * @param password
	 * @return Wss4jSecurityInterceptor {@link Wss4jSecurityInterceptor}
	 */
	private Wss4jSecurityInterceptor securityInterceptor(String userName, String password) {
		Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
		wss4jSecurityInterceptor.setSecurementActions(WSHandlerConstants.USERNAME_TOKEN);
		wss4jSecurityInterceptor.setSecurementUsername(userName);
		wss4jSecurityInterceptor.setSecurementPassword(password);
		return wss4jSecurityInterceptor;
	}

	/**
	 * This API configures the HttpSender for timeouts *
	 * 
	 * @param soapWebServiceTemplate
	 */
	public void configureSendersForTimeout(WebServiceTemplate soapWebServiceTemplate) {
		int timeout = getTimeout();
		if (timeout > 0) {
			WebServiceMessageSender[] senders = soapWebServiceTemplate.getMessageSenders();
			for (WebServiceMessageSender sender : senders) {
				if (sender instanceof HttpComponentsMessageSender) {
					HttpComponentsMessageSender httpSender = (HttpComponentsMessageSender) sender;
					httpSender.setReadTimeout(timeout);
					httpSender.setConnectionTimeout(timeout);
				}
			}
		}
	}

	/**
	 * This API adds Security( {@link Wss4jSecurityInterceptor}) to the SOAP web
	 * service template {@link WebServiceTemplate}
	 *
	 * @param soapWebServiceTemplate {@link WebServiceTemplate}
	 */
	public void addSecurityInterceptorToSOAPTemplate(WebServiceTemplate soapWebServiceTemplate) {
		if (null != soapWebServiceTemplate) {
			ClientInterceptor[] interceptors = new ClientInterceptor[] { securityInterceptor(applcationName, applicationPassword) };
			soapWebServiceTemplate.setInterceptors(interceptors);
		}
	}

	/**
	 * This API sets the required header info for the SOAP Message
	 * {@link SOAPMessage}
	 * 
	 * @param message
	 * @param soapRequest
	 * @throws SOAPException
	 * @throws UnknownHostException
	 * @see {@link SOAPRequest}
	 * @see {@link WebServiceMessage}
	 */
	public void setSOAPWebServiceHeaders(WebServiceMessage message, SOAPRequest soapRequest) throws SOAPException, UnknownHostException {
		SOAPMessage soapMsg = ((SaajSoapMessage) message).getSaajMessage();
		MimeHeaders mimeHeader = soapMsg.getMimeHeaders();
		if (null != mimeHeader && !StringUtils.isEmpty(soapRequest.getxInfReqId())) {
			mimeHeader.setHeader(X_INF_REQUEST_ID, soapRequest.getxInfReqId());
		}
		SOAPEnvelope envelope = ((SaajSoapMessage) message).getSaajMessage().getSOAPPart().getEnvelope();
		SOAPHeaderElement sinfoHeader = ((SaajSoapMessage) message).getSaajMessage().getSOAPHeader().addHeaderElement(envelope.createName("sinfo", "esb", "http://esb.QAELABS.org"));
		String guid = generateGUID();
		sinfoHeader.addAttribute(envelope.createName(GUID), guid);
		String userRef = StringUtils.isEmpty(soapRequest.getUserRef()) ? "UNKNOWN" : soapRequest.getUserRef();
		sinfoHeader.addAttribute(envelope.createName(USER_REF), userRef);
		sinfoHeader.addAttribute(envelope.createName(APPNAME), applcationName);
		sinfoHeader.addAttribute(envelope.createName(SENDER_MACHINE), InetAddress.getLocalHost().getHostName());
		sinfoHeader.setAttribute(CORRELATION_ID, soapRequest.getCorrelationID());
	}

	/**
	 * Validates the SOAP request. *
	 * 
	 * @param soapRequest
	 * @return {@code boolean}
	 */
	public boolean isSOAPRequestValid(SOAPRequest soapRequest) {
		return null != soapRequest && !StringUtils.isEmpty(soapRequest.getUserRef()) && !StringUtils.isEmpty(soapRequest.getxInfReqId())
				&& !StringUtils.isEmpty(soapRequest.getCorrelationID()) && null != soapRequest.getRequestPayload();
	}
}
