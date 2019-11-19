package com.qaelabs.funcTest.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = { "pretty" }, 
		monochrome = true, 
		features = "src/test/resources/features", 
		tags = { "@SampleGet" },
		glue= {"com.qaelabs.funcTest.stepdefs"}

)

public class CucumberRunner {

}
