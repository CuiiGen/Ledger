package models;

public class LabelStructure {
	private String label = null, createtime = null;
	// 相关金额总数
	private float amount = 0;
	// 相关流水个数
	private int count = 0;

	public LabelStructure(String label, String createtime, float amount, int count) {
		this.label = label;
		this.createtime = createtime;
		this.amount = amount;
		this.count = count;
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

	public int getCount() {
		return count;
	}

	/**
	 * 该函数用于更新标签名
	 * 
	 * @param aValue 新的标签名
	 */
	public void setLabel(String aValue) {
		label = aValue;
	}
}
