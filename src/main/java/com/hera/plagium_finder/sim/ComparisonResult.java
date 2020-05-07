package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.Precision;
import com.hera.plagium_finder.common.Submission;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class ComparisonResult {
	private final Precision precision;
	private List<ParsedFile> parsedFiles = new LinkedList<>();
	private List<ParsedFile> otherParsedFiles = new LinkedList<>();
	private List<String> commonCodeLines = new LinkedList<>();
	private List<String> commonCodeBlocks = new LinkedList<>();
	private ComparisonResult oppositeComparisonResult;

	public ComparisonResult(Precision precision) {
		this.precision = precision;
	}

	public boolean significantMatch() {
		return (parsedFiles.stream().mapToInt(ParsedFile::getNrOfTokens).sum() >= precision.getSimMinimumNrOfTokens() && getPercentage() >= precision.getMinimumExpectedPercentage())
						|| (double)getNrOfMatchingTokens() >= precision.getSimNrOfMatchingTokensThatMustBeReportedIfFound();
	}

	public double getPercentage() {
		try {
			return getNrOfMatchingTokens() / (double)getNrOfTokens() * 100;
		}
		catch (Exception e) {
			return 0;
		}
	}

	private int getNrOfTokens() {
		return parsedFiles.stream()
						.mapToInt(ParsedFile::getNrOfTokens)
						.sum();
	}

	public List<String> getSplitCommonCodeBlocksWithTokenSize() {
		return commonCodeBlocks.stream()
						.flatMap(commonCodeBlock -> {
							String[] splitByOldNewSeparator = commonCodeBlock.split("\\|.");
							return Stream.of(splitByOldNewSeparator[0], splitByOldNewSeparator[1].split("\\[")[0])
											.map(String::trim);
						})
						.collect(toList());
	}

	public int getNrOfMatchingTokens() {
		return commonCodeBlocks.stream()
						.map(commonCodeBlock -> commonCodeBlock.contains(" - common code -") ||  commonCodeBlock.contains(" - public code -") ? "0" : getTokenSizeOfCommonCodeBlock(commonCodeBlock))
						.mapToInt(Integer::valueOf)
						.sum();
	}

	private static String getTokenSizeOfCommonCodeBlock(String commonCodeBlock) {
		return commonCodeBlock.split("\\[")[1].split("]")[0];
	}

	public void addCommonCodeLine(String commonCodeLine) {
		commonCodeLines.add(commonCodeLine);
	}

	public void addCommonCodeBlock(String commonCodeBlock) {
		commonCodeBlocks.add(commonCodeBlock);
	}

	public List<String> getCommonCodeLines() {
		return commonCodeLines;
	}

	public List<String> getCommonCodeBlocks() {
		return commonCodeBlocks;
	}

	public void addParsedFile(ParsedFile parsedFile) {
		parsedFiles.add(parsedFile);
	}

	public void addOtherParsedFile(ParsedFile otherParsedFile) {
		otherParsedFiles.add(otherParsedFile);
	}

	public List<String> getParsedFileNames() {
		return parsedFiles.stream()
						.map(ParsedFile::getName)
						.collect(toList());
	}

	public Submission getSubmission() {
		return parsedFiles.get(0).getSubmission();
	}

	public Submission getOtherSubmission() {
		return otherParsedFiles.get(0).getSubmission();
	}

	public void markCommonCode(String codeBlockPart) {
		markCode(codeBlockPart, " - common code -");
	}

	public void markPublicCode(String codeBlockPart) {
		markCode(codeBlockPart, " - public code -");
	}

	private void markCode(String codeBlockPart, String mark) {
		List<String> commonCodeBlocksOccurredTooManyTimes = commonCodeBlocks.stream()
						.filter(commonCodeBlock -> commonCodeBlock.contains(codeBlockPart))
						.collect(toList());
		commonCodeBlocksOccurredTooManyTimes.forEach(commonCodeBlockOccurredTooManyTimes -> {
			for (int i = 0; i < commonCodeLines.size(); i++) {
				String commonCodeLine = commonCodeLines.get(i);
				if (commonCodeLine.equals(commonCodeBlockOccurredTooManyTimes)) {
					commonCodeLines.set(i, commonCodeBlockOccurredTooManyTimes + mark);
				}
			}
			commonCodeBlocks.remove(commonCodeBlockOccurredTooManyTimes);
		});
	}

	public static void setOppositeComparisonResults(ComparisonResult comparisonResult, ComparisonResult oppositeComparisonResult) {
		comparisonResult.oppositeComparisonResult = oppositeComparisonResult;
		oppositeComparisonResult.oppositeComparisonResult = comparisonResult;
	}

	public ComparisonResult getOppositeComparisonResult() {
		return oppositeComparisonResult;
	}
}
