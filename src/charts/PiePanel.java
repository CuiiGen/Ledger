package charts;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import panels.QueryConditions;

public class PiePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7341489230357523930L;

	// 绘图区域
	private MultiplePiePlot mplot = null;
	PiePlot<String> plot = null;
	// 用于记录标签名
	private ArrayList<String> list = new ArrayList<>();
	// 日志
	private Logger logger = LogManager.getLogger();

	/**
	 * 构造函数
	 * 
	 * @throws SQLException
	 */
	public PiePanel() throws SQLException {
		// 日志记录
		logger.info("支出情况饼图初始化 - 开始");
		// 创建饼图数据集
		DefaultCategoryDataset dataset = createcaCategoryDataset();
		// 创建图形
		ChartPanel chartPanel = new ChartPanel(createChart(dataset));
		// 面板设置
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		// 日志记录
		logger.info("支出情况饼图初始化 - 开始");
	}

	/**
	 * 获取收支-标签二维表格数据
	 * 
	 * @return 收支标签表数据
	 * @throws SQLException
	 */
	private DefaultCategoryDataset createcaCategoryDataset() throws SQLException {
		// dataset
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		// SQL
		String sql = QueryConditions.getInstance().getPieSql();
		H2_DB h2 = new H2_DB();
		logger.info(sql);
		// 重置
		list.clear();
		// 数据整合
		ResultSet rs = h2.query(sql);
		while (rs.next()) {
			// 防止空键值出现
			String key = rs.getString(1);
			if (key == null) {
				key = "null";
			}
			// 加入数据
			if (rs.getString("type").equals("1")) {
				dataset.addValue(rs.getDouble("total"), key, "  收 入 - 类 别 情 况  ");
			} else {
				dataset.addValue(rs.getDouble("total"), key, "  支 出 - 类 别 情 况  ");
			}
			list.add(key);
		}
		h2.close();
		return dataset;
	}

	/**
	 * 绘制图形返回chart
	 * 
	 * @param dataset
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JFreeChart createChart(DefaultCategoryDataset dataset) {
		// 设置字体
		DefaultFont font = new DefaultFont();
		StandardChartTheme chartTheme = new StandardChartTheme("CN");
		chartTheme.setRegularFont(font.getFont(13f));
		chartTheme.setLargeFont(font.getFont(13f));
		chartTheme.setSmallFont(font.getFont(13f));
		// 应用
		ChartFactory.setChartTheme(chartTheme);
		// 创建chart
		JFreeChart freeChart = ChartFactory.createMultiplePieChart("", dataset, TableOrder.BY_COLUMN, false, true,
				true);
		ChartUtils.applyCurrentTheme(freeChart);
		// 设置背景为白色
		freeChart.setBackgroundPaint(Color.WHITE);

		// 绘图主要区域
		mplot = (MultiplePiePlot) freeChart.getPlot();
		plot = (PiePlot<String>) mplot.getPieChart().getPlot();
		// 无数据提示信息
		mplot.setNoDataMessage("No Data is Available to Show\n无数据");
		mplot.setNoDataMessageFont(font.getFont(1, 30));
		mplot.setNoDataMessagePaint(Color.LIGHT_GRAY);
		// 标题设置
		plot.getChart().getTitle().setFont(font.getFont(18f));
		plot.getChart().getTitle().setBackgroundPaint(ThemeColor.LIGHT_GRAY);
		// 绘图区域设置
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setForegroundAlpha(0.5f);
		plot.setShadowPaint(null);
		// 标签
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}￥{1}({2})"));
		plot.setLabelFont(font.getFont(1));
		plot.setLabelOutlinePaint(null);
		plot.setLabelOutlineStroke(null);
		plot.setLabelShadowPaint(null);
		plot.setLabelBackgroundPaint(null);
		// 连接线
		plot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
		plot.setLabelLinkPaint(Color.DARK_GRAY);
		plot.setLabelLinkStroke(new BasicStroke(1.5f));
		plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator("Tooltip for legend item {0}"));
		// 分离设置
		setExplodePercent();

		return freeChart;
	}

	/**
	 * 更新数据重回折现
	 * 
	 * @throws SQLException
	 */
	public void updatePlot() throws SQLException {
		logger.info("折线重绘");
		DefaultCategoryDataset dataset = createcaCategoryDataset();
		mplot.setDataset(dataset);
		setExplodePercent();

	}

	/**
	 * 分离设置
	 */
	private void setExplodePercent() {
		for (int i = 0; i < list.size(); i++) {
			plot.setExplodePercent(list.get(i), 0.25 * Math.exp(-0.3 * i));
		}
	}
}
