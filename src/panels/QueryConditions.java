package panels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tool.SystemProperties;

public class QueryConditions {

	// 起始结束时间
	private String startTime = "1970-01-01";
	private String stopTime = "1970-01-31";
	// 标签
	private String label = "全部";
	// 类别
	private String type = "全部";
	// 账户名
	private String name = "%";
	// 模糊搜索
	private boolean isFuzzy = false;
	// 模糊搜索关键词
	private String fuzzyWord = "%";
	// 是否只显示有效数据
	private boolean isValid = false;

	private int reimbursement = -1;

	public final static String nullPopItem = "  ";

	/**
	 * 初始化时间
	 */
	public void initTimeInternal() {
		Logger logger = LogManager.getLogger();
		logger.info("查询时间区间初始化");
		// TimeNow
		Calendar c = Calendar.getInstance();
		// StartTime
		c.set(Calendar.DAY_OF_MONTH, 1);
		startTime = String.format("%tF", c);
		// StopTime
		c.add(Calendar.MONDAY, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		stopTime = String.format("%tF", c);
	}

	/**
	 * 其他查询选项初始化
	 */
	public void initQueryItems() {
		Logger logger = LogManager.getLogger();
		logger.info("其他查询条件初始化");
		label = "全部";
		type = "全部";
		name = "%";
		reimbursement = -1;
		// 设置默认选项
		isValid = SystemProperties.getInstance().getBoolean("ledger.onlyValid");

	}

	/**
	 * 初始化筛选默认条件
	 */
	public void reset() {
		Logger logger = LogManager.getLogger();
		logger.info("查询条件初始化");
		initTimeInternal();
		initQueryItems();
	}

	private QueryConditions() {
		reset();
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	private String getLabelSQL() {
		if (label.equals("全部")) {
			return "true";
		} else if (label.equals(QueryConditions.nullPopItem)) {
			return "`label` IS null";
		} else {
			return String.format("`label` = '%s'", label);
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String getTypeSQL() {
		if (type.equals("全部")) {
			return "true";
		} else if (type.equals("支出")) {
			return "`type` = '-1'";
		} else {
			return "`type` = '1'";
		}
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

	private String getReimbursementSQL() {
		if (reimbursement < 0) {
			return "true";
		} else if (reimbursement == 0) {
			return "`reimbursement` IS null";
		} else {
			return String.format("`reimbursement` = '%d'", reimbursement);
		}
	}

	/**
	 * 上个月或下个月
	 * 
	 * @param delta
	 */
	public void nextMonth(int delta) {
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
	public void setIsFuzzy(boolean is) {
		isFuzzy = is;
	}

	/**
	 * 设置进行模糊查询并设定关键词
	 * 
	 * @param word
	 */
	public void setFuzzyWord(String word) {
		isFuzzy = true;
		fuzzyWord = String.format("%%%s%%", word);
	}

	public void setIsValid(boolean is) {
		isValid = is;
	}

	public boolean getIsValid() {
		return isValid;
	}

	private String getIsValidSQL() {
		if (isValid) {
			return "`isValid` = 'o'";
		} else {
			return "true";
		}
	}

	/**
	 * 根据筛选条件返回SQL语句
	 * 
	 * @return
	 */
	public String getSQL() {
		String sql = "";
		if (isFuzzy) {
			sql = String.format(
					"SELECT * FROM ledger WHERE label LIKE '%1$s' OR remark LIKE '%1$s' ORDER BY createtime DESC;",
					fuzzyWord);
		} else {
			// SQL语句
			sql = String.format(
					"SELECT * FROM ledger WHERE %s AND name LIKE '%s' "
							+ "AND createtime >= '%s 00:00:00' AND createtime <= '%s 23:59:59' "
							+ "AND %s and %s AND %s ORDER BY createtime DESC;",
					getIsValidSQL(), name, //
					startTime, stopTime, //
					getLabelSQL(), getTypeSQL(), getReimbursementSQL());
		}
		return sql;
	}

	/**
	 * 返回每月消费流水
	 * 
	 * @return
	 */
	public String getPlotSql() {
		// SQL语句
		String sql = String.format("SELECT FORMATDATETIME(`CREATETIME`, 'yyyy-MM') AS x, SUM(`amount`) AS y FROM `ledger`"
				+ "WHERE `type` = '-1' AND `isvalid` = 'o' and %s GROUP BY x ORDER BY x;", getLabelSQL());
		return sql;
	}

	/**
	 * 绘制饼图所需要的的SQL语句
	 * 
	 * @return
	 */
	public String getPieSql() {
		String sql = String.format("SELECT `label`, `type`, SUM(`amount`) AS `total` FROM `ledger` "
				+ "WHERE `isvalid` = 'o' AND createtime >= '%s 00:00:00' AND createtime <= '%s 23:59:59'"
				+ "GROUP BY `label`, `type` ORDER BY `type`, `total` DESC;", startTime, stopTime);
		return sql;
	}

	private static volatile QueryConditions instance;

	public static QueryConditions getInstance() {
		if (instance == null) {
			synchronized (QueryConditions.class) {
				if (instance == null) {
					instance = new QueryConditions();
				}
			}
		}
		return instance;
	}

}
