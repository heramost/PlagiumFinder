package com.hera.plagium_finder.jplag;

import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.common.Submission;
import com.hera.plagium_finder.util.ExternalProgramOutput;
import com.hera.plagium_finder.util.ExternalResourceUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static com.hera.plagium_finder.util.ExternalResourceUtil.callExternalProgram;
import static java.lang.String.join;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;

public class JplagPlagiarismFinder {
	private final StarterDto starterDto;

	public JplagPlagiarismFinder(StarterDto starterDto) {
		this.starterDto = starterDto;
	}

	public JplagResults findPlagiarism() {
		JplagResults jplagResults = new JplagResults();
		ExternalProgramOutput externalProgramOutput = callExternalProgram("java -jar ./jplag-2.12.1-SNAPSHOT-jar-with-dependencies.jar -m "
						+ starterDto.getPrecision().getMinimumExpectedPercentage()
						+ "% -vl -s -l "
						+ starterDto.getLanguage().jplagParameter
						+ " -t " + starterDto.getPrecision().getJplagThreshold()
						+ (isNoneEmpty(starterDto.getNameOfPublicSubmission()) ? " -bc " + starterDto.getNameOfPublicSubmission() : "")
						+ " -p " + join(",", starterDto.getLanguage().fileExtensions)
						+ " ./submissions", false);
		if (isNotEmpty(externalProgramOutput.getStdErr())) {
			System.out.println("Running JPlag failed: " + join("\r\n", externalProgramOutput.getStdErr()));
			return jplagResults;
		}
		if (externalProgramOutput.getStdOut().isEmpty()) {
			System.out.println("Running jplag failed unexpectedly");
			return jplagResults;
		}
		if (finishedWithAnIssue(externalProgramOutput)) {
			if (ExternalResourceUtil.hasDirectory("./", "result")) {
				System.out.println("Running JPlag has some issues: " + join("\r\n", externalProgramOutput.getStdOut()));
			}
			else {
				System.out.println("Running JPlag failed: " + join("\r\n", externalProgramOutput.getStdOut()));
				return jplagResults;
			}
		}

		String line;
			try (BufferedReader br = new BufferedReader(new FileReader("./result/matches_avg.csv"))) {
				while ((line = br.readLine()) != null) {

					String[] matchesForAFile = line.split(";");
					Submission submission = new Submission(matchesForAFile[0]);
					for (int i = 2; i < matchesForAFile.length; i += 3) {
						jplagResults.addJPlagMatch(submission, new Submission(matchesForAFile[i]), Double.valueOf(matchesForAFile[i + 1]));
					}
				}
			}
			catch (IOException e) {
				return jplagResults;
			}
		jplagResults.setSuccessfulRun(true);
		return jplagResults;
	}

	private boolean finishedWithAnIssue(ExternalProgramOutput externalProgramOutput) {
		return externalProgramOutput.getStdOut().stream().noneMatch(message -> message.equals("0 parser errors!"))
						|| externalProgramOutput.getStdOut().stream().anyMatch(message -> message.endsWith("submissions are not valid because they contain fewer tokens"));
	}
}
