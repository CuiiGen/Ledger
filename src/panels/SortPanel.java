package panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import main.MainFrame;
import models.CustomListCellRenderer;

public class SortPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3949915897977563970L;

	// 时间输入框
	private JTextField[] tx = new JTextField[2];
	// 类型和标签
	private JComboBox<String> type = new JComboBox<>(), tag = new JComboBox<>();
	// 按钮
	private JButton[] btn = new JButton[2];
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
		tx[0].setText(QueryConditions.getStartTime());
		tx[1].setText(QueryConditions.getStopTime());
		// 标签设置
		JLabel[] labels = new JLabel[4];
		String[] lstr = { "起始时间", "结束时间", "类别：", "标签：" };
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
		tag.addItem("全部");
		tag.addItem(null);
		h2 = new H2_DB();
		String sql = "SELECT label FROM labels";
		ResultSet rs = h2.query(sql);
		while (rs.next()) {
			tag.addItem(rs.getString("label"));
		}
		h2.close();
		// 按钮
		String[] bstr = { "筛选", "重置" };
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
		vbox.add(Box.createVerticalStrut(20));
		// 时间
		vbox.add(labels[0]);
		vbox.add(tx[0]);
		vbox.add(labels[1]);
		vbox.add(tx[1]);
		vbox.add(Box.createVerticalStrut(10));
		// 类别
		Box hbox1 = Box.createHorizontalBox(), hbox2 = Box.createHorizontalBox();
		hbox1.add(labels[2]);
		hbox1.add(Box.createHorizontalStrut(5));
		hbox1.add(type);
		vbox.add(hbox1);
		// 标签
		vbox.add(Box.createVerticalStrut(7));
		hbox2.add(labels[3]);
		hbox2.add(Box.createHorizontalStrut(5));
		hbox2.add(tag);
		vbox.add(hbox2);
		vbox.add(Box.createVerticalStrut(10));
		// 按钮
		add(Box.createHorizontalStrut(5));
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(btn[0]);
		hbox3.add(Box.createHorizontalStrut(20));
		hbox3.add(btn[1]);
		vbox.add(hbox3);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[0]) {
			try {
				QueryConditions.setStartTime(tx[0].getText());
				QueryConditions.setStopTime(tx[1].getText());
				QueryConditions.setLabel(tag.getSelectedItem().toString());
				QueryConditions.setType(type.getSelectedIndex());
				f.updateLedger();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
