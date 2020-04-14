package com.hera.plagium_finder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class ExternalResourceUtil {
	public static ExternalProgramOutput callExternalProgram(String program) {
		System.out.println("Calling: " + program);
		ExternalProgramOutput externalProgramOutput = new ExternalProgramOutput();
		try {
			Process process = Runtime.getRuntime().exec(program);
			InputStream stderr = process.getErrorStream();
			InputStream stdout = process.getInputStream();

			BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
			String line;
			while ((line = brCleanUp.readLine()) != null) {
				externalProgramOutput.addStdOutMessage(line);
			}
			brCleanUp.close();

			brCleanUp = new BufferedReader(new InputStreamReader(stderr));
			while ((line = brCleanUp.readLine()) != null) {
				externalProgramOutput.addStdErrMessage(line);
			}
			brCleanUp.close();
		}
		catch (Exception e) {
			System.out.println("Calling external program: " + program + " failed: " + e.getMessage());
		}

		return externalProgramOutput;
	}

	public static List<String> getDirectories(String path) {
		try {
			File file = new File(path);
			String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
			return Arrays.asList(directories);
		}
		catch (Exception e) {
			System.out.println("Getting directories of path: " + path + " failed");
		}

		return emptyList();
	}
}
