package com.hera.plagium_finder.common;

import java.util.Objects;

public class Submission {
	private final String publisher;

	public Submission(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisher() {
		return publisher;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Submission))
			return false;
		Submission that = (Submission)o;
		return Objects.equals(publisher, that.publisher);
	}

	@Override
	public int hashCode() {
		return Objects.hash(publisher);
	}

	@Override
	public String toString() {
		return publisher;
	}
}
