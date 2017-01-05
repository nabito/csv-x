package com.dadfha;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	
	/**
	 * Try to get the String from matched pattern group out of a regular expression matching.
	 * @param targetGroup in int starting from 1.
	 * @param regex
	 * @param text 
	 * @return the matched group String or null if the whole regular expression or just the target group 
	 * doesn't match. 
	 */
	public static String getRegExGroup(int targetGroup, String regex, String text) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			return matcher.group(targetGroup);
		}
		return null;
	}
}
