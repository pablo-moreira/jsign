package com.github.jsign.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


public class StringUtils {

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	public static boolean isNullOrEmpty(char[] password) {
		return (password == null || password.length == 0);
	}
	
	public static String join(String[] itens, String delimiter) {
		return join(Arrays.asList(itens), delimiter);	
	}
	
	public static String join(Collection<String> itens, String delimiter) {
		
		if (itens == null || itens.isEmpty()) return "";
		
		Iterator<String> iter = itens.iterator();
		
		StringBuilder builder = new StringBuilder(iter.next());

		while(iter.hasNext()) {
			builder.append(delimiter).append(iter.next());
		}
		
		return builder.toString();
	}
}