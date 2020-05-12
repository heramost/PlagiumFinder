package com.hera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.jplag.JplagPlagiarismFinder;
import com.hera.plagium_finder.jplag.JplagResults;
import com.hera.plagium_finder.sim.SimPlagiarismFinder;
import com.hera.plagium_finder.util.ExternalResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import static com.hera.plagium_finder.util.ExternalResourceUtil.hasDirectory;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Main {

	public static void main(String[] args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		if (hasDirectory("./", "result")) {
			System.out.println("Old results directory: results must be deleted before run");
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		StarterDto starterDto;
		try {
			starterDto = mapper.readValue(new FileInputStream(new File("./config.json")), StarterDto.class);
			StarterDto.validate(starterDto);
		}
		catch (Exception e) {
			System.out.println("Parsing config file failed: " + e.getMessage());
			return;
		}
		if (ExternalResourceUtil.getDirectories("./submissions", starterDto.getNameOfPublicSubmission()).size() < 2) {
			System.out.println("There must be at least two valid submissions to compare");
			return;
		}
		if (isNotEmpty(starterDto.getNameOfPublicSubmission()) && !hasDirectory("./submissions", starterDto.getNameOfPublicSubmission())) {
			System.out.println("Public submission not found at: ./submissions/" + starterDto.getNameOfPublicSubmission());
			return;
		}

		JplagPlagiarismFinder jplagPlagiarismFinder = new JplagPlagiarismFinder(starterDto);
		JplagResults jplagResults = jplagPlagiarismFinder.findPlagiarism();
		if (!jplagResults.isSuccessfulRun()) {
			System.out.println("JPlag failed unexpectedly.");
			return;
		}

		SimPlagiarismFinder simPlagiarismFinder = new SimPlagiarismFinder(starterDto, jplagResults);
		simPlagiarismFinder.findPlagiarism();
		stopWatch.stop();
		System.out.println("Execution finished in: " + Math.round(stopWatch.getTime(TimeUnit.MILLISECONDS) / 600.0) / 100.0 + " minutes");
	}
}
