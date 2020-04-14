package com.hera.plagium_finder.sim;

import com.hera.plagium_finder.common.Submission;

public class ParsedFile {
	private final String name;
	private int nrOfTokens;

	public ParsedFile(String name, int nrOfTokens) {
		this.name = name;
		this.nrOfTokens = nrOfTokens;
	}

	public String getName() {
		return name;
	}

	public int getNrOfTokens() {
		return nrOfTokens;
	}

	public Submission getSubmission() {
		return new Submission(getName().split("/")[2]);
	}

	@Override
	public String toString() {
		return "ParsedFile{" +
						"name='" + name + '\'' +
						", nrOfTokens=" + nrOfTokens +
						'}';
	}

	public String getFileExtension() {
		int lastDot = name.lastIndexOf(".");
		if (lastDot == -1) {
			return "";
		}

		return name.substring(lastDot);
	}
}
