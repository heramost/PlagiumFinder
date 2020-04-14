package com.hera.plagium_finder.util;

import java.util.LinkedList;
import java.util.List;

public class ExternalProgramOutput {
	private final List<String> stdOut = new LinkedList<>();
	private final List<String> stdErr = new LinkedList<>();

	public void addStdOutMessage(String stdOutMessage) {
		stdOut.add(stdOutMessage);
	}

	public void addStdErrMessage(String stdErrMessage) {
		stdOut.add(stdErrMessage);
	}

	public List<String> getStdErr() {
		return stdErr;
	}

	public List<String> getStdOut() {
		return stdOut;
	}

	@Override
	public String toString() {
		return "ExternalProgramOutput{" +
						"stdOut=" + stdOut +
						", stdErr=" + stdErr +
						'}';
	}
}
