/**
 * 
 */
package com.qaelabs.funcTest.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

/**
 * @author dekag Generate HTMl report from the cucumber generated json files
 *
 */
public class ReportingUtil {

	static List<String> jsonFiles(String directory) {
		List<String> jsonFiles = new ArrayList<String>();
		File dir = new File(directory);
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith((".json"))) {
				jsonFiles.add("target/" + file.getName());
			}
		}

		return jsonFiles;
	}

	public static void main(String args[]) throws Exception {
		File reportOutputDirectory = new File("target/CucumberReports");
		List<String> jsonFiles = jsonFiles("target");
		String buildNumber = "1";
		String projectName = "My Project";
		boolean runWithJenkins = true;
		boolean parallelTesting = false;

		Configuration configuration = new Configuration(reportOutputDirectory, projectName);

		configuration.setParallelTesting(parallelTesting);
		configuration.setRunWithJenkins(runWithJenkins);
		configuration.setBuildNumber(buildNumber);

		ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
		reportBuilder.generateReports();

	}

}
