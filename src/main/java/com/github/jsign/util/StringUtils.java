package com.github.jsign.util;


public class StringUtils {

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	public static boolean isNullOrEmpty(char[] password) {
		return (password == null || password.length == 0);
	}
}