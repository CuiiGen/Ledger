package panels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
			sql = String.format(
					"SELECT * FROM ledger WHERE name LIKE '%s' and createtime >= '%s 00:00:00' and createtime <= '%s 23:59:59' and %s and %s ORDER BY createtime DESC;",
					name, startTime, stopTime, sortLabel, sortType);
		}
		return sql;
	}
}
