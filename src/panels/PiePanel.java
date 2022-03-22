package panels;

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
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import database.H2_DB;
import design.DefaultFont;

public class PiePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7341489230357523930L;

	// 绘图区域
	private PiePlot<String> plot = null;
	private ArrayList<String> list = new ArrayList<>();
	// 日志
	private Logger logger = LogManager.getLogger();

	public PiePanel() throws SQLException {
		logger.info("支出情况饼图初始化 - 开始");
		// 创建饼图数据集
		DefaultPieDataset<String> dataset = createDataset();
		// 创建图形
		ChartPanel chartPanel = new ChartPanel(createChart(dataset));
		// 面板设置
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		logger.info("支出情况饼图初始化 - 开始");
	}

	private DefaultPieDataset<String> createDataset() throws SQLException {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		String sql = QueryConditions.getPieSql();
		H2_DB h2 = new H2_DB();
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		list.clear();
		while (rs.next()) {
			// 防止空键值出现
			String key = rs.getString(1);
			key = key == null ? "null" : key;
			dataset.setValue(key, rs.getDouble(2));
			list.add(key);
		}
		h2.close();
		return dataset;
	}

	@SuppressWarnings("unchecked")
	private JFreeChart createChart(DefaultPieDataset<String> dataset) {
		// 设置字体
		DefaultFont font = new DefaultFont();
		StandardChartTheme chartTheme = new StandardChartTheme("CN");
		chartTheme.setRegularFont(font.getFont(13f));
		chartTheme.setLargeFont(font.getFont(13f));
		chartTheme.setSmallFont(font.getFont(13f));
		// 应用
		ChartFactory.setChartTheme(chartTheme);
		// 创建chart
		JFreeChart freeChart = ChartFactory.createPieChart("", dataset, false, true, true);
		ChartUtils.applyCurrentTheme(freeChart);
		// 设置背景为白色
		freeChart.setBackgroundPaint(Color.WHITE);

		// 绘图主要区域
		plot = (PiePlot<String>) freeChart.getPlot();
		// 绘图区域设置
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setForegroundAlpha(0.5f);
		plot.setShadowPaint(null);
		// 标签
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}￥{1}({2})"));
		plot.setLabelFont(font.getFont(1, 16));
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
		DefaultPieDataset<String> dataset = createDataset();
		plot.setDataset(dataset);
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
