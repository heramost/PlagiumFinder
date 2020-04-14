package com.hera.plagium_finder.sim;

import java.util.LinkedList;
import java.util.List;

public class ParsedFileHandler {
	private List<ParsedFile> parsedFiles = new LinkedList<>();
	private List<ComparisonResult> comparisonResults = new LinkedList<>();

	public void createParsedFileIfNew(String path, int nrOfTokens) {
		try {
			getParsedFile(path);
		}
		catch (IllegalArgumentException e) { //parsed file does not exists
			parsedFiles.add(new ParsedFile(path, nrOfTokens));
		}
	}

	public void addPercentage(String parsedFileName, String otherParsedFileName, double percentage) {
		ParsedFile parsedFile = getParsedFile(parsedFileName);
		ParsedFile otherParsedFile = getParsedFile(otherParsedFileName);
		comparisonResults.add(new ComparisonResult(parsedFile, otherParsedFile, percentage));
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
}
