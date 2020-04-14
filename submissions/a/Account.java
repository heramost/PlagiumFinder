@SuppressWarnings("ALL")
public class Account {
	private final static int WELCOME_BONUS = 100;
	private int amount;

	public Account(int startingAmount) {
		amount = startingAmount;
		if (startingAmount >= 10000) {
			 add(WELCOME_BONUS);
		}
	}

	public void add(int amount) {
		this.amount += amount;
		checkAmount();
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
