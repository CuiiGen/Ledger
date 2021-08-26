package panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
	// 数据库
	private H2_DB h2 = null;
	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();
	// 父窗口指针
	private MainFrame f = null;

	public SortPanel(MainFrame frame) throws SQLException {
		// 父窗口指针
		f = frame;
		// 输入窗口
		for (int i = 0; i < tx.length; i++) {
			tx[i] = new JTextField(10);
			tx[i].setHorizontalAlignment(JTextField.CENTER);
			tx[i].setFont(font.getFont());
			tx[i].setSelectedTextColor(Color.WHITE);
			tx[i].setSelectionColor(ThemeColor.BLUE);
		}
		// 标签设置
		JLabel[] labels = new JLabel[5];
		String[] lstr = { "起始时间", "结束时间", "类别：", "标签：", "全局模糊搜索" };
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
			this.add(btn[i]);
		}
		// 最外层纵向box
		Box vbox = Box.createVerticalBox();
		add(Box.createHorizontalStrut(5));
		add(vbox);
		// 最上层空白
		vbox.add(Box.createVerticalStrut(10));
		// 时间
		vbox.add(labels[0]);
		vbox.add(tx[0]);
		vbox.add(labels[1]);
		vbox.add(tx[1]);
		// 空白
		vbox.add(Box.createVerticalStrut(7));
		// 上下个月
		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(btn[2]);
		hbox4.add(Box.createHorizontalStrut(15));
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
		vbox.add(Box.createVerticalStrut(7));
		// 标签
		hbox2.add(labels[3]);
		hbox2.add(Box.createHorizontalStrut(5));
		hbox2.add(tag);
		vbox.add(hbox2);
		// 空白
		vbox.add(Box.createVerticalStrut(10));
		// 按钮
		add(Box.createHorizontalStrut(5));
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(btn[0]);
		hbox3.add(Box.createHorizontalStrut(20));
		hbox3.add(btn[1]);
		vbox.add(hbox3);
		// 空白
		vbox.add(Box.createVerticalStrut(50));
		// 搜索框
		vbox.add(labels[4]);
		vbox.add(tx[2]);
		tx[2].addActionListener(this);

		setBackground(Color.WHITE);

		Border tb1 = BorderFactory.createMatteBorder(0, 0, 0, 3, ThemeColor.BACKGROUND);
		setBorder(tb1);
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
		tx[0].setText(QueryConditions.getStartTime());
		tx[1].setText(QueryConditions.getStopTime());
		type.setSelectedIndex(QueryConditions.getType());
		tag.setSelectedItem(QueryConditions.getLabel());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[0]) {
			// 筛选
			QueryConditions.setIsFuzzy(false);
			try {
				if (tx[0].getText().matches("\\d{4}-\\d{1,2}-\\d{1,2}") == false) {
					throw new ParseException(tx[0].getText(), 0);
				}
				if (tx[1].getText().matches("\\d{4}-\\d{1,2}-\\d{1,2}") == false) {
					throw new ParseException(tx[1].getText(), 0);
				}
				// 设置筛选条件
				QueryConditions.setStartTime(tx[0].getText());
				QueryConditions.setStopTime(tx[1].getText());
				QueryConditions.setLabel(tag.getSelectedItem().toString());
				QueryConditions.setType(type.getSelectedIndex());
				// 筛选结果更新
				f.updateLedger();
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (ParseException e1) {
				MessageDialog.showError(f, "日期格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == btn[1]) {
			// 重置筛选
			QueryConditions.setIsFuzzy(false);
			try {
				QueryConditions.init();
				updateContent();
				f.updateLedger();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == tx[2]) {
			// 筛选结果更新
			QueryConditions.setFuzzyWord(tx[2].getText());
			tx[2].setText(null);
			try {
				f.updateLedger();
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
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，查询失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		}
	}
}
