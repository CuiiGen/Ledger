package models;

public class LabelStructure {
	private String label = null, createtime = null;
	private float amount = 0;

	public LabelStructure(String label, String createtime, float amount) {
		this.label = label;
		this.createtime = createtime;
		this.amount = amount;
	}

	public String getLabel() {
		return label;
	}

	public String getCreatetime() {
		return createtime;
	}

	public float getAmount() {
		return amount;
	}

	public void setLabel(String aValue) {
		label = aValue;
	}
}
