package panels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tool.DefaultProperties;

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
	// 模糊搜索
	private static boolean isFuzzy = false;
	// 模糊搜索关键词
	private static String fuzzyWord = "%";
	// 是否只显示有效数据
	private static boolean isValid = false;

	/**
	 * 初始化筛选默认条件
	 */
	public static void init() {
		Logger logger = LogManager.getLogger();
		logger.info("查询条件初始化");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		startTime = String.format("%tF", c);
		c.add(Calendar.MONDAY, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		stopTime = String.format("%tF", c);
		label = "全部";
		type = 0;
		name = "%";
		// 设置默认选项
		isValid = DefaultProperties.getProperty("ledger.onlyValid", false);
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
	 * 上个月或下个月
	 * 
	 * @param delta
	 */
	public static void nextMonth(int delta) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			// 起始时间
			calendar.setTime(ft.parse(startTime));
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.MONTH, delta);
			startTime = String.format("%tF", calendar);
			// 结束时间
			calendar.setTime(ft.parse(stopTime));
			// 调整为1号
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			// 移动至后一月的一号
			calendar.add(Calendar.MONTH, 1 + delta);
			// 前一天
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			stopTime = String.format("%tF", calendar);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置是否进行模糊查询
	 * 
	 * @param is
	 */
	public static void setIsFuzzy(boolean is) {
		isFuzzy = is;
	}

	/**
	 * 设置进行模糊查询并设定关键词
	 * 
	 * @param word
	 */
	public static void setFuzzyWord(String word) {
		isFuzzy = true;
		fuzzyWord = String.format("%%%s%%", word);
	}

	public static void setIsValid(boolean is) {
		isValid = is;
	}

	public static boolean getIsValid() {
		return isValid;
	}

	/**
	 * 根据筛选条件返回SQL语句
	 * 
	 * @return
	 */
	public static String getSQL() {
		String sql = "";
		if (isFuzzy) {
			sql = String.format(
					"SELECT * FROM ledger WHERE label LIKE '%1$s' OR remark LIKE '%1$s' ORDER BY createtime DESC;",
					fuzzyWord);
		} else {
			String sortLabel = null, sortType = null;
			// 标签
			if (label.equals("  ")) {
				sortLabel = "`label` IS NULL";
			} else if (label.equals("全部")) {
				sortLabel = "true";
			} else {
				sortLabel = String.format("`label` LIKE '%s'", label);
			}
			// 类别
			if (type == 0) {
				sortType = "true";
			} else {
				sortType = String.format("`type` = '%d'", 2 * type - 3);
			}
			// SQL语句
			sql = String.format(
					"SELECT * FROM ledger WHERE isValid LIKE '%s' AND name LIKE '%s' and createtime >= '%s 00:00:00' and createtime <= '%s 23:59:59' and %s and %s ORDER BY createtime DESC;",
					isValid ? "o" : "%", name, startTime, stopTime, sortLabel, sortType);
		}
		return sql;
	}

	/**
	 * 返回每月消费流水
	 * 
	 * @return
	 */
	public static String getPlotSql() {
		String sql = "";
		String sortLabel = null;
		// 标签
		if (label.equals("  ")) {
			sortLabel = "label IS NULL";
		} else if (label.equals("全部")) {
			sortLabel = "true";
		} else {
			sortLabel = String.format("label LIKE '%s'", label);
		}
		// SQL语句
		sql = String.format("SELECT FORMATDATETIME(`CREATETIME`, 'yyyy-MM') AS x, SUM(`amount`) AS y FROM `ledger`"
				+ "WHERE `type` = '-1' AND `isvalid` = 'o' and %s GROUP BY x ORDER BY x;", sortLabel);
		return sql;
	}

	/**
	 * 绘制饼图所需要的的SQL语句
	 * 
	 * @return
	 */
	public static String getPieSql() {
		String sql = String.format("SELECT `label`, `type`, SUM(`amount`) AS `total` FROM `ledger` "
				+ "WHERE `isvalid` = 'o' AND createtime >= '%s 00:00:00' AND createtime <= '%s 23:59:59'"
				+ "GROUP BY `label`, `type` ORDER BY `type`, `total` DESC;", startTime, stopTime);
		return sql;
	}
}
