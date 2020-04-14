import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class SameLoops {
	public static void loop() {

		for (int i = 0; i < 10; ++i) {
			System.out.println(i);
		}

		IntStream.range(0, 10).forEach(System.out::println);

		int i = 0;
		while (i < 10) {
			System.out.println(i);
			++i;
		}

		int j = 0;
		while (true) {
			if (j >= 10)
				break;
			System.out.println(j);
			++j;
		}

		int k = 0;
		if (k < 10) {
			do {
				System.out.println(k);
				++k;
			} while (k < 10);
		}
	}
	
	private void doNothing(int importantNumber) {
		importantNumber += 0;
	}
}
