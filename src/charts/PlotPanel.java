package charts;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import panels.QueryConditions;

public class PlotPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8734246980656734214L;

	// 绘图区域
	private CategoryPlot plot = null;
	// 日志
	private Logger logger = LogManager.getLogger();

	public PlotPanel() throws SQLException {
		logger.info("每月流水图初始化 - 开始");
		// 获取数据
		DefaultCategoryDataset dataset = createDataset();
		// 创建图形
		JFreeChart chart = createChart(dataset);
		// 面板设置
		ChartPanel chartPanel = new ChartPanel(chart);
		// 布局设置
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		// 日志
		logger.info("每月流水图初始化 - 完成");
	}

	/**
	 * 创建dataset
	 * 
	 * @return
	 * @throws SQLException
	 */
	private DefaultCategoryDataset createDataset() throws SQLException {
		// 获取数据
		H2_DB h2 = new H2_DB();
		String sql = QueryConditions.getPlotSql();
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		// 数据整理
		// 遍历resultSet
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		while (rs.next()) {
			dataset.addValue(rs.getDouble("y"), "每月消费流水", rs.getString("x"));
		}
		h2.close();
		// 返回
		return dataset;
	}

	/**
	 * 根据dataset创建Chart
	 * 
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChart(DefaultCategoryDataset dataset) {
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
		// 创建柱状图图形
		JFreeChart chart = ChartFactory.createBarChart("每月消费流水折线图", "日期", "金额 ", dataset, PlotOrientation.VERTICAL,
				false, true, true);
		// 使当前主题马上生效
		ChartUtils.applyCurrentTheme(chart);

		// Plot
		plot = chart.getCategoryPlot();
		// y轴字体
		plot.getRangeAxis().setTickLabelFont(font.getFont(13f));
		// x轴格式化
		CategoryAxis xAxis = plot.getDomainAxis();
		xAxis.setTickLabelFont(font.getFont(13f));
		// 不显示标签
		xAxis.setLabel(null);
		plot.getRangeAxis().setLabel(null);
		// 颜色设置
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.DARK_GRAY);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.DARK_GRAY);

		// 柱状图渲染器
		BarRenderer barRenderer = new BarRenderer();
		// 取消渐变效果
		barRenderer.setBarPainter(new StandardBarPainter());
		// 可用宽度的最宽比例
		barRenderer.setMaximumBarWidth(0.03);
		// 不显示引用
		barRenderer.setShadowVisible(false);
		// 颜色
		barRenderer.setSeriesPaint(0, ThemeColor.LIGHT_BLUE);
		// 标签
		barRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		barRenderer.setDefaultItemLabelsVisible(true);
		barRenderer.setDefaultItemLabelFont(font.getFont(1));
		// 应用渲染
		plot.setRenderer(0, barRenderer);
		// 折线图渲染
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setSeriesPaint(0, ThemeColor.BLUE);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		// 显示折线图
		plot.setDataset(1, dataset);
		plot.setRenderer(1, renderer);
		// 设置两个图的前后顺序
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		// 标题设置
		chart.setTitle(new TextTitle(""));
		return chart;
	}

	/**
	 * 更新数据重回折现
	 * 
	 * @throws SQLException
	 */
	public void updatePlot() throws SQLException {
		logger.info("折线重绘");
		DefaultCategoryDataset dataset = createDataset();
		plot.setDataset(dataset);
		plot.setDataset(1, dataset);
	}
}
