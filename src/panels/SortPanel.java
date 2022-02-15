package panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.MessageDialog;
import main.MainFrame;
import models.CustomListCellRenderer;
import tool.LogHelper;

public class SortPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3949915897977563970L;

	// 时间输入框
	private JTextField[] tx = new JTextField[3];
	// 类型和标签
	private JComboBox<String> type = new JComboBox<>(), tag = new JComboBox<>();
	// 按钮
	private JButton[] btn = new JButton[4];
	// 复选框
	private JCheckBox isValid = new JCheckBox("仅显示有效数据");
	// 数据库
	private H2_DB h2 = null;
	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();
	// 父窗口指针
	private MainFrame f = null;

	public SortPanel(MainFrame frame) throws SQLException {
		logger.info("查询面板初始化 - 开始");
		// 父窗口指针
		f = frame;
		// 输入窗口
		for (int i = 0; i < tx.length; i++) {
			tx[i] = new JTextField(10);
			tx[i].setHorizontalAlignment(JTextField.CENTER);
			tx[i].setFont(font.getFont());
			tx[i].setSelectedTextColor(Color.WHITE);
			tx[i].setSelectionColor(ThemeColor.BLUE);
			tx[i].addActionListener(this);
		}
		// 标签设置
		JLabel[] labels = new JLabel[5];
		String[] lstr = { "起始时间：", "结束时间：", "类别：", "标签：", "全局模糊搜索：" };
		for (int i = 0; i < lstr.length; i++) {
			labels[i] = new JLabel(lstr[i]);
			labels[i].setFont(font.getFont());
		}
		// 类别
		type.setRenderer(new CustomListCellRenderer());
		type.setFont(font.getFont());
		type.setOpaque(true);
		type.setBackground(ThemeColor.BLUE);
		type.setForeground(Color.WHITE);
		type.addItem("全部");
		type.addItem("支出");
		type.addItem("收入");
		// 标签下拉列表
		tag.setRenderer(new CustomListCellRenderer());
		tag.setFont(font.getFont());
		tag.setOpaque(true);
		tag.setBackground(ThemeColor.BLUE);
		tag.setForeground(Color.WHITE);
		// 更新内容
		QueryConditions.init();
		updateContent();
		// 按钮
		String[] bstr = { "筛选", "重置", "<<", ">>" };
		for (int i = 0; i < bstr.length; i++) {
			btn[i] = new JButton(bstr[i]);
			btn[i].setFont(font.getFont());
			btn[i].setForeground(Color.WHITE);
			btn[i].setBackground(ThemeColor.BLUE);
			btn[i].addActionListener(this);
		}

		// 绑定快捷键
		btn[2].registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		btn[3].registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		btn[1].registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		isValid.setFont(font.getFont());
		isValid.setBackground(Color.WHITE);
		isValid.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					QueryConditions.setIsValid(isValid.isSelected());
					f.updateLedger();
					logger.info("勾选复选框，刷新表格 - 完成\n");
				} catch (SQLException e1) {
					MessageDialog.showError(f, "数据库访问错误，查询失败！");
					logger.error(LogHelper.exceptionToString(e1));
				}
			}
		});
		// 最外层纵向box
		Box vbox = Box.createVerticalBox();
		add(Box.createHorizontalStrut(5));
		add(vbox);
		add(Box.createHorizontalStrut(5));

		// 最上层空白
		vbox.add(Box.createVerticalStrut(10));
		// 时间框
		vbox.add(getHorizontalBox(labels[0]));
		vbox.add(Box.createVerticalStrut(5));
		vbox.add(tx[0]);
		vbox.add(Box.createVerticalStrut(5));
		vbox.add(getHorizontalBox(labels[1]));
		vbox.add(Box.createVerticalStrut(5));
		vbox.add(tx[1]);
		// 空白
		vbox.add(Box.createVerticalStrut(7));
		// 上下个月切换按钮
		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(btn[2]);
		hbox4.add(Box.createHorizontalStrut(20));
		hbox4.add(btn[3]);
		vbox.add(hbox4);
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 类别
		Box hbox1 = Box.createHorizontalBox(), hbox2 = Box.createHorizontalBox();
		hbox1.add(labels[2]);
		hbox1.add(Box.createHorizontalStrut(5));
		hbox1.add(type);
		vbox.add(hbox1);
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 标签
		hbox2.add(labels[3]);
		hbox2.add(Box.createHorizontalStrut(5));
		hbox2.add(tag);
		vbox.add(hbox2);
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 确认和重置按钮
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(btn[0]);
		hbox3.add(Box.createHorizontalStrut(20));
		hbox3.add(btn[1]);
		vbox.add(hbox3);
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 是否仅显示有效数据
		vbox.add(getHorizontalBox(isValid));
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 搜索框
		vbox.add(getHorizontalBox(labels[4]));
		vbox.add(Box.createVerticalStrut(5));
		vbox.add(tx[2]);

		setBackground(Color.WHITE);

		Border tb1 = BorderFactory.createMatteBorder(0, 0, 0, 3, ThemeColor.BACKGROUND);
		setBorder(tb1);
		logger.info("查询面板初始化 - 完成");
	}

	private Box getHorizontalBox(JComponent component) {
		Box h = Box.createHorizontalBox();
		h.add(component);
		h.add(Box.createHorizontalGlue());
		return h;
	}

	/**
	 * 更新页面内容
	 * 
	 * @throws SQLException
	 */
	public void updateContent() throws SQLException {
		logger.info("查询面板内容更新");
		tag.removeAllItems();
		tag.addItem("全部");
		// 两个空格
		tag.addItem("  ");
		h2 = new H2_DB();
		String sql = "SELECT label FROM labels";
		ResultSet rs = h2.query(sql);
		while (rs.next()) {
			tag.addItem(rs.getString("label"));
		}
		h2.close();
		// 起始和结束时间
		tx[0].setText(QueryConditions.getStartTime());
		tx[1].setText(QueryConditions.getStopTime());
		// 收入或支出
		type.setSelectedIndex(QueryConditions.getType());
		// 标签
		tag.setSelectedItem(QueryConditions.getLabel());
		isValid.setSelected(QueryConditions.getIsValid());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[0] || e.getSource() == tx[0] || e.getSource() == tx[1]) {
			// 筛选
			logger.info("根据筛选条件刷新表格 - 开始");
			QueryConditions.setIsFuzzy(false);
			try {
				SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
				// 验证起始时间字符串格式是否正确
				Date date = ft.parse(tx[0].getText());
				tx[0].setText(ft.format(date));
				// 验证结束时间字符串格式是否正确
				date = ft.parse(tx[1].getText());
				tx[1].setText(ft.format(date));
				// 设置筛选条件
				QueryConditions.setStartTime(tx[0].getText());
				QueryConditions.setStopTime(tx[1].getText());
				QueryConditions.setLabel(tag.getSelectedItem().toString());
				QueryConditions.setType(type.getSelectedIndex());
				// 筛选结果更新
				f.updateLedger();
				logger.info("根据筛选条件刷新表格 - 完成\n");
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (ParseException e1) {
				MessageDialog.showError(f, "日期格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == btn[1]) {
			// 重置筛选
			logger.info("重置筛选页面 - 开始");
			QueryConditions.setIsFuzzy(false);
			try {
				QueryConditions.init();
				updateContent();
				f.updateLedger();
				logger.info("重置筛选页面 - 完成\n");
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == tx[2]) {
			// 模糊搜索结果更新
			logger.info("模糊筛选 - 开始");
			QueryConditions.setFuzzyWord(tx[2].getText());
			tx[2].setText(null);
			try {
				f.updateLedger();
				logger.info("模糊筛选 - 完成\n");
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else {
			// 上下个月
			if (e.getSource() == btn[2]) {
				QueryConditions.nextMonth(-1);
				logger.info(String.format("上个月为：%s -> %s", tx[0].getText(), tx[1].getText()));
			} else {
				QueryConditions.nextMonth(1);
				logger.info(String.format("下个月为：%s -> %s", tx[0].getText(), tx[1].getText()));
			}
			tx[0].setText(QueryConditions.getStartTime());
			tx[1].setText(QueryConditions.getStopTime());
			try {
				f.updateLedger();
				logger.info("月份切换成功\n");
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		}
	}
}
