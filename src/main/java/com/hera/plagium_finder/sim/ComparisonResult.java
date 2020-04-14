package com.hera.plagium_finder.sim;

import java.util.LinkedList;
import java.util.List;

public class ComparisonResult {
	private ParsedFile parsedFile;
	private ParsedFile otherParsedFile;
	private double percentage;
	private List<String> commonCodeLines = new LinkedList<>();

	public ComparisonResult(ParsedFile parsedFile, ParsedFile otherParsedFile, double percentage) {
		this.parsedFile = parsedFile;
		this.otherParsedFile = otherParsedFile;
		this.percentage = percentage;
	}

	public boolean significantMatch() {
		return getParsedFile().getNrOfTokens() >= 80 && getOtherParsedFile().getNrOfTokens() >= 80 && (percentage >= 30 || getNrOfMatchingTokens() >= 50);
	}

	public double getNrOfMatchingTokens() {
		return otherParsedFile.getNrOfTokens() * percentage / 100;
	}

	public void addCommonCodeLine(String commonCodeLine) {
		commonCodeLines.add(commonCodeLine);
	}

	public ParsedFile getParsedFile() {
		return parsedFile;
	}

	public ParsedFile getOtherParsedFile() {
		return otherParsedFile;
	}

	public double getPercentage() {
		return percentage;
	}

	public List<String> getCommonCodeLines() {
		return commonCodeLines;
	}
}
