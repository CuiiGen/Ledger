package panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import database.H2_DB;
import design.DefaultFont;

public class PlotPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8734246980656734214L;

	private JFreeChart chart = null;
	private XYPlot plot = null;
	// 消费类型
	private int type = -1;
	// 日志
	private Logger logger = LogManager.getLogger();

	public PlotPanel(int aType) throws SQLException {
		logger.info("每日流水图初始化 - 完成");
		type = aType;
		// 获取数据
		XYDataset dataset = createDataset();
		// 创建图形
		chart = createChart(dataset);
		// 面板设置
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 20));
		// 布局设置
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		logger.info("每日流水图初始化 - 完成，type = " + aType);
	}

	/**
	 * 创建dataset
	 * 
	 * @return
	 * @throws SQLException
	 */
	private XYDataset createDataset() throws SQLException {
		// 获取数据
		H2_DB h2 = new H2_DB();
		String sql = QueryConditions.getPlotSql(type);
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		// 数据整理
		TimeSeries series = new TimeSeries("消费流水");
		// 遍历resultSet
		while (rs.next()) {
			series.add(new Day(rs.getDate("x")), rs.getDouble("y"));
		}
		h2.close();
		// 创建TimeSeriesCollection
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);
		// 返回
		return dataset;
	}

	/**
	 * 根据dataset创建TimeSeriesChart
	 * 
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChart(XYDataset dataset) {
		// 字体
		DefaultFont font = new DefaultFont();
		// 支持中文
		StandardChartTheme chartTheme = new StandardChartTheme("CN");
		// 设置字体
		chartTheme.setRegularFont(font.getFont(13f));
		chartTheme.setLargeFont(font.getFont(13f));
		chartTheme.setSmallFont(font.getFont(13f));
		// 应用
		ChartFactory.setChartTheme(chartTheme);
		// 创建时序图形
		JFreeChart chart = ChartFactory.createTimeSeriesChart("每日消费流水折线图", "日期", "金额 ", dataset, true, true, false);
		// 使当前主题马上生效
		ChartUtils.applyCurrentTheme(chart);
		// Plot
		plot = chart.getXYPlot();
		// y轴字体
		plot.getRangeAxis().setTickLabelFont(font.getFont(13f));
		// x轴格式化
		ValueAxis xAxis = plot.getDomainAxis();
		xAxis.setTickLabelFont(font.getFont(13f));
		((DateAxis) xAxis).setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
		// 渲染
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		plot.setRenderer(renderer);
		// 颜色设置
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);
		// 标题设置
		chart.setTitle(new TextTitle(""));
		// 移除legend
		chart.removeLegend();
		return chart;
	}

	/**
	 * 更新数据重回折现
	 * 
	 * @throws SQLException
	 */
	public void updatePlot() throws SQLException {
		logger.info("折线重绘");
		XYDataset dataset = createDataset();
		plot.setDataset(dataset);
	}
}
