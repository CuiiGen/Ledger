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

	/**
	 * 初始化筛选默认条件
	 */
	public static void init() {
		Calendar c = Calendar.getInstance();
		stopTime = String.format("%tF", c);
		// c.add(Calendar.MONTH, -1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		startTime = String.format("%tF", c);
		label = "全部";
		type = 0;
		name = "%";
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
		// 标签
		if (label.isEmpty()) {
			sortLabel = "label IS NULL";
		} else if (label.equals("全部")) {
			sortLabel = "1 = 1";
		} else {
			sortLabel = String.format("label LIKE '%s'", label);
		}
		// 类别
		if (type == 0) {
			sortType = "1 = 1";
		} else {
			sortType = String.format("type = '%d'", 2 * type - 3);
		}
		// SQL语句
		String sql = String.format(
				"SELECT * FROM ledger WHERE name LIKE '%s' and createtime >= '%s 00:00:00' and createtime <= '%s 23:59:59' and %s and %s ORDER BY createtime DESC ",
				name, startTime, stopTime, sortLabel, sortType);
		return sql;
	}
}
