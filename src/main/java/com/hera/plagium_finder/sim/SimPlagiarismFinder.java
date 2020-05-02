package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.jplag.JplagResult;
import com.hera.plagium_finder.util.ExternalProgramOutput;
import com.hera.plagium_finder.util.ExternalResourceUtil;
import com.hera.plagium_finder.util.StringNumComparator;

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

import static com.hera.plagium_finder.util.ExternalResourceUtil.callExternalProgram;
import static java.lang.Math.round;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class SimPlagiarismFinder {
	private final StarterDto starterDto;
	private final JplagResult jplagResult;

	public SimPlagiarismFinder(StarterDto starterDto, JplagResult jplagResult) {
		this.starterDto = starterDto;
		this.jplagResult = jplagResult;
	}

	public void findPlagiarism() {
		List<String> submissions = ExternalResourceUtil.getDirectories("./submissions");
		ParsedFileHandler parsedFileHandler = findSimilaritiesBetweenSubmissions(submissions);
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
							double oppositeComparisonResultpercentage = comparisonResult.getOppositeComparisonResult().getPercentage();
							return comparisonResultPercentage < oppositeComparisonResultpercentage
											|| StringNumComparator.INSTANCE.compare(comparisonResult.getPublisher(), comparisonResult.getOppositeComparisonResult().getPublisher()) >= 0;
						})
						.collect(toList()));
	}

	private void markCommonCode(ParsedFileHandler parsedFileHandler) {
		System.out.println("Finding similarities over " + starterDto.getMaximumMatchOccurrenceBeforeIgnored() + " occurrences.");
		Map<String, Set<ComparisonResult>> commonCodeBlocksFound = new HashMap<>();
		parsedFileHandler.getComparisonResults()
						.forEach(comparisonResult -> comparisonResult.getSplitCommonCodeBlocksWithTokenSize()
										.forEach(splitCommonCodeBlocksWithTokenSize -> commonCodeBlocksFound.compute(splitCommonCodeBlocksWithTokenSize, (block, comparisonResults) -> {
											comparisonResults = ofNullable(comparisonResults)
															.orElseGet(HashSet::new);
											comparisonResults.add(comparisonResult);
											return comparisonResults;
										})));
		commonCodeBlocksFound.entrySet().stream()
						.filter(commonCodeBlockFound -> commonCodeBlockFound.getValue().size() - commonCodeBlockFound.getValue().stream()
										.filter(comparisonResult -> commonCodeBlockFound.getValue().contains(comparisonResult.getOppositeComparisonResult()))
										.count() / 2 > starterDto.getMaximumMatchOccurrenceBeforeIgnored())
						.forEach(commonCodeBlockFound -> commonCodeBlockFound.getValue().forEach(comparisonResult -> comparisonResult.markCommonCode(commonCodeBlockFound.getKey())));
	}

	private void getRidOfNotSignificantMatches(ParsedFileHandler parsedFileHandler) {
		System.out.println("Selecting significant matches");
		parsedFileHandler.getComparisonResults().removeIf(comparisonResult -> !comparisonResult.significantMatch());
	}

	private void writeOutput(ParsedFileHandler parsedFileHandler) {
		System.out.println("Extending JPlag's output.");
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
		Map<String, List<ComparisonResult>> comparisonResultsByPublisher = parsedFileHandler.getComparisonResults().stream()
						.collect(Collectors.groupingBy(ComparisonResult::getPublisher));
		Set<String> alreadyReportedPublishers = new HashSet<>();
		return "<HR><H4>Matches sorted by " + similarityType + " found by SIM:</H4>\n"
						+ "<TABLE CELLPADDING=3 CELLSPACING=2>"
						+ parsedFileHandler.getComparisonResults().stream()
						.filter(comparisonResult -> isNotEmpty(comparisonResult.getCommonCodeLines()))
						.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
						.filter(comparisonResult -> alreadyReportedPublishers.add(comparisonResult.getPublisher()))
						.map(comparisonResult -> "<TR>"
										+ "<TD BGCOLOR=#9ACD32>" + comparisonResult.getPublisher() + "</TD><TD><nobr>-&gt;</nobr></TD>"
										+ comparisonResultsByPublisher.get(comparisonResult.getPublisher()).stream()
										.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
										.map(comparisonResultForTheSamePublisher -> "<TD BGCOLOR=#9ACD32 ALIGN=center><A HREF=\"sim_match" + matchFileMaker.createMatchFile(comparisonResultForTheSamePublisher) + ".html\">"
														+ comparisonResultForTheSamePublisher.getOtherPublisher()
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
		return round(comparisonResult.getPercentage() * 10) / 10.0;
	}

	private ParsedFileHandler findSimilaritiesBetweenSubmissions(List<String> submissions) {
		ParsedFileHandler parsedFileHandler = new ParsedFileHandler();
		for (int i = 0; i < submissions.size() - 1; ++i) {
			for (int j = i + 1; j < submissions.size(); ++j) {
				ComparisonResult comparisonResult = calculateSimilarity(submissions, parsedFileHandler, i, j);
				ComparisonResult oppositeComparisonResult = calculateSimilarity(submissions, parsedFileHandler, j, i);
				if (comparisonResult != null && oppositeComparisonResult != null) {
					ComparisonResult.setOppositeComparisonResults(comparisonResult, oppositeComparisonResult);
				}
			}
		}
		return parsedFileHandler;
	}

	private ComparisonResult calculateSimilarity(List<String> submissions, ParsedFileHandler parsedFileHandler, int i, int j) {
		Submission submission = new Submission(submissions.get(i));
		Submission otherSubmission = new Submission(submissions.get(j));
		if (starterDto.isAvoidDuplicatesBetweenJplagAndSim() && jplagResult.contains(submission, otherSubmission)) {
			return null;
		}
		ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
						+ starterDto.getLanguage().simProgram
						+ " -r "
						+ starterDto.getPrecision().getSimMinimumRunLength()
						+ " -S -w " + starterDto.getPageWith() + " -R \"./submissions/"
						+ submission
						+ "/*\" \"|\" \"./submissions/"
						+ otherSubmission
						+ "/*\"", false);
		ComparisonResult comparisonResult = new ComparisonResult(starterDto.getPrecision());
		parsedFileHandler.addComparisonResult(comparisonResult);
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

	private ParsedFile createParsedFileIfNew(ParsedFileHandler parsedFileHandler, String out) {
		int fileNameEnd = out.indexOf(": ");
		return parsedFileHandler.createParsedFileIfNew(out.substring(5, fileNameEnd), Integer.parseInt(out.substring(fileNameEnd + 2, fileNameEnd + out.substring(fileNameEnd).lastIndexOf(" token"))));
	}
}
