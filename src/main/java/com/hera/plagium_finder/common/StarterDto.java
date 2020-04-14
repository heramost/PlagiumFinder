package com.hera.plagium_finder.common;

public class StarterDto {
	private Language language;
	private boolean filterFileExtensions;
	private int pageWith;
	private boolean avoidDuplicatesBetweenJplagAndSim;

	public static void validate(StarterDto starterDto) {
		if (starterDto.language == null) {
			throw new IllegalArgumentException("Language must be set at Config");
		}

		if (starterDto.pageWith < 80 || starterDto.pageWith > 500) {
			throw new IllegalArgumentException("Page with must be between 80 and 500");
		}
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public boolean isFilterFileExtensions() {
		return filterFileExtensions;
	}

	public void setFilterFileExtensions(boolean filterFileExtensions) {
		this.filterFileExtensions = filterFileExtensions;
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
}
