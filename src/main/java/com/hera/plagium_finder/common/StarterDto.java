package com.hera.plagium_finder.common;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class StarterDto {
	private Language language;
	private int pageWidth;
	private int maximumMatchOccurrenceBeforeIgnored;
	private IntegrationMode integrationMode;
	private Precision precision;

	public static void validate(StarterDto starterDto) {
		if (starterDto.language == null) {
			throw new IllegalArgumentException("Language (language) must be set at Config");
		}

		if (starterDto.pageWidth < 80 || starterDto.pageWidth > 500) {
			throw new IllegalArgumentException("Page width (pageWidth) must be between 80 and 500");
		}

		if (starterDto.maximumMatchOccurrenceBeforeIgnored < 2) {
			throw new IllegalArgumentException("Maximum match occurrence before it is ignored (maximumMatchOccurrenceBeforeIgnored) must be at least 2");
		}

		if (starterDto.precision == null) {
			throw new IllegalArgumentException("Accepted precision (precision) values are: " + Arrays.stream(Precision.values()).map(Enum::name).collect(joining(", ")));
		}

		if (starterDto.integrationMode == null) {
			throw new IllegalArgumentException("Accepted integration mode (integrationMode) values are: " + Arrays.stream(IntegrationMode.values()).map(Enum::name).collect(joining(", ")));
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
