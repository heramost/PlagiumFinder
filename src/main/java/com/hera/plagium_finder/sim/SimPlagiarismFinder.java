package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.jplag.JplagResult;
import com.hera.plagium_finder.util.ExternalProgramOutput;
import com.hera.plagium_finder.util.ExternalResourceUtil;
import org.apache.commons.collections4.CollectionUtils;

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
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculateMaximumSimilarity, "maximum"));
		lines.insert(whereToPutNewTable, reportMatchesFoundBySim(parsedFileHandler, matchFileMaker, SimPlagiarismFinder::calculateAverageSimilarity, "average"));
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./result/index.html"))) {
			writer.write(lines.toString());
		}
		catch (IOException e) {
			System.out.println("Changing jplag results failed: " + e.getMessage());
		}
	}

	private String reportMatchesFoundBySim(ParsedFileHandler parsedFileHandler, MatchFileMaker matchFileMaker, ToDoubleFunction<Map.Entry<Submission, List<ComparisonResult>>> similarityCalculator, final String similarityType) {
		return "<HR><H4>Matches sorted by " + similarityType + " similarity found by SIM:</H4>\n"  //todo sorting
						+ "<TABLE CELLPADDING=3 CELLSPACING=2>"
						+ parsedFileHandler.getComparisonResults().stream()
						.collect(groupingBy(comparisonResult -> comparisonResult.getParsedFile().getSubmission())).entrySet().stream()
						.map(result -> "<TR>"
										+ "<TD BGCOLOR=#c0c0ff>" + result.getKey().getPublisher() + "</TD><TD><nobr>-&gt;</nobr></TD>"
										+ result.getValue().stream()
										.collect(groupingBy(comparisonResult -> comparisonResult.getOtherParsedFile().getSubmission())).entrySet().stream()
										.sorted(Comparator.comparingDouble(similarityCalculator).reversed())
										.map(comparisonResults -> "<TD BGCOLOR=#c0c0ff ALIGN=center><A HREF=\"sim_match"+ matchFileMaker.createMatchFile(comparisonResults.getValue()) +".html\">"
														+ comparisonResults.getKey()
														+ "</A><BR><FONT COLOR=\"#ff0000\">(" + similarityCalculator.applyAsDouble(comparisonResults) + "%)</FONT></TD>")
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

	private static double calculateAverageSimilarity(Map.Entry<Submission, List<ComparisonResult>> comparisonResults) {
		Double nrOfMatchingTokens = comparisonResults.getValue().stream()
						.map(ComparisonResult::getNrOfMatchingTokens)
						.reduce(Double::sum).get();
		Integer nrOfTokens = comparisonResults.getValue().stream()
						.map(ComparisonResult::getOtherParsedFile)
						.map(ParsedFile::getNrOfTokens)
						.reduce(Integer::sum).get();
		return Math.round(nrOfMatchingTokens / nrOfTokens * 1000) / 10.0;
	}

	private void compareSimilarFiles(ParsedFileHandler parsedFileHandler) {
		parsedFileHandler.getComparisonResults().stream()
						.filter(ComparisonResult::significantMatch)
						.filter(comparisonResult -> !starterDto.isFilterFileExtensions() ||
										(hasAppropriateFileExtension(comparisonResult.getParsedFile()) && hasAppropriateFileExtension(comparisonResult.getOtherParsedFile())))
						.forEach(comparisonResult -> {
							ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
											+ starterDto.getLanguage().simProgram
											+ " -S -w " + starterDto.getPageWith() + " \""
											+ comparisonResult.getParsedFile().getName()
											+ "\" \"|\" \""
											+ comparisonResult.getOtherParsedFile().getName()
											+ "\"");
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
				Submission submission = new Submission(submissions.get(i));
				Submission otherSubmission = new Submission(submissions.get(j));
				if (starterDto.isAvoidDuplicatesBetweenJplagAndSim() && jplagResult.contains(submission, otherSubmission)) {
					continue;
				}
				ExternalProgramOutput externalProgramOutput = callExternalProgram("./"
								+ starterDto.getLanguage().simProgram
								+ " -p -S -R \"./submissions/"
								+ submission
								+ "/*\" \"|\" \"./submissions/"
								+ otherSubmission
								+ "/*\"");
				for (String out : externalProgramOutput.getStdOut()) {
					if (out.startsWith("File ./")) {
						createParsedFileIfNew(parsedFileHandler, out);
					}
					else if (out.contains(" consists for ")) {
						addPercentageResult(parsedFileHandler, out);
					}
				}

				if (CollectionUtils.isNotEmpty(externalProgramOutput.getStdErr())) {
					System.out.println("Comparing submissions " + submission + " and " + otherSubmission + " failed: " + String.join("\r\n", externalProgramOutput.getStdErr()));
				}
			}
		}
		return parsedFileHandler;
	}

	private void addPercentageResult(ParsedFileHandler parsedFileHandler, String out) {
		String[] firstAndSecondParsedFileWithPercentage = out.split(" consists for ");
		String parsedFile = firstAndSecondParsedFileWithPercentage[0];
		String[] percentageAndSecondParsedFile = firstAndSecondParsedFileWithPercentage[1].split(" % of ");
		double percentage = Double.parseDouble(percentageAndSecondParsedFile[0]);
		String otherParsedFile = percentageAndSecondParsedFile[1].substring(0, percentageAndSecondParsedFile[1].lastIndexOf(" material"));
		parsedFileHandler.addPercentage(parsedFile, otherParsedFile, percentage);
	}

	private void createParsedFileIfNew(ParsedFileHandler parsedFileHandler, String out) {
		int fileNameEnd = out.indexOf(": ");
		parsedFileHandler.createParsedFileIfNew(out.substring(5, fileNameEnd), Integer.parseInt(out.substring(fileNameEnd + 2, fileNameEnd + out.substring(fileNameEnd).indexOf(" tokens, "))));
	}
}
