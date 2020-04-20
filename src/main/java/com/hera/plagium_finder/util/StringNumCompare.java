package com.hera.plagium_finder.util;

public class StringNumCompare {

	static public class UnsignedIntRegion implements Comparable {
		public String containing;
		///  num of '0's before the 'start' position
		public int leadingZeros;
		///	index of the first digit
		public int start;
		/// length of the region (without leading zeros)
		public int length;

		public UnsignedIntRegion(String containing, int start) {
			this.containing = containing;
			this.start = start;
		}

		@Override
		public int compareTo(Object to) {
			UnsignedIntRegion otherRegion = (UnsignedIntRegion)to;
			int comp =
							this.length - otherRegion.length;
			if (comp != 0) {// different size integers the shortest is the first
				return comp;
			}
			int i = 0;
			for (; i < this.length; i++) {
				if (this.containing.charAt(i + this.start) != otherRegion.containing.charAt(i + otherRegion.start)) {
					break;
				}
			}
			if (i == this.length) {// numerically equals
				return this.leadingZeros - otherRegion.leadingZeros;// the shortest is the first
			}
			// the differing character decides
			return this.containing.charAt(i + this.start) -
							otherRegion.containing.charAt(i + otherRegion.start); // the smaller is the first
		}
	}

	/// Used for special ordering of Strings
	/// 'unsigned integer' substrings compared numerically (in case of equality the num of the leading zeros decides)
	public static int compareStrings(String thisString, String otherString) {
		if (thisString == null) {
			thisString = "";
		}
		if (otherString == null) {
			otherString = "";
		}

		int thisSize = thisString.length();
		int otherSize = otherString.length();
		if (thisSize == 0 || otherSize == 0) {// at least one of them is empty
			return thisSize - otherSize;// the shortest is the first
		}

		int commonSize = thisSize < otherSize ? thisSize : otherSize;

		int i = 0;
		for (; i < commonSize; i++) {
			if (thisString.charAt(i) != otherString.charAt(i)) {
				break;
			}
		}

		if (i == commonSize) {// the shortest is the prefix of the other
			return thisSize - otherSize;// the shortest is the first
		}

		int firstDiffIndex = i;
		char thisFirstDiff = thisString.charAt(firstDiffIndex);
		char otherFirstDiff = otherString.charAt(firstDiffIndex);
		boolean thisIsDigit = thisFirstDiff >= '0' && thisFirstDiff <= '9';
		boolean otherIsDigit = otherFirstDiff >= '0' && otherFirstDiff <= '9';
		if (thisIsDigit && otherIsDigit) {// first different characters are digits in both Strings
			return compareIntRegions(thisString, otherString, firstDiffIndex);
		}
		else if (i > 0 && (thisIsDigit || otherIsDigit)) {// the different character is not the first one &&
			// one of the first different characters is digit, the other is not
			char previuosChar = thisString.charAt(firstDiffIndex - 1);
			if (previuosChar >= '0' && previuosChar <= '9') {
				return compareIntRegions(thisString, otherString, firstDiffIndex - 1);
			}
		}

		// string compare
		return thisString.compareTo(otherString);
	}

	private static int compareIntRegions(String thisString, String otherString,
					int firstDiffIndex) {
		UnsignedIntRegion thisRegion = new UnsignedIntRegion(thisString, firstDiffIndex);
		UnsignedIntRegion otherRegion = new UnsignedIntRegion(otherString, firstDiffIndex);

		findRegions(thisRegion, otherRegion);
		return thisRegion.compareTo(otherRegion);
	}

	private static void findRegions(UnsignedIntRegion thisRegion, UnsignedIntRegion otherRegion) {
		correctStartPosIfLeadingZerosPossible(thisRegion);
		correctStartPosIfLeadingZerosPossible(otherRegion);

		determineLength(thisRegion);
		determineLength(otherRegion);
	}

	private static void correctStartPosIfLeadingZerosPossible(UnsignedIntRegion thisRegion) {
		if (thisRegion.containing.charAt(thisRegion.start) == '0') {
			int firstNotZeroIndex = -1;
			for (firstNotZeroIndex = thisRegion.start - 1; firstNotZeroIndex >= 0; firstNotZeroIndex--) {
				if (thisRegion.containing.charAt(firstNotZeroIndex) != '0') {
					break;
				}
			}

			if (firstNotZeroIndex == -1 ||
							thisRegion.containing.charAt(firstNotZeroIndex) < '0' ||
							thisRegion.containing.charAt(firstNotZeroIndex) > '9') {// only leading '0'-s found before the difference place
				lookForRealStart(thisRegion);
			}
		}
	}

	private static void determineLength(UnsignedIntRegion thisRegion) {
		int actIndex;
		for (actIndex = thisRegion.start + 1; actIndex < thisRegion.containing.length(); actIndex++) {
			if (thisRegion.containing.charAt(actIndex) < '0' ||
							thisRegion.containing.charAt(actIndex) > '9') {
				break;
			}
		}
		thisRegion.length = actIndex - thisRegion.start;
	}

	private static void lookForRealStart(UnsignedIntRegion thisRegion) {
		int firstNonZero;
		for (firstNonZero = thisRegion.start; firstNonZero < thisRegion.containing.length(); firstNonZero++) {
			if (thisRegion.containing.charAt(firstNonZero) != '0') {
				break;
			}
			thisRegion.leadingZeros++;
		}
		if (firstNonZero == thisRegion.containing.length() ||
						thisRegion.containing.charAt(firstNonZero) < '0' ||
						thisRegion.containing.charAt(firstNonZero) > '9') {
			// thisRegion is only '0's
			thisRegion.start = firstNonZero - 1;
			thisRegion.leadingZeros--;
		}
		else {
			thisRegion.start = firstNonZero;
		}
	}
}
