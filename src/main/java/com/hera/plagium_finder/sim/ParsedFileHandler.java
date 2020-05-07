package com.hera.plagium_finder.sim;

import java.util.LinkedList;
import java.util.List;

public class ParsedFileHandler {
	private List<ParsedFile> parsedFiles = new LinkedList<>();
	private List<ComparisonResult> comparisonResults = new LinkedList<>();
	private List<ComparisonResult> publicComparisonResults = new LinkedList<>();

	public ParsedFile createParsedFileIfNew(String path, int nrOfTokens) {
		ParsedFile parsedFile;
		try {
			parsedFile =  getParsedFile(path);
		}
		catch (IllegalArgumentException e) { //parsed file does not exists
			parsedFile = new ParsedFile(path, nrOfTokens);
			parsedFiles.add(parsedFile);
		}
		return parsedFile;
	}

	private ParsedFile getParsedFile(String parsedFileName) {
		return parsedFiles.stream()
						.filter(parsedFile -> parsedFile.getName().equals(parsedFileName))
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Parsed file does not exists: " + parsedFileName));
	}

	public List<ComparisonResult> getComparisonResults() {
		return comparisonResults;
	}

	public List<ComparisonResult> getPublicComparisonResults() {
		return publicComparisonResults;
	}

	public void addComparisonResult(ComparisonResult comparisonResult) {
		comparisonResults.add(comparisonResult);
	}

	public void addPublicComparisonResult(ComparisonResult comparisonResult) {
		publicComparisonResults.add(comparisonResult);
	}
}
