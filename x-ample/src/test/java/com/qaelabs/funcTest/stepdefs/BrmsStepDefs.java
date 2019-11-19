/**
 * 
 */
package com.qaelabs.funcTest.stepdefs;

import org.springframework.beans.factory.annotation.Autowired;

import com.qaelabs.connector.RestConnector;
import com.qaelabs.connector.SOAPConnector;
import com.qaelabs.funcTest.services.BrmsUtility;
import com.qaelabs.funcTest.utils.ScenarioContext;

import io.cucumber.java8.En;

/**
 * @author dekag Brms step def class for brms implementation (Sample Test Only)
 *
 */
public class BrmsStepDefs implements En {

	@Autowired
	BrmsUtility brmsUtility;

	@Autowired
	RestConnector restConnector;

	@Autowired
	SOAPConnector soapConnector;

	@Autowired
	public BrmsStepDefs(ScenarioContext sc) {
		When("system runs {string} request for {string} rule validation", (String requestType, String opType) -> {
			switch (requestType.trim().toLowerCase()) {
			case "rest":
				brmsUtility.executeRestServiceRequest(opType, sc, restConnector);
				break;
			case "soap":
				brmsUtility.executeSoapServiceRequest(opType, sc, soapConnector);
				break;
			}

		});
		When("system runs {string} request for {string} validation", (String requestType, String opType) -> {
			switch (requestType.trim().toLowerCase()) {
			case "rest":
				brmsUtility.executeJsonRestServiceRequest(opType, sc, restConnector);
				break;
			case "get":
				brmsUtility.executeGetRequest(opType, sc, restConnector);
				break;
			}

		});

	}

}
