package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.MessageDialog;
import main.MainFrame;
import models.RecordStructure;

public class InfoPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9004702478997478324L;

	// 输入框
	private JTextField[] tx = new JTextField[3];
	private static int TX_TIME = 0, TX_AMOUNT = 1, TX_REMARK = 2;
	// 下拉列表
	private JComboBox<String> account = new JComboBox<>(), type = new JComboBox<>(), label = new JComboBox<>();
	// 按钮
	private JButton[] btn = new JButton[4];
	private static int BUTTON_INSERT = 0, BUTTON_SAVE = 1, BUTTON_DEL = 2, BUTTON_CLEAR = 3;

	private DefaultFont font = new DefaultFont();

	private H2_DB h2 = null;

	private Logger logger = LogManager.getLogger();

	private MainFrame f = null;

	public InfoPanel(MainFrame frame) throws SQLException {

		// 父窗口指针
		f = frame;

		// 布局设置
		setLayout(null);
		Border tb1 = BorderFactory.createTitledBorder(new LineBorder(Color.DARK_GRAY), "记录信息新建或设置", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, font.getFont());
		setBorder(tb1);

		// 标签
		JLabel[] l = new JLabel[6];
		String[] lstr = { "时间：", "账户：", "类型：", "金额：", "标签：", "备注：" };
		for (int i = 0; i < l.length; i++) {
			l[i] = new JLabel(lstr[i]);
			l[i].setFont(font.getFont());
			add(l[i]);
			l[i].setBounds(30, 35 * i + 40, 50, 25);
		}
		// 输入框
		for (int i = 0; i < tx.length; i++) {
			tx[i] = new JTextField();
			tx[i].setFont(font.getFont());
			tx[i].setHorizontalAlignment(JTextField.CENTER);
			tx[i].setSelectedTextColor(Color.WHITE);
			tx[i].setSelectionColor(ThemeColor.BLUE);
			add(tx[i]);
		}
		tx[TX_TIME].setBounds(90, 40, 200, 25);
		tx[TX_AMOUNT].setBounds(90, 145, 200, 25);
		tx[TX_REMARK].setBounds(90, 215, 200, 25);

		// 下拉列表
		type.addItem("支出");
		type.addItem("收入");
		type.setFont(font.getFont());
		type.setBounds(90, 110, 80, 25);
		add(type);

		account.setFont(font.getFont());
		account.setBounds(90, 75, 100, 25);
		add(account);

		label.setFont(font.getFont());
		label.setBounds(90, 180, 100, 25);
		add(label);

		contentReset(null);

		String[] bstr = { "插入", "保存", "删除", "清空" };
		for (int i = 0; i < bstr.length; i++) {
			btn[i] = new JButton(bstr[i]);
			btn[i].setFont(font.getFont());
			btn[i].setForeground(Color.WHITE);
			btn[i].addActionListener(this);
			this.add(btn[i]);
			btn[i].setBounds(350, 50 * i + 40, 80, 30);
		}
		btn[BUTTON_INSERT].setBackground(ThemeColor.BLUE);
		btn[BUTTON_SAVE].setBackground(ThemeColor.BLUE);
		btn[BUTTON_DEL].setBackground(ThemeColor.RED);
		btn[BUTTON_CLEAR].setBackground(Color.LIGHT_GRAY);

		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(300, 280));
		validate();
	}

	public void contentReset(RecordStructure rds) throws SQLException {
		h2 = new H2_DB();
		String sql = "SELECT name FROM accounts";
		ResultSet rs = h2.query(sql);
		account.removeAllItems();
		while (rs.next()) {
			account.addItem(rs.getString("name"));
		}
		sql = "SELECT label FROM labels";
		rs = h2.query(sql);
		label.removeAllItems();
		while (rs.next()) {
			label.addItem(rs.getString("label"));
		}
		h2.close();

		tx[TX_TIME].setText(String.format("%1$tF %1$tT", Calendar.getInstance()));
		tx[TX_AMOUNT].setText(null);
		tx[TX_REMARK].setText(null);
		tx[TX_TIME].setEditable(true);
		if (rds != null) {
			tx[TX_TIME].setText(rds.getCreatetime());
			tx[TX_AMOUNT].setText(String.valueOf(rds.getAmount()));
			tx[TX_REMARK].setText(rds.getRemark());
			type.setSelectedIndex(rds.getType() == -1 ? 0 : 1);
			account.setSelectedItem(rds.getName());
			label.setSelectedItem(rds.getLabel());
			tx[TX_TIME].setEditable(false);
		}
	}

	private void insert() throws SQLException, ParseException, NumberFormatException {
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = ft.parse(tx[TX_TIME].getText());
		float amount = Float.parseFloat(tx[TX_AMOUNT].getText());
		int type = 0;
		if (this.type.getSelectedIndex() == 0) {
			type = -1;
		} else {
			type = 1;
		}
		String sql = String.format("INSERT INTO ledger VALUES ('%s', '%s', '%d', %.2f, '%s', '%s');", ft.format(date),
				account.getSelectedItem().toString(), type, amount, label.getSelectedItem().toString(),
				tx[TX_REMARK].getText());
		h2 = new H2_DB();
		h2.execute(sql);
		sql = String.format("UPDATE accounts SET balance = balance + %.2f WHERE accounts.`name` = '%s';", type * amount,
				account.getSelectedItem().toString());
		h2.execute(sql);
		h2.close();
	}

	private void delete() throws SQLException {
		String sql = String.format("DELETE FROM ledger WHERE createtime='%s'", tx[TX_TIME].getText());
		h2 = new H2_DB();
		h2.execute(sql);
		h2.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[BUTTON_INSERT]) {
			// 插入
			try {
				insert();
				f.updateTable();
				contentReset(null);
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (ParseException | NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(e1);
			}
		} else if (e.getSource() == btn[BUTTON_CLEAR]) {
			// 清除
			try {
				contentReset(null);
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == btn[BUTTON_DEL]) {
			// 删除
			try {
				delete();
				f.updateTable();
				contentReset(null);
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == btn[BUTTON_SAVE]) {
			// 更新保存
			try {
				delete();
				insert();
				f.updateTable();
				contentReset(null);
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			} catch (NumberFormatException | ParseException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(e1);
			}
		}
	}

}
