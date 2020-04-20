package com.hera.plagium_finder.util;

import java.util.Comparator;

public enum StringNumComparator implements Comparator<String> {
	INSTANCE;

	@Override
	public int compare(String string1, String string2) {
		return StringNumCompare.compareStrings(string1, string2);
	}
}