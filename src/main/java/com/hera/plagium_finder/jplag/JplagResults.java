package com.hera.plagium_finder.jplag;

import com.hera.plagium_finder.common.Submission;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class JplagResults {
	private boolean successfulRun = false;
	private final Map<Submission, List<JplagMatch>> plagiarizedSubmissions = new LinkedHashMap<>();

	public boolean isSuccessfulRun() {
		return successfulRun;
	}

	public void setSuccessfulRun(boolean successfulRun) {
		this.successfulRun = successfulRun;
	}

	public double getPercentageOfSimilarity(Submission submission, Submission otherSubmission) {
		return Math.max(getPercentage(submission, otherSubmission) , getPercentage(otherSubmission, submission));
	}

	private double getPercentage(Submission submission, Submission otherSubmission) {
		return plagiarizedSubmissions.getOrDefault(submission, emptyList()).stream()
						.filter(jplagMatch -> jplagMatch.otherSubmission.equals(otherSubmission))
						.findFirst()
						.map(jplagMatch -> jplagMatch.percentage)
						.orElse(0d);
	}

	public void addJPlagMatch(Submission submission, Submission otherSubmission, Double percentage) {
			plagiarizedSubmissions.compute(submission, (s, matches) -> {
				matches = ofNullable(matches)
								.orElseGet(LinkedList::new);
				matches.add(new JplagMatch(submission, otherSubmission, percentage));
				return matches;
			});
	}

	private class JplagMatch {
		private final Submission submission;
		private final Submission otherSubmission;
		private final double percentage;

		public JplagMatch(Submission submission, Submission otherSubmission, double percentage) {
			this.submission = submission;
			this.otherSubmission = otherSubmission;
			this.percentage = percentage;
		}
	}
}
