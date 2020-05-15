package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.jplag.JplagResults;
import com.hera.plagium_finder.util.ExternalProgramOutput;
import com.hera.plagium_finder.util.ExternalResourceUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static com.hera.plagium_finder.common.IntegrationMode.*;
import static com.hera.plagium_finder.common.Language.TEXT;
import static com.hera.plagium_finder.util.ExternalResourceUtil.callExternalProgram;
import static java.lang.Math.round;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class SimPlagiarismFinder {
	private final StarterDto starterDto;
	private final JplagResults jplagResults;

	public SimPlagiarismFinder(StarterDto starterDto, JplagResults jplagResults) {
		this.starterDto = starterDto;
		this.jplagResults = jplagResults;
	}

	public void findPlagiarism() {
		List<String> submissions = ExternalResourceUtil.getDirectories("./submissions");
		ParsedFileHandler parsedFileHandler = findSimilaritiesBetweenSubmissions(submissions);
		System.out.println("Processing SIM results started");
		markCommonCode(parsedFileHandler);
		getRidOfNotSignificantMatches(parsedFileHandler);
		keepBetterResultForOppositeComparisonResults(parsedFileHandler);
		writeOutput(parsedFileHandler);
	}

	private void keepBetterResultForOppositeComparisonResults(ParsedFileHandler parsedFileHandler) {
		System.out.println("Keeping higher result for opposite measures");
		parsedFileHandler.getComparisonResults().removeAll(parsedFileHandler.getComparisonResults().stream()
						.filter(comparisonResult -> parsedFileHandler.getComparisonResults().contains(comparisonResult.getOppositeComparisonResult()))
						.filter(comparisonResult -> {
							double comparisonResultPercentage = comparisonResult.getPercentage();
							double oppositeComparisonResultPercentage = comparisonResult.getOppositeComparisonResult().getPercentage();
							return comparisonResultPercentage < oppositeComparisonResultPercentage
											|| ( comparisonResultPercentage == oppositeComparisonResultPercentage && comparisonResult.getSubmission().getPublisher().compareTo(comparisonResult.getOppositeComparisonResult().getSubmission().getPublisher()) >= 0);
						})
						.collect(toList()));
	}

	private void markCommonCode(ParsedFileHandler parsedFileHandler) {
		Map<String, Set<ComparisonResult>> commonCodeBlocksFound = new HashMap<>();
		parsedFileHandler.getComparisonResults()
						.forEach(comparisonResult -> comparisonResult.getSplitCommonCodeBlocksWithTokenSize()
										.forEach(splitCommonCodeBlocksWithTokenSize -> commonCodeBlocksFound.compute(splitCommonCodeBlocksWithTokenSize, (block, comparisonResults) -> {
											comparisonResults = ofNullable(comparisonResults)
															.orElseGet(HashSet::new);
											comparisonResults.add(comparisonResult);
											return comparisonResults;
										})));
		if (StringUtils.isNotEmpty(starterDto.getNameOfPublicSubmission())) {
			markPublicCodeUsage(parsedFileHandler, commonCodeBlocksFound);
		}
		markTooManyOccurrences(commonCodeBlocksFound);
	}

	private void markPublicCodeUsage(ParsedFileHandler parsedFileHandler, Map<String, Set<ComparisonResult>> commonCodeBlocksFound) {
		System.out.println("Finding usages of public submission's source code");
		Set<String> publicCodesFound = parsedFileHandler.getPublicComparisonResults().stream()
						.flatMap(comparisonResult -> comparisonResult.getSplitCommonCodeBlocksWithTokenSize().stream())
						.collect(toSet());
		publicCodesFound
						.forEach(publicCodeFound -> commonCodeBlocksFound.remove(publicCodeFound).forEach(comparisonResult -> comparisonResult.markPublicCode(publicCodeFound)));
	}

	private void markTooManyOccurrences(Map<String, Set<ComparisonResult>> commonCodeBlocksFound) {
		System.out.println("Finding similarities over " + starterDto.getMaximumMatchOccurrence() + " occurrences");
		commonCodeBlocksFound.entrySet().stream()
						.filter(commonCodeBlockFound -> commonCodeBlockFound.getValue().size() - commonCodeBlockFound.getValue().stream()
										.filter(comparisonResult -> commonCodeBlockFound.getValue().contains(comparisonResult.getOppositeComparisonResult()))
										.count() / 2 > starterDto.getMaximumMatchOccurrence())
						.forEach(commonCodeBlockFound -> commonCodeBlockFound.getValue().forEach(comparisonResult -> comparisonResult.markCommonCode(commonCodeBlockFound.getKey())));
	}

	private void getRidOfNotSignificantMatches(ParsedFileHandler parsedFileHandler) {
		System.out.println("Selecting significant matches");
		parsedFileHandler.getComparisonResults().removeIf(comparisonResult -> !comparisonResult.significantMatch());
	}

	private void writeOutput(ParsedFileHandler parsedFileHandler) {
		System.out.println("Extending JPlag's output");
		StringBuilder lines = new StringBuilder();
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader("./result/index.html"))) {
			while ((line = br.readLine()) != null) {
				lines.append(line);
			}
		}
		catch (IOException e) {
			System.out.println("Reading jplag results failed: " + e.getMessage());
		}

		int whereToPutNewTable = lines.lastIndexOf("<HR>");
		MatchFileMaker matchFileMaker = new MatchFileMaker();
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculateNrOfMatchingTokens, "number of matching tokens", false));
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculatePercentage, "similarity percentage", true));
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./result/index.html"))) {
			writer.write(lines.toString());
		}
		catch (IOException e) {
			System.out.println("Changing jplag results failed: " + e.getMessage());
		}
	}

	private String reportMatchesFoundBySim(ParsedFileHandler parsedFileHandler, MatchFileMaker matchFileMaker, ToDoubleFunction<ComparisonResult> similarityCalculator, final String similarityType, boolean percentageNeeded) {
		Map<Submission, List<ComparisonResult>> comparisonResultsByPublisher = parsedFileHandler.getComparisonResults().stream()
						.filter(comparisonResult -> starterDto.getIntegrationMode() != HIGHER_ONLY
						|| jplagResults.getPercentageOfSimilarity(comparisonResult.getSubmission(), comparisonResult.getOtherSubmission()) < comparisonResult.getPercentage())
						.collect(Collectors.groupingBy(ComparisonResult::getSubmission));
		Set<Submission> alreadyReportedSubmissions = new HashSet<>();
		return "<HR><H4>Matches sorted by " + similarityType + " found by SIM:</H4>\n"
						+ "<TABLE CELLPADDING=3 CELLSPACING=2>"
						+ parsedFileHandler.getComparisonResults().stream()
						.filter(comparisonResult -> isNotEmpty(comparisonResult.getCommonCodeLines()))
						.filter(comparisonResult -> comparisonResultsByPublisher.containsKey(comparisonResult.getSubmission()))
						.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
						.filter(comparisonResult -> alreadyReportedSubmissions.add(comparisonResult.getSubmission()))
						.map(comparisonResult -> "<TR>"
										+ "<TD BGCOLOR=#9ACD32>" + comparisonResult.getSubmission() + "</TD><TD><nobr>-&gt;</nobr></TD>"
										+ comparisonResultsByPublisher.get(comparisonResult.getSubmission()).stream()
										.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
										.map(comparisonResultForTheSamePublisher -> "<TD BGCOLOR=#9ACD32 ALIGN=center><A HREF=\"sim_match" + matchFileMaker.createMatchFile(comparisonResultForTheSamePublisher) + ".html\">"
														+ comparisonResultForTheSamePublisher.getOtherSubmission()
														+ "</A><BR><FONT COLOR=\"#ff0000\">(" + similarityCalculator.applyAsDouble(comparisonResultForTheSamePublisher) + (percentageNeeded ? "%" : "") + ")</FONT></TD>")
										.collect(joining(""))
										+ "</TR>")
						.collect(Collectors.joining("\r\n"))
						+ "</TABLE><P>\n";
	}

	private static double calculateNrOfMatchingTokens(ComparisonResult comparisonResult) {
		return comparisonResult.getNrOfMatchingTokens();
	}

	private static double calculatePercentage(ComparisonResult comparisonResult) {
		return roundToDecimals(comparisonResult.getPercentage(), 1);
	}

	private static double roundToDecimals(double num, int nrOfDecimals) {
		double modifier = Math.pow(10, nrOfDecimals);
		return round(num * modifier) / modifier;
	}

	private ParsedFileHandler findSimilaritiesBetweenSubmissions(List<String> submissions) {
		System.out.println("Calling SIM for each submission pairs");
		ParsedFileHandler parsedFileHandler = new ParsedFileHandler();
		int nrOfComparisons = (submissions.size() - 1) * submissions.size();
		System.out.println("SIM's progress: 0 %");
		int comparisonCounter = 0;
		for (int i = 0; i < submissions.size() - 1; ++i) {
			for (int j = i + 1; j < submissions.size(); ++j) {
				++comparisonCounter;
				Submission submission = new Submission(submissions.get(i));
				Submission otherSubmission = new Submission(submissions.get(j));
				if (starterDto.getIntegrationMode() == NO_DUPLICATES && jplagResults.getPercentageOfSimilarity(submission, otherSubmission) != 0d) {
					continue;
				}
				try {
					ComparisonResult comparisonResult = calculateSimilarity(parsedFileHandler, submission, otherSubmission);
					ComparisonResult oppositeComparisonResult = calculateSimilarity(parsedFileHandler, otherSubmission, submission);
					if (comparisonResult != null && oppositeComparisonResult != null) {
						ComparisonResult.setOppositeComparisonResults(comparisonResult, oppositeComparisonResult);
					}
				} catch (Exception e) {
					System.out.println("Some issue happened when comparing: " + submission.getPublisher() + " and " + otherSubmission.getPublisher() + ". They are skipped.");
					e.printStackTrace();
				}

			}
			System.out.println("SIM's progress: " + roundToDecimals((comparisonCounter / (double) nrOfComparisons * 200), 3) + " %");
		}
		System.out.println("SIM finished");
		return parsedFileHandler;
	}

	private ComparisonResult calculateSimilarity(ParsedFileHandler parsedFileHandler, Submission submission, Submission otherSubmission) {
		ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
						+ starterDto.getLanguage().simProgram
						+ " -r "
						+ starterDto.getPrecision().getSimMinimumRunLength()
						+ " -S -w " + starterDto.getPageWidth() + " -R \"./submissions/"
						+ submission
						+ "/*\" \"|\" \"./submissions/"
						+ otherSubmission
						+ "/*\"", false, false);
		ComparisonResult comparisonResult = new ComparisonResult(starterDto.getPrecision());
		parsedFileHandler.addComparisonResult(comparisonResult);
		if (isPublicSubmission(submission) || isPublicSubmission(otherSubmission)) {
			parsedFileHandler.addPublicComparisonResult(comparisonResult);
		}
		boolean newFiles = true;
		for (String out : externalProgramOutput.getStdOut()) {
			if (out.startsWith("File |: new/old separator")) {
				newFiles = false;
			}
			if (out.startsWith("File ./")) {
				if (newFiles) {
					comparisonResult.addParsedFile(createParsedFileIfNew(parsedFileHandler, out));
				}
				else {
					comparisonResult.addOtherParsedFile(createParsedFileIfNew(parsedFileHandler, out));
				}
			}
			if (comparisonResult.getParsedFileNames().stream()
							.anyMatch(parsedFileName -> out.startsWith(parsedFileName + ": line"))) {
				comparisonResult.addCommonCodeBlock(out);
			}
			if (isNotEmpty(comparisonResult.getCommonCodeBlocks())) {
				comparisonResult.addCommonCodeLine(out);
			}
		}

		if (isNotEmpty(externalProgramOutput.getStdErr())) {
			System.out.println("Comparing submissions " + submission + " and " + otherSubmission + " failed: " + String.join("\r\n", externalProgramOutput.getStdErr()));
			return null;
		}

		return comparisonResult;
	}

	private boolean isPublicSubmission(Submission submission) {
		return submission.getPublisher().equals(starterDto.getNameOfPublicSubmission());
	}

	private ParsedFile createParsedFileIfNew(ParsedFileHandler parsedFileHandler, String out) {
		int fileNameEnd = out.indexOf(": ");
		return parsedFileHandler.createParsedFileIfNew(out.substring(5, fileNameEnd), Integer.parseInt(out.substring(fileNameEnd + 2, fileNameEnd + out.substring(fileNameEnd).lastIndexOf(starterDto.getLanguage() == TEXT ? " word" : " token"))));
	}
}
