@SuppressWarnings("ALL")
public class AccountDeadCode {
	private final static int WELCOME_BONUS = 100;
	private int amount;

	public AccountDeadCode(int startingAmount) {
		amount = startingAmount;
		if (startingAmount >= 10000) {
			 add(WELCOME_BONUS);
		}
		doNothing(1);
	}

	public void add(int amount) {
		this.amount += amount;
		doNothing(2);
		checkAmount();
	}

	private void doNothing(int importantNumber) {
		importantNumber += 0;
	}

	private void checkAmount() {
		if (amount < 0) {
			throw new IllegalStateException("Deposit account amount is lower than 0");
		}
	}

	public int getAmount() {
		return amount;
	}
}
