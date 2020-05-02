package com.hera.plagium_finder.sim;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class MatchFileMaker {
	private int matchesCreated = 0;

	public int createMatchFile(ComparisonResult comparisonResult) {
		List<String> parsedFileNames = comparisonResult.getParsedFileNames();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./result/sim_match" + matchesCreated + ".html"))) {
			writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"
							+ "<HTML><HEAD><TITLE>Matches for " + comparisonResult.getPublisher() + " & " + comparisonResult.getOtherPublisher() + "</TITLE>\n"
							+ "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
							+ "</HEAD>\n"
							+ "<PRE>\n"
							+ "<div style=\"position:absolute;left:0\">"
							+ comparisonResult.getCommonCodeLines().stream()
							.map(StringEscapeUtils::escapeHtml4)
							.map(line -> parsedFileNames.stream().anyMatch(parsedFileName -> line.startsWith(parsedFileName + ": line")) ? "<FONT color=\"#FF0000\">" + line + "</FONT>" : line)
							.collect(Collectors.joining("\r\n"))
							+ "</B></FONT></div></PRE>"
							+ "</HTML>\n");
			++matchesCreated;
		}
		catch (IOException e) {
			System.out.println("Creating matching results failed: " + e.getMessage());
		}
		return matchesCreated - 1;
	}
}
