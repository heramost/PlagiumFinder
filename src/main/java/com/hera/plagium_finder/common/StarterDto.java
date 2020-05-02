package com.hera.plagium_finder.common;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class StarterDto {
	private Language language;
	private int pageWith;
	private boolean avoidDuplicatesBetweenJplagAndSim;
	private int maximumMatchOccurrenceBeforeIgnored;
	private Precision precision;

	public static void validate(StarterDto starterDto) {
		if (starterDto.language == null) {
			throw new IllegalArgumentException("Language must be set at Config");
		}

		if (starterDto.pageWith < 80 || starterDto.pageWith > 500) {
			throw new IllegalArgumentException("Page with must be between 80 and 500");
		}

		if (starterDto.maximumMatchOccurrenceBeforeIgnored < 2) {
			throw new IllegalArgumentException("Maximum match occurrence before it is ignored must be at least 2");
		}

		if (starterDto.precision == null) {
			throw new IllegalArgumentException("Accepted precision values are: " + Arrays.stream(Precision.values()).map(Enum::name).collect(joining(", ")));
		}
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public int getPageWith() {
		return pageWith;
	}

	public void setPageWith(int pageWith) {
		this.pageWith = pageWith;
	}

	public boolean isAvoidDuplicatesBetweenJplagAndSim() {
		return avoidDuplicatesBetweenJplagAndSim;
	}

	public void setAvoidDuplicatesBetweenJplagAndSim(boolean avoidDuplicatesBetweenJplagAndSim) {
		this.avoidDuplicatesBetweenJplagAndSim = avoidDuplicatesBetweenJplagAndSim;
	}

	public int getMaximumMatchOccurrenceBeforeIgnored() {
		return maximumMatchOccurrenceBeforeIgnored;
	}

	public void setMaximumMatchOccurrenceBeforeIgnored(int maximumMatchOccurrenceBeforeIgnored) {
		this.maximumMatchOccurrenceBeforeIgnored = maximumMatchOccurrenceBeforeIgnored;
	}

	public Precision getPrecision() {
		return precision;
	}

	public void setPrecision(Precision precision) {
		this.precision = precision;
	}
}
