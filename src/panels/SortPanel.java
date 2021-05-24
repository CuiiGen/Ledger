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

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import models.CustomListCellRenderer;

public class SortPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3949915897977563970L;

	private JTextField[] tx = new JTextField[2];
	private DefaultFont font = new DefaultFont();
	private JComboBox<String> type = new JComboBox<>(), tag = new JComboBox<>();
	private H2_DB h2 = null;
	private JButton[] btn = new JButton[2];

	public SortPanel() throws SQLException {
		for (int i = 0; i < tx.length; i++) {
			tx[i] = new JTextField(10);
			tx[i].setHorizontalAlignment(JTextField.CENTER);
			tx[i].setFont(font.getFont());
			tx[i].setSelectedTextColor(Color.WHITE);
			tx[i].setSelectionColor(ThemeColor.BLUE);
		}
		// 标签
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

		// 布局
		vbox.add(Box.createVerticalStrut(20));
		vbox.add(labels[0]);
		vbox.add(tx[0]);
		vbox.add(labels[1]);
		vbox.add(tx[1]);
		vbox.add(Box.createVerticalStrut(10));
		Box hbox1 = Box.createHorizontalBox(), hbox2 = Box.createHorizontalBox();
		hbox1.add(labels[2]);
		hbox1.add(Box.createHorizontalStrut(5));
		hbox1.add(type);
		vbox.add(hbox1);
		vbox.add(Box.createVerticalStrut(7));
		hbox2.add(labels[3]);
		hbox2.add(Box.createHorizontalStrut(5));
		hbox2.add(tag);
		vbox.add(hbox2);
		vbox.add(Box.createVerticalStrut(10));

		add(Box.createHorizontalStrut(5));
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(btn[0]);
		hbox3.add(Box.createHorizontalStrut(20));
		hbox3.add(btn[1]);
		vbox.add(hbox3);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
