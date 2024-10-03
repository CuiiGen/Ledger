package models;

import java.util.Calendar;

public class RecordStructure implements Cloneable {

	private String createtime = null, name = null, label = null, remark = null;

	int type = 1;
	// 报销单ID，0表示空
	int reimbursement = 0;
	float amount = 0;
	// 是否计入流水
	private boolean isValid = true;

	public RecordStructure() {
	}

	public void setCreatetime() {
		createtime = String.format("%1$tF %1$tT", Calendar.getInstance());
	}

	public void setCreatetime(String newTime) {
		createtime = newTime;
	}

	public String getCreatetime() {
		return createtime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReimbursement() {
		return reimbursement;
	}

	public void setReimbursement(int reimbursement) {
		this.reimbursement = reimbursement;
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

	public void setType(int type) {
		this.type = type;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmcount(float amount) {
		this.amount = amount;
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
