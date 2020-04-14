@SuppressWarnings("ALL")
public class BankAccount {
	private final static int INITIAL_BONUS = 100;
	private int balance;

	public BankAccount(int initialAmount) {
		balance = initialAmount;
		if (initialAmount >= 10000) {
			change(INITIAL_BONUS);
		}
	}

	public void change(int amount) {
		this.balance += amount;
		checkBalance();
	}
	
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

	private void checkBalance() {
		if (balance < 0) {
			throw new IllegalStateException("Deposit account balance is lower than zero");
		}
	}

	public int getBalance() {
		return balance;
	}
}
