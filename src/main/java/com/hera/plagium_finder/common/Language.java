package com.hera.plagium_finder.common;

import java.util.List;

import static java.util.Arrays.asList;

public enum Language {
	JAVA11("java11", "sim_java.exe", asList(".java", ".JAVA")),
	JAVA12("java12", "sim_java.exe", asList(".java", ".JAVA")),
	JAVA15("java15", "sim_java.exe", asList(".java", ".JAVA")),
	JAVA17("java17", "sim_java.exe", asList(".java", ".JAVA")),
	JAVA19("java19", "sim_java.exe", asList(".java", ".JAVA")),
	CPP("c/c++", "sim_c++.exe", asList(".cpp", ".CPP", ".cxx", ".CXX", ".c++", ".C++", ".c", ".C", ".cc", ".CC", ".h", ".H", ".hpp", ".HPP", ".hh", ".HH")),
	C("c/c++", "sim_c.exe", asList(".c", ".C", ".cc", ".CC", ".h", ".H", ".hpp", ".HPP", ".hh", ".HH")),
	TEXT("text", "sim_text.exe", asList(".TXT", ".txt", ".ASC", ".asc", ".TEX", ".tex"));

	public final String jplagParameter;
	public final String simProgram;
	public final List<String> fileExtensions;

	Language(String jplagParameter, String simProgram, List<String> fileExtensions) {
		this.jplagParameter = jplagParameter;
		this.simProgram = simProgram;
		this.fileExtensions = fileExtensions;
	}
}
