package com.qaelabs.funcTest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.qaelabs.funcTest.services.GenericStepUtility;
import com.qaelabs.funcTest.utils.ScenarioContext;
import com.qaelabs.funcTest.services.BrmsUtility;
import com.qaelabs.funcTest.services.BaseTemplate;

/**
 * @author dekag Base Configuration class
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = { "com.qaelabs.*" })
public class BaseConfiguration {

	@Bean
	public ScenarioContext scenarioContext() {
		return new ScenarioContext();
	}

	@Bean
	public GenericStepUtility genericStepUtility() {
		return new GenericStepUtility(baseTemplate());
	}

	@Bean
	public BrmsUtility brmsUtility() {
		return new BrmsUtility(baseTemplate());
	}

	@Bean
	public BaseTemplate baseTemplate() {
		return new BaseTemplate();
	}
}
