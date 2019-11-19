/**
 * 
 */
package com.qaelabs.funcTest.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author dekag Utility to create GUID at runtime
 */
public class GuidUtility {
	private static SecureRandom random = new SecureRandom();

	private static String systemId = "QA_SM_Test";

	public static String generateGUID() {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			long time = System.currentTimeMillis();
			long rand = random.nextLong();
			StringBuilder sbValueBeforeMD5 = new StringBuilder();
			sbValueBeforeMD5.append(systemId);
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(rand));
			if (md5 != null) {
				StringBuilder sb = new StringBuilder();
				md5.update(sbValueBeforeMD5.toString().getBytes());
				byte[] array = md5.digest();
				for (byte element : array) {
					int b = element & 0xFF;
					if (b < 0x10) {
						sb.append('0');
					}
					sb.append(Integer.toHexString(b));
				}
				return convertToGUIDString(sb.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error occurred during generation of GUID" + e);
		}
		return null;
	}

	private static String convertToGUIDString(String str) {
		String raw = str.toUpperCase();
		StringBuilder sb = new StringBuilder();
		sb.append(raw.substring(0, 8));
		sb.append("-");
		sb.append(raw.substring(8, 12));
		sb.append("-");
		sb.append(raw.substring(12, 16));
		sb.append("-");
		sb.append(raw.substring(16, 20));
		sb.append("-");
		sb.append(raw.substring(20));
		return sb.toString();
	}

}
