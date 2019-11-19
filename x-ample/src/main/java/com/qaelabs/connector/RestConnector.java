package com.qaelabs.connector;

import static com.qaelabs.utils.XMLUtil.marshal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.qaelabs.utils.WebServiceUtil;

/**
 * @author dekag Class to connect to Rest https methods
 */
@Component
public class RestConnector {
	@Autowired
	WebServiceUtil util;

	private RestTemplate restTemplate;

	private static final Logger LOG = LogManager.getLogger(RestConnector.class);

	@PostConstruct
	private void initialize() {
		int timeout = util.getTimeout();
		if (timeout > 0) {
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(timeout);
			factory.setReadTimeout(timeout);
			this.restTemplate = new RestTemplate(factory);
		} else {
			this.restTemplate = new RestTemplate();
		}
	}

	/**
	 * This API is to do <code>GET</code> call with passing relevant parameters.
	 * Rest all will be taken care by this API
	 * 
	 * @param serviceOperationName key for service & operation to be invoked as
	 *                             configured in properties
	 * @param uriParams            any uri params which needs to be passed
	 * @param queryParams          any query params which needs to be passed
	 * @param userRef              header to be passed in request
	 * @param correlationId        header to be passed in request
	 * @param xInfRequestId        header to be passed in request
	 * @param responseType
	 * @return response object
	 */
	public <T> T getData(String serviceOperationName, Map<String, String> uriParams, Map<String, String> queryParams, String userRef, String correlationId, String xInfRequestId,
			Class<T> responseType) {
		try {
			String uri = util.getServiceURI(serviceOperationName);
			String url = createUri(uri, uriParams, queryParams);
			LOG.trace("Calling Webservice : " + serviceOperationName);
			LOG.trace("URI : " + url);
			HttpHeaders headers = util.createHttpHeaders(serviceOperationName, userRef, correlationId, xInfRequestId);
			HttpEntity<T> entity = new HttpEntity<T>(headers);
			ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
			T object = responseEntity.getBody();
			LOG.trace("Response Received:");
			LOG.trace(() -> marshal(object));
			return object;
		} catch (Exception exception) {
			throw new RestClientException("Error occurred during GET operation", exception);
		}
	}

	/**
	 * This API is to do <code>POST</code> call with passing relevant parameters.
	 * Rest all will be taken care by this API
	 * 
	 * @param serviceOperationName key for service & operation to be invoked as
	 *                             configured in properties
	 * @param uriParams            any uri params which needs to be passed
	 * @param queryParams          any query params which needs to be passed
	 * @param userRef              header to be passed in request
	 * @param correlationId        header to be passed in request
	 * @param xInfRequestId        header to be passed in request
	 * @param request              entity to be sent as part of request
	 * @param responseType
	 * @return response object
	 */
	public <T> T postData(String serviceOperationName, Map<String, String> uriParams, Map<String, String> queryParams, String userRef, String correlationId, String xInfRequestId,
			Object request, Class<T> responseType) {
		try {
			String uri = util.getServiceURI(serviceOperationName);
			String url = createUri(uri, uriParams, queryParams);
			LOG.trace("Calling Webservice : " + serviceOperationName);
			LOG.trace("URI : " + url);
			LOG.trace("Payload : \n{}", () -> marshal(request));
			HttpHeaders headers = util.createHttpHeaders(serviceOperationName, userRef, correlationId, xInfRequestId);
			HttpEntity<?> requestEntity = new HttpEntity<Object>(request, headers);
			ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, requestEntity, responseType);
			T object = responseEntity.getBody();
			LOG.trace("Response Received : \n{}", () -> marshal(object));
			return object;
		} catch (HttpClientErrorException exception) {
			throw exception;
			// throw new RestClientException("Error occurred during POST operation",
			// exception);
		}
	}

	/**
	 * This API is to do <code>PUT</code> call with passing relevant parameters.
	 * Rest all will be taken care by this API
	 * 
	 * @param serviceOperationName key for service & operation to be invoked as
	 *                             configured in properties
	 * @param uriParams            any uri params which needs to be passed
	 * @param queryParams          any query params which needs to be passed
	 * @param userRef              header to be passed in request
	 * @param correlationId        header to be passed in request
	 * @param xInfRequestId        header to be passed in request
	 * @param request              entity to be sent as part of request
	 */
	public <T> T putData(String serviceOperationName, Map<String, String> uriParams, Map<String, String> queryParams, String userRef, String correlationId, String xInfRequestId,
			Object request, Class<T> responseType) {
		try {
			String uri = util.getServiceURI(serviceOperationName);
			String url = createUri(uri, uriParams, queryParams);
			LOG.trace("Calling Webservice : " + serviceOperationName);
			LOG.trace("URI : " + url);
			LOG.trace("Payload : \n{}", () -> marshal(request));
			HttpHeaders headers = util.createHttpHeaders(serviceOperationName, userRef, correlationId, xInfRequestId);
			HttpEntity<?> requestEntity = new HttpEntity<Object>(request, headers);
			ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, responseType);
			T object = responseEntity.getBody();
			LOG.trace("Response Received : \n{}", () -> marshal(object));
			return object;
		} catch (Exception exception) {
			throw new RestClientException("Error occurred during PUT operation", exception);
		}
	}

	public <T> T postMultiPartData(String serviceOperationName, String metaDataFileName, String csvFileName, String csvPayload, Map<String, String> uriParams,
			Map<String, String> queryParams, String userRef, String correlationId, String xInfRequestId, Object request, Class<T> responseType) {
		try {
			String uri = util.getServiceURI(serviceOperationName);
			String url = createUri(uri, uriParams, queryParams);
			// String serviceAccept = util.getServiceAccept(serviceOperationName);
			String servicePayloadMediaType = util.getServicePayloadMediaType(serviceOperationName);
			LOG.trace("Calling Webservice : " + serviceOperationName);
			LOG.trace("URI : " + url);
			HttpHeaders headers = util.createHttpHeaders(serviceOperationName, userRef, correlationId, xInfRequestId);

			List<MediaType> acceptList = headers.getAccept();
			if (!acceptList.isEmpty()) {
				acceptList.clear();
			}
			headers.setAccept(Arrays.asList(util.getServiceAccept(serviceOperationName)));

			MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

			HttpHeaders metaDataFileHeader = new HttpHeaders();
			metaDataFileHeader.setContentType(util.getServiceAccept(serviceOperationName));

			String storeDocumentRequestString = (String) request;
			ByteArrayResource metadataAsResource = new ByteArrayResource(storeDocumentRequestString.getBytes()) {
				@Override
				public String getFilename() {
					return metaDataFileName;
				}
			};
			HttpEntity<?> metaDataPart = new HttpEntity<>(metadataAsResource, metaDataFileHeader);

			HttpHeaders txtFileHeader = new HttpHeaders();
			txtFileHeader.setContentType(MediaType.valueOf(servicePayloadMediaType));
			ByteArrayResource txtAsResource = new ByteArrayResource(csvPayload.getBytes()) {
				@Override
				public String getFilename() {
					return csvFileName;
				}
			};
			HttpEntity<?> textPart = new HttpEntity<>(txtAsResource, txtFileHeader);

			multipartRequest.add(metaDataFileName, metaDataPart);
			multipartRequest.add(csvFileName, textPart);

			HttpEntity<?> requestEntity = new HttpEntity<Object>(multipartRequest, headers);

			List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
			messageConverters.add(new ByteArrayHttpMessageConverter());

			ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, requestEntity, responseType);
			T object = responseEntity.getBody();

			LOG.trace("Response Received : \n{}", () -> marshal(object));
			return object;

		} catch (Exception exception) {
			exception.printStackTrace();
			throw new RestClientException("Error occurred during POST operation", exception);
		}
	}

	private String createUri(String uri, Map<String, String> uriParams, Map<String, String> queryParams) {
		String finalURI = null;
		UriComponentsBuilder builder = null;
		MultiValueMap<String, String> params = null;
		if (null != queryParams) {
			params = new LinkedMultiValueMap<>();
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				params.put(entry.getKey(), Arrays.asList(entry.getValue()));
			}
			builder = UriComponentsBuilder.fromUriString(uri).queryParams(params);
		} else {
			builder = UriComponentsBuilder.fromUriString(uri);
		}
		if (null != uriParams) {
			finalURI = builder.buildAndExpand(uriParams).toString();
		} else {
			finalURI = builder.build().toString();
		}
		return finalURI;
	}

}
