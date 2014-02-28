package com.bkahlert.nebula.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static String md5(String text) {
		try {
			return hash("MD5", text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String hash(String algorithm, String text)
			throws NoSuchAlgorithmException {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm
					.toUpperCase());
			byte[] hash = md.digest(text.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
