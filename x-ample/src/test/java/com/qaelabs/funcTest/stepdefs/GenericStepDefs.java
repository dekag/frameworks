package com.qaelabs.funcTest.stepdefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.qaelabs.funcTest.configuration.SpringIntegrationTest;
import com.qaelabs.funcTest.services.GenericStepUtility;
import com.qaelabs.funcTest.utils.GuidUtility;
import com.qaelabs.funcTest.utils.ScenarioContext;

import io.cucumber.core.api.Scenario;
import io.cucumber.java8.En;

/**
 * @author dekag Generic step def class for basic operations
 *
 */
public class GenericStepDefs extends SpringIntegrationTest implements En {

	@Autowired
	GenericStepUtility genericStepUtility;

	@Autowired
	Environment propData;

	private static final Logger LOG = LogManager.getLogger(GenericStepDefs.class);

	@Autowired
	public GenericStepDefs(ScenarioContext sc) {
		Before((Scenario scenario) -> {
			LOG.info("Executing " + scenario.getName() + " id " + scenario.getId() + " tags " + scenario.getSourceTagNames());
			sc.setContext("guid", GuidUtility.generateGUID());
			String str = "Guid for the operation is: " + sc.getContext("guid");
			LOG.info("GUID " + str);
			scenario.embed(str.getBytes(), "text/plain");
		});
		After((Scenario scenario) -> {
			LOG.info(scenario.getName() + "-> " + scenario.getStatus());
		});

		Given("the template {string} is prepared", (String templateName) -> {
			genericStepUtility.createXMLDoc(templateName, propData.getProperty("baseTemplatesPath"));
		});
		Given("the base template {string} is prepared", (String templateName) -> {
			genericStepUtility.createJsonRequest(templateName, propData.getProperty("baseTemplatesPath"));
		});
		Given("set the {string} of/for/in {string} is {string}", (String key, String coveringElement, String values) -> {
			genericStepUtility.setElementValue(key, coveringElement, values);
		});
		Given("set the node {string} of/for/in {string} is {string}", (String key, String coveringElement, String values) -> {
			genericStepUtility.setJsonElementValue(key, coveringElement, values);
		});
		Then("validate the {string} of/for/in {string} is {string}", (String key, String coveringElement, String values) -> {
			genericStepUtility.validateElementValue(key, coveringElement, values, sc);
		});
		Then("validate the node {string} of/for/in {string} is {string}", (String key, String coveringElement, String values) -> {
			genericStepUtility.validateJsonNodeValue(key, coveringElement, values, sc);
		});
		Then("validate the {string} of/for/in {string} is {string} if/for/in/of {string} is {string}",
				(String key, String coveringElement, String values, String tagKey, String tagValue) -> {
					genericStepUtility.validateElementValue("value@" + tagKey + "~" + tagValue + "@" + key, coveringElement, values, sc);
				});

	}

}
