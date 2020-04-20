package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.jplag.JplagResult;
import com.hera.plagium_finder.util.ExternalProgramOutput;
import com.hera.plagium_finder.util.ExternalResourceUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static com.hera.plagium_finder.util.ExternalResourceUtil.callExternalProgram;
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
		ParsedFileHandler parsedFileHandler = initializeParsedFileHandler(submissions);
		compareSimilarFiles(parsedFileHandler);
		writeOutput(parsedFileHandler);
	}

	private void writeOutput(ParsedFileHandler parsedFileHandler) {
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
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculateMaximumSimilarity, "maximum similarity", true));
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculateAverageSimilarity, "average similarty", true));
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./result/index.html"))) {
			writer.write(lines.toString());
		}
		catch (IOException e) {
			System.out.println("Changing jplag results failed: " + e.getMessage());
		}
	}

	private String reportMatchesFoundBySim(ParsedFileHandler parsedFileHandler, MatchFileMaker matchFileMaker, ToDoubleFunction<Map.Entry<Submission, List<ComparisonResult>>> similarityCalculator, final String similarityType, boolean percentageNeeded) {
		return "<HR><H4>Matches sorted by " + similarityType + " found by SIM:</H4>\n"
						+ "<TABLE CELLPADDING=3 CELLSPACING=2>"
						+ parsedFileHandler.getComparisonResults().stream()
						.filter(comparisonResult -> isNotEmpty(comparisonResult.getCommonCodeLines()))
						.collect(groupingBy(comparisonResult -> comparisonResult.getParsedFile().getSubmission())).entrySet().stream()
						.sorted(Comparator.comparingDouble(comparisonResults -> ((Map.Entry<Submission, List<ComparisonResult>>)comparisonResults).getValue().stream()
										.collect(groupingBy(comparisonResult -> comparisonResult.getOtherParsedFile().getSubmission())).entrySet().stream()
										.mapToDouble(similarityCalculator)
										.max().getAsDouble()).reversed())
						.map(result -> "<TR>"
										+ "<TD BGCOLOR=#9ACD32>" + result.getKey().getPublisher() + "</TD><TD><nobr>-&gt;</nobr></TD>"
										+ result.getValue().stream()
										.collect(groupingBy(comparisonResult -> comparisonResult.getOtherParsedFile().getSubmission())).entrySet().stream()
										.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
										.map(comparisonResults -> "<TD BGCOLOR=#9ACD32 ALIGN=center><A HREF=\"sim_match" + matchFileMaker.createMatchFile(comparisonResults.getValue()) + ".html\">"
														+ comparisonResults.getKey()
														+ "</A><BR><FONT COLOR=\"#ff0000\">(" + similarityCalculator.applyAsDouble(comparisonResults) + (percentageNeeded ? "%" : "") + ")</FONT></TD>")
										.collect(joining(""))
										+ "</TR>")
						.collect(Collectors.joining("\r\n"))
						+ "</TABLE><P>\n";
	}

	private static double calculateMaximumSimilarity(Map.Entry<Submission, List<ComparisonResult>> comparisonResults) {
		return comparisonResults.getValue().stream()
						.map(ComparisonResult::getPercentage)
						.mapToDouble(percentage -> Math.round(percentage * 10) / 10.0)
						.max().getAsDouble();
	}

	private static double calculateNrOfMatchingTokens(Map.Entry<Submission, List<ComparisonResult>> comparisonResults) {
		return comparisonResults.getValue().stream()
						.filter(ComparisonResult::significantMatch)
						.mapToDouble(ComparisonResult::getNrOfMatchingTokens)
						.sum();
	}

	private static double calculateAverageSimilarity(Map.Entry<Submission, List<ComparisonResult>> comparisonResults) {
		Double nrOfMatchingTokens = comparisonResults.getValue().stream()
						.filter(ComparisonResult::significantMatch)
						.map(ComparisonResult::getNrOfMatchingTokens)
						.reduce(Double::sum).get();
		Integer nrOfTokens = comparisonResults.getValue().stream()
						.map(ComparisonResult::getParsedFile)
						.mapToInt(ParsedFile::getNrOfTokens)
						.sum();
		return Math.round(nrOfMatchingTokens / nrOfTokens * 1000) / 10.0;
}

	private void compareSimilarFiles(ParsedFileHandler parsedFileHandler) {
		parsedFileHandler.getComparisonResults().stream()
						.map(ComparisonResult::getMeasure)
						.distinct()
						.filter(Measure::significantSimilarity)
						.flatMap(measure -> measure.getComparisonResults().stream())
						.filter(ComparisonResult::significantMatch)
						.filter(comparisonResult -> !starterDto.isFilterFileExtensions() ||
										(hasAppropriateFileExtension(comparisonResult.getParsedFile()) && hasAppropriateFileExtension(comparisonResult.getOtherParsedFile())))
						.forEach(comparisonResult -> {
							ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
											+ starterDto.getLanguage().simProgram
											+ " -r 30 -S -w " + starterDto.getPageWith() + " \""
											+ comparisonResult.getParsedFile().getName()
											+ "\" \"|\" \""
											+ comparisonResult.getOtherParsedFile().getName()
											+ "\"", false);
							boolean codeReported = false;
							for (String out : externalProgramOutput.getStdOut()) {
								if (out.startsWith(comparisonResult.getParsedFile().getName() + ": line")) {
									codeReported = true;
								}
								if (codeReported) {
									comparisonResult.addCommonCodeLine(out);
								}
							}
						});
	}

	private boolean hasAppropriateFileExtension(ParsedFile parsedFile) {
		return starterDto.getLanguage().fileExtensions.contains(parsedFile.getFileExtension());
	}

	private ParsedFileHandler initializeParsedFileHandler(List<String> submissions) {
		ParsedFileHandler parsedFileHandler = new ParsedFileHandler();
		for (int i = 0; i < submissions.size() - 1; ++i) {
			for (int j = i + 1; j < submissions.size(); ++j) {
				calculateSimilarityPercentage(submissions, parsedFileHandler, i, j);
				calculateSimilarityPercentage(submissions, parsedFileHandler, j, i);
			}
		}
		return parsedFileHandler;
	}

	private void calculateSimilarityPercentage(List<String> submissions, ParsedFileHandler parsedFileHandler, int i, int j) {
		Submission submission = new Submission(submissions.get(i));
		Submission otherSubmission = new Submission(submissions.get(j));
		if (starterDto.isAvoidDuplicatesBetweenJplagAndSim() && jplagResult.contains(submission, otherSubmission)) {
			return;
		}
		ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
						+ starterDto.getLanguage().simProgram
						+ " -r 30 -p -u -S -R \"./submissions/"
						+ submission
						+ "/*\" \"|\" \"./submissions/"
						+ otherSubmission
						+ "/*\"", false);
		Measure measure = new Measure();
		for (String out : externalProgramOutput.getStdOut()) {
			if (out.startsWith("File ./")) {
				measure.addParsedFile(createParsedFileIfNew(parsedFileHandler, out));
			}
			else if (out.contains(" consists for ")) {
				addPercentageResult(parsedFileHandler, out, measure);
			}
		}

		if (isNotEmpty(externalProgramOutput.getStdErr())) {
			System.out.println("Comparing submissions " + submission + " and " + otherSubmission + " failed: " + String.join("\r\n", externalProgramOutput.getStdErr()));
		}
	}

	private void addPercentageResult(ParsedFileHandler parsedFileHandler, String out, Measure measure) {
		String[] firstAndSecondParsedFileWithPercentage = out.split(" consists for ");
		String parsedFile = firstAndSecondParsedFileWithPercentage[0];
		String[] percentageAndSecondParsedFile = firstAndSecondParsedFileWithPercentage[1].split(" % of ");
		double percentage = Double.parseDouble(percentageAndSecondParsedFile[0]);
		String otherParsedFile = percentageAndSecondParsedFile[1].substring(0, percentageAndSecondParsedFile[1].lastIndexOf(" material"));
		parsedFileHandler.addPercentage(parsedFile, otherParsedFile, percentage, measure);
	}

	private ParsedFile createParsedFileIfNew(ParsedFileHandler parsedFileHandler, String out) {
		int fileNameEnd = out.indexOf(": ");
		return parsedFileHandler.createParsedFileIfNew(out.substring(5, fileNameEnd), Integer.parseInt(out.substring(fileNameEnd + 2, fileNameEnd + out.substring(fileNameEnd).lastIndexOf(" token"))));
	}
}
