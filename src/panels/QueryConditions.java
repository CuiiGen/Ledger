package panels;

import java.util.Calendar;

public class QueryConditions {

	// 起始结束时间
	private static String startTime = null;
	private static String stopTime = null;
	// 标签
	private static String label = "全部";
	// 类别
	private static int type = 0;
	// 账户名
	private static String name = "%";

	public static void init() {
		Calendar c = Calendar.getInstance();
		stopTime = String.format("%tF", c);
		c.add(Calendar.MONTH, -1);
		startTime = String.format("%tF", c);
	}

	public static String getStartTime() {
		return startTime;
	}

	public static void setStartTime(String startTime) {
		QueryConditions.startTime = startTime;
	}

	public static String getStopTime() {
		return stopTime;
	}

	public static void setStopTime(String stopTime) {
		QueryConditions.stopTime = stopTime;
	}

	public static String getLabel() {
		return label;
	}

	public static void setLabel(String label) {
		QueryConditions.label = label;
	}

	public static int getType() {
		return type;
	}

	public static void setType(int type) {
		QueryConditions.type = type;
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		QueryConditions.name = name;
	}

	/**
	 * 根据筛选条件返回SQL语句
	 * 
	 * @return
	 */
	public static String getSQL() {
		String sortLabel = null, sortType = null;
		if (label == null) {
			sortLabel = "label IS NULL";
		} else if (label.equals("全部")) {
			sortLabel = "1 = 1";
		} else {
			sortLabel = String.format("label LIKE '%s'", label);
		}
		if (type == 0) {
			sortType = "1 = 1";
		} else {
			sortType = String.format("type = '%d'", 2 * type - 3);
		}
		String sql = String.format(
				"SELECT * FROM ledger WHERE name LIKE '%s' and createtime > '%s' and createtime < '%s' and %s and %s ORDER BY createtime DESC ",
				name, startTime, stopTime, sortLabel, sortType);
		return sql;
	}
}
