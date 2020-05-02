package com.hera.plagium_finder.common;

public enum Precision {
	LIGHT(10, 30, 50, 100, 5),
	NORMAL(15, 40, 75, 400, 7),
	HIGH(20, 50, 100, 600, 10);

	private final int simMinimumRunLength;
	private final int minimumExpectedPercentage;
	private final int simMinimumNrOfTokens;
	private final int simNrOfMatchingTokensThatMustBeReportedIfFound;
	private final int jplagThreshold;

	Precision(int simMinimumRunLength, int minimumExpectedPercentage, int simMinimumNrOfTokens, int simNrOfMatchingTokensThatMustBeReportedIfFound, int jplagThreshold) {
		this.simMinimumRunLength = simMinimumRunLength;
		this.minimumExpectedPercentage = minimumExpectedPercentage;
		this.simMinimumNrOfTokens = simMinimumNrOfTokens;
		this.simNrOfMatchingTokensThatMustBeReportedIfFound = simNrOfMatchingTokensThatMustBeReportedIfFound;
		this.jplagThreshold = jplagThreshold;
	}

	public int getSimMinimumRunLength() {
		return simMinimumRunLength;
	}

	public int getMinimumExpectedPercentage() {
		return minimumExpectedPercentage;
	}

	public int getSimMinimumNrOfTokens() {
		return simMinimumNrOfTokens;
	}

	public int getSimNrOfMatchingTokensThatMustBeReportedIfFound() {
		return simNrOfMatchingTokensThatMustBeReportedIfFound;
	}

	public int getJplagThreshold() {
		return jplagThreshold;
	}
}
