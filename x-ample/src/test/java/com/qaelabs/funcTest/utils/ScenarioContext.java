/**
 * 
 */
package com.qaelabs.funcTest.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dekag Use ScenarioContext class to store scenario specific data
 */
public class ScenarioContext {

	private Map<String, Object> scenarioContext;

	public ScenarioContext() {
		scenarioContext = new HashMap<String, Object>();
	}

	public void setContext(String key, Object value) {
		scenarioContext.put(key, value);
	}

	public Object getContext(String key) {
		return scenarioContext.get(key.toString());
	}

	public Boolean isContains(String key) {
		return scenarioContext.containsKey(key.toString());
	}

}
