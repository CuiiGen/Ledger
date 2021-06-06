package models;

public class RecordStructure {

	private String createtime = null, name = null, label = null, remark = null;

	int type = 1;
	float amount = 0;
	// 是否计入流水
	private boolean isValid = true;

	public RecordStructure(String createtime, String name, int type, float amount, String label, String remark,
			String isValid) {
		this.createtime = createtime;
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.label = label;
		this.remark = remark;
		this.isValid = isValid.equals("o");

	}

	public String getCreatetime() {
		return createtime;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getRemark() {
		return remark;
	}

	public int getType() {
		return type;
	}

	public float getAmount() {
		return amount;
	}

	public String toString() {
		return String.format("%s,%s,%s,%.2f,%s,%s", createtime, name, type == -1 ? "支出" : "收入", amount, label, remark);
	}

	public boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

}
