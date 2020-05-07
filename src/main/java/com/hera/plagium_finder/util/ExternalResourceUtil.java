package com.hera.plagium_finder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class ExternalResourceUtil {
	public static ExternalProgramOutput callExternalProgram(String program, boolean readinessNeedsToBeChecked, boolean logCall) {
		if (logCall) {
			System.out.println("Calling: " + program);
		}
		ExternalProgramOutput externalProgramOutput = new ExternalProgramOutput();
		try {
			Process process = Runtime.getRuntime().exec(program);
			InputStream stderr = process.getErrorStream();
			InputStream stdout = process.getInputStream();

			BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
			String line;
			while ((!readinessNeedsToBeChecked || brCleanUp.ready()) && (line = brCleanUp.readLine()) != null) {
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

	public static List<String> getDirectories(String path, String... directoriesToExclude) {
		try {
			File file = new File(path);
			String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
			List<String> directoriesToExc = asList(directoriesToExclude);
			return  Arrays.stream(directories)
							.filter(directory -> !directoriesToExc.contains(directory.split(path)[0]))
							.sorted(StringNumComparator.INSTANCE)
							.collect(toList());
		}
		catch (Exception e) {
			System.out.println("Getting directories of path: " + path + " failed");
		}

		return emptyList();
	}

	public static boolean hasDirectory(String path, String directory) {
		return getDirectories(path).contains(directory);
	}
}
