package com.hera.plagium_finder.jplag;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.util.ExternalProgramOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.hera.plagium_finder.util.ExternalResourceUtil.callExternalProgram;
import static java.lang.String.join;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class JplagPlagiarismFinder {
	private final StarterDto starterDto;

	public JplagPlagiarismFinder(StarterDto starterDto) {
		this.starterDto = starterDto;
	}

	public JplagResult findPlagiarism() {
		JplagResult jplagResult = new JplagResult();
		ExternalProgramOutput externalProgramOutput = callExternalProgram("java -jar ./jplag-2.12.1-SNAPSHOT-jar-with-dependencies.jar -vl -s -l "
						+ starterDto.getLanguage().jplagParameter
						+ " -p "
						+ join(",", starterDto.getLanguage().fileExtensions)
						+ " ./submissions");
		if (isNotEmpty(externalProgramOutput.getStdErr())) {
			System.out.println("Running jplag failed: " + join("\r\n", externalProgramOutput.getStdErr()));
			return jplagResult;
		}
		if (externalProgramOutput.getStdOut().isEmpty()) {
			System.out.println("Running jplag failed unexpectedly");
			return jplagResult;
		}
		if (!externalProgramOutput.getStdOut().get(externalProgramOutput.getStdOut().size() - 1).equals("Writing results to: result")) {
			System.out.println("Running jplag failed: " + join("\r\n", externalProgramOutput.getStdOut()));
			return jplagResult;
		}

		String line;
		for (String generatedExcelNames : Arrays.asList("matches_avg.csv", "matches_max.csv")) {
			try (BufferedReader br = new BufferedReader(new FileReader("./result/" + generatedExcelNames))) {
				while ((line = br.readLine()) != null) {

					String[] matchesForAFile = line.split(";");
					Submission submission = new Submission(matchesForAFile[0]);
					List<Submission> submissionsFoundToBeSimilar = new LinkedList<>();
					for (int i = 2; i < matchesForAFile.length; i += 3) {
						submissionsFoundToBeSimilar.add(new Submission(matchesForAFile[i]));
					}
					jplagResult.addPlagiarizedSubmissions(submission, submissionsFoundToBeSimilar);
				}
			}
			catch (IOException e) {
				return jplagResult;
			}
		}
		jplagResult.setSuccessfulRun(true);
		return jplagResult;
	}
}
