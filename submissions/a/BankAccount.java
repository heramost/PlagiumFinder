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

	private void checkBalance() {
		if (balance < 0) {
			throw new IllegalStateException("Deposit account balance is lower than zero");
		}
	}

	public int getBalance() {
		return balance;
	}
}
