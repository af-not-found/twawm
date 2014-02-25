package com.appspot.afnf4199ga.utils;

public class MyStringUtlis {

	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}
		else if (str.length() == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean eqauls(String s1, String s2) {
		boolean b1 = isEmpty(s1);
		boolean b2 = isEmpty(s2);
		if (b1 && b2) {
			return true;
		}
		else if (b1 || b2) {
			return false;
		}
		return s1.equals(s2);
	}

	public static String normalize(String str) {
		if (isEmpty(str)) {
			return "";
		}

		StringBuilder sb = new StringBuilder(str.length());
		char[] charArray = str.toCharArray();
		for (char d : charArray) {
			if (d == ' ' || d == '\t' || d == '\n' || d == '\r') {
				// do nothing
			}
			else if (d == '　') {
				sb.append(" ");
			}
			else {
				sb.append(d);
			}
		}
		return sb.toString();
	}

	public static int count(String str, char c) {
		if (isEmpty(str)) {
			return 0;
		}

		int count = 0;
		char[] charArray = str.toCharArray();
		for (char d : charArray) {
			if (d == c) {
				count++;
			}
		}
		return count;
	}

	public static String replaceFirst(String str, String from, String to) {
		if (isEmpty(str) || isEmpty(from)) {
			return str;
		}
		return str.replaceFirst(from, to != null ? to : "");
	}

	public static String subStringBefore(String str, String sep) {
		if (isEmpty(str)) {
			return "";
		}

		int pos = str.indexOf(sep);
		if (pos == -1) {
			return str;
		}
		else {
			return str.substring(0, pos);
		}
	}
}
