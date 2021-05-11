package models;

public class AccountStructure {
	private String name = null, createtime = null;
	private float amount = 0;

	public AccountStructure(String name, String createtime, float amount) {
		this.name = name;
		this.createtime = createtime;
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public String getCreatetime() {
		return createtime;
	}

	public float getAmount() {
		return amount;
	}

	public void setName(String aValue) {
		name = aValue;
	}
}
