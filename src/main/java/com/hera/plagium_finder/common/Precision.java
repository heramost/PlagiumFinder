package com.hera.plagium_finder.common;

public enum Precision {
	LIGHT(10, 30, 60, 100, 5),
	NORMAL(15, 40, 70, 400, 7),
	HIGH(20, 50, 80, 600, 10);

	private final int simMinimumRunLength;
	private final int expectedPercentage;
	private final int simMinimumNrOfTokens;
	private final int simNrOfMatchingTokensThatMustBeReportedIfFound;
	private final int jplagThreshold;

	Precision(int simMinimumRunLength, int expectedPercentage, int simMinimumNrOfTokens, int simNrOfMatchingTokensThatMustBeReportedIfFound, int jplagThreshold) {
		this.simMinimumRunLength = simMinimumRunLength;
		this.expectedPercentage = expectedPercentage;
		this.simMinimumNrOfTokens = simMinimumNrOfTokens;
		this.simNrOfMatchingTokensThatMustBeReportedIfFound = simNrOfMatchingTokensThatMustBeReportedIfFound;
		this.jplagThreshold = jplagThreshold;
	}

	public int getSimMinimumRunLength() {
		return simMinimumRunLength;
	}

	public int getExpectedPercentage() {
		return expectedPercentage;
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
