package com.qaelabs.connector;

/**
 * @author dekag This Class builds SOAP web service request for the SOAP web
 *         service client. {@link SOAPRequestBuilder} helps in building Object
 *         for {@code SOAPRequest}
 */
public class SOAPRequest {

	private String serviceName;

	private String userRef;

	private String xInfReqId;

	private String correlationID;

	private Object requestPayload;

	public String getServiceName() {
		return serviceName;
	}

	public String getUserRef() {
		return userRef;
	}

	public String getxInfReqId() {
		return xInfReqId;
	}

	public String getCorrelationID() {
		return correlationID;
	}

	public Object getRequestPayload() {
		return requestPayload;
	}

	private SOAPRequest(String serviceName, String userRef, String xInfReqId, String correlationID, Object requestPayload) {
		super();
		this.serviceName = serviceName;
		this.userRef = userRef;
		this.xInfReqId = xInfReqId;
		this.correlationID = correlationID;
		this.requestPayload = requestPayload;
	}

	/**
	 * Builds Object for the {@link SOAPRequest}
	 */
	public static class SOAPRequestBuilder {

		private String serviceName;

		private String userRef;

		private String xInfReqId;

		private String correlationID;

		private Object requestPayload;

		public SOAPRequestBuilder(String serviceName, Object requestPayload) {
			super();
			this.serviceName = serviceName;
			this.requestPayload = requestPayload;
		}

		public SOAPRequestBuilder setuserRef(String userRef) {
			this.userRef = userRef;
			return this;
		}

		public SOAPRequestBuilder setxInfReqId(String xInfReqId) {
			this.xInfReqId = xInfReqId;
			return this;
		}

		public SOAPRequestBuilder setcorrelationID(String correlationID) {
			this.correlationID = correlationID;
			return this;
		}

		public SOAPRequestBuilder setRequestPayload(Object requestPayload) {
			this.requestPayload = requestPayload;
			return this;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public SOAPRequest buildRequest() {
			return new SOAPRequest(serviceName, userRef, xInfReqId, correlationID, requestPayload);
		}
	}
}
