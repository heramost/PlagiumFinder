package com.hera.plagium_finder.common;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class StarterDto {
	private Language language;
	private int pageWidth;
	private int maximumMatchOccurrence;
	private IntegrationMode integrationMode;
	private Precision precision;
	private String nameOfPublicSubmission;

	public static void validate(StarterDto starterDto) {
		if (starterDto.language == null) {
			throw new IllegalArgumentException("Language (language) must be set at Config");
		}

		if (starterDto.pageWidth < 80 || starterDto.pageWidth > 500) {
			throw new IllegalArgumentException("Page width (pageWidth) must be between 80 and 500");
		}

		if (starterDto.maximumMatchOccurrence < 2) {
			throw new IllegalArgumentException("Maximum match occurrence before it is ignored (maximumMatchOccurrenceBeforeIgnored) must be at least 2");
		}

		if (starterDto.precision == null) {
			throw new IllegalArgumentException("Accepted precision (precision) values are: " + Arrays.stream(Precision.values()).map(Enum::name).collect(joining(", ")));
		}

		if (starterDto.integrationMode == null) {
			throw new IllegalArgumentException("Accepted integration mode (integrationMode) values are: " + Arrays.stream(IntegrationMode.values()).map(Enum::name).collect(joining(", ")));
		}

		if (starterDto.nameOfPublicSubmission == null || !starterDto.nameOfPublicSubmission.matches("^[a-zA-Z_0-9]*$")) {
			throw new IllegalArgumentException("Name of public submission (nameOfPublicSubmission) must match regex: ^[a-zA-Z_0-9]*$");
		}
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}

	public IntegrationMode getIntegrationMode() {
		return integrationMode;
	}

	public void setIntegrationMode(IntegrationMode integrationMode) {
		this.integrationMode = integrationMode;
	}

	public int getMaximumMatchOccurrence() {
		return maximumMatchOccurrence;
	}

	public void setMaximumMatchOccurrence(int maximumMatchOccurrence) {
		this.maximumMatchOccurrence = maximumMatchOccurrence;
	}

	public Precision getPrecision() {
		return precision;
	}

	public void setPrecision(Precision precision) {
		this.precision = precision;
	}

	public String getNameOfPublicSubmission() {
		return nameOfPublicSubmission;
	}

	public void setNameOfPublicSubmission(String nameOfPublicSubmission) {
		this.nameOfPublicSubmission = nameOfPublicSubmission;
	}
}
