package com.hera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hera.plagium_finder.common.StarterDto;
import com.hera.plagium_finder.jplag.JplagPlagiarismFinder;
import com.hera.plagium_finder.jplag.JplagResult;
import com.hera.plagium_finder.sim.SimPlagiarismFinder;

import java.io.File;
import java.io.FileInputStream;

public class Main {

	public static void main(String[] args) {
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

		JplagPlagiarismFinder jplagPlagiarismFinder = new JplagPlagiarismFinder(starterDto);
		JplagResult jplagResult = jplagPlagiarismFinder.findPlagiarism();
		if (!jplagResult.isSuccessfulRun()) {
			return;
		}

		SimPlagiarismFinder simPlagiarismFinder = new SimPlagiarismFinder(starterDto, jplagResult);
		simPlagiarismFinder.findPlagiarism();
	}
}
