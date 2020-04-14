package com.hera.plagium_finder.jplag;

import com.hera.plagium_finder.common.Submission;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class JplagResult {
	private boolean successfulRun = false;
	private final Map<Submission, List<Submission>> plagiarizedSubmissions = new LinkedHashMap<>();

	public void addPlagiarizedSubmissions(Submission submission, List<Submission> plagiarizedSubmissions) {
		this.plagiarizedSubmissions.put(submission, plagiarizedSubmissions);
	}

	public boolean isSuccessfulRun() {
		return successfulRun;
	}

	public void setSuccessfulRun(boolean successfulRun) {
		this.successfulRun = successfulRun;
	}

	public boolean contains(Submission submission, Submission otherSubmission) {
		return hasPlagiarized(submission, otherSubmission) || hasPlagiarized(otherSubmission, submission);
	}

	private boolean hasPlagiarized(Submission submission, Submission otherSubmission) {
		List<Submission> submissions = plagiarizedSubmissions.getOrDefault(submission, emptyList());
		return submissions.contains(otherSubmission);
	}
}
