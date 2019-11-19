package com.qaelabs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author dekag Utility class for generic XML operations
 *
 */
public class XMLUtil {

	public static final Logger log = LogManager.getLogger(XMLUtil.class);

	/**
	 * @param object
	 * @return String of Object
	 */
	public static String marshal(Object object) {
		StringWriter sw = new StringWriter();
		JAXB.marshal(object, sw);
		return sw.toString();
	}

	/**
	 * @param xml
	 * @param objectClass
	 * @return T
	 */
	public static <T> T unmarshal(String xml, Class<T> objectClass) {
		return JAXB.unmarshal(new StringReader(xml), objectClass);
	}

	/**
	 * @param path
	 * @param objectClass
	 * @return T
	 */
	public static <T> T unmarshalFile(String path, Class<T> objectClass) {
		try {
			return JAXB.unmarshal(new FileReader(new File(path)), objectClass);
		} catch (FileNotFoundException e) {
			log.warn("Error in Marshalling " + e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * @param fileSource
	 * @param object
	 */
	public static <T> void marhsalToFile(String fileSource, Object object) {
		try {
			FileWriter fw = new FileWriter(fileSource);
			JAXB.marshal(object, fw);
		} catch (IOException e) {
			log.warn("Error in Marshalling " + e.getLocalizedMessage());
		}
	}
}
