package models;

public class ReimbursementStructure {

	// 序号
	private int number = 0;
	// 报销单名称
	private String name = null;
	// 结余
	private float balance = 0;
	// 是否已经完成
	private boolean complete = false;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public ReimbursementStructure() {
	}

}
