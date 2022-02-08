package models;

import java.util.Calendar;

public class RecordStructure implements Cloneable {

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

	public void resetCreatetime() {
		createtime = String.format("%1$tF %1$tT", Calendar.getInstance());
	}

	public String getCreatetime() {
		return createtime;
	}

	public String getName() {
		return name;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRemark() {
		return remark;
	}

	public void reverseType() {
		type *= -1;
	}

	public int getType() {
		return type;
	}

	public float getAmount() {
		return amount;
	}

	public String toString() {
		return String.format("%s,%s,%s,%s,%.2f,%s,%s", isValid ? "o" : "", createtime, name, type == -1 ? "支出" : "收入",
				amount, label, remark);
	}

	public boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public RecordStructure clone() throws CloneNotSupportedException {
		return (RecordStructure) super.clone();
	}

}
