package com.hera.plagium_finder.sim;

import java.util.HashSet;
import java.util.Set;

public class Measure {
	private final Set<ParsedFile> parsedFiles = new HashSet<>();
	private final Set<ComparisonResult> comparisonResults = new HashSet<>();

	public void addParsedFile(ParsedFile parsedFile) {
		parsedFiles.add(parsedFile);
	}

	public void addComparsionResult(ComparisonResult comparisonResult) {
		comparisonResults.add(comparisonResult);
	}

	public boolean significantSimilarity() {
		double sumOfMatchingTokens = comparisonResults.stream()
						.filter(ComparisonResult::significantMatch)
						.mapToDouble(ComparisonResult::getNrOfMatchingTokens)
						.sum() * 2;
		double sumOfTokens = parsedFiles.stream()
						.mapToDouble(ParsedFile::getNrOfTokens)
						.sum();
		return (sumOfMatchingTokens / sumOfTokens) > 0.4;
	}

	public Set<ComparisonResult> getComparisonResults() {
		return comparisonResults;
	}
}
