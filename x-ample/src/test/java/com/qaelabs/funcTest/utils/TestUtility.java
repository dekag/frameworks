package com.qaelabs.funcTest.utils;

import org.springframework.stereotype.Component;

/**
 * @author dekag Generic utility class for basic validation methods
 *
 */
@Component
public class TestUtility {

	/**
	 * @param valArr
	 * @param index
	 * @return value from the String array for the given index
	 */
	public String retrieveValue(String[] valArr, int i) {
		try {
			return valArr[i].trim();
		} catch (IndexOutOfBoundsException iex) {

		}
		return valArr[0];
	}

	/**
	 * @param actualValues
	 * @param expectedValue
	 * @return true if the value is same else false
	 */
	public boolean checkExpectedValue(String actualValues, String expectedValue) {
		if (actualValues.equalsIgnoreCase(expectedValue)) {
			return true;
		}
		return false;
	}
}
