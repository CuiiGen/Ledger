package dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import main.MainFrame;
import models.CustomListCellRenderer;
import models.RecordStructure;

public class InfoDialog extends JDialog implements ActionListener {

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
	private JButton[] btn = new JButton[5];
	private static int BUTTON_INSERT = 0, BUTTON_SAVE = 1, BUTTON_REFUND = 2, BUTTON_DEL = 3, BUTTON_EXIT = 4;

	private DefaultFont font = new DefaultFont();

	private H2_DB h2 = null;

	private Logger logger = LogManager.getLogger();
	// 标志位
	private boolean flag = false;

	// 保存流水记录方便删除和修改
	RecordStructure rds = null;

	public InfoDialog(MainFrame frame, Point p, Dimension d, RecordStructure rds) throws SQLException {
		// 父类构造函数
		super(frame, "流水记录信息新建或设置", true);
		// 布局设置
		setLayout(null);
		setResizable(false);
		// 窗口显示位置
		final int w = 440, h = 350;
		setBounds(p.x + (d.width - w) / 2, p.y + (d.height - h) / 2, w, h);
		// 标签
		JLabel[] l = new JLabel[6];
		String[] lstr = { "时间：", "账户：", "类型：", "金额：", "标签：", "备注：" };
		for (int i = 0; i < l.length; i++) {
			l[i] = new JLabel(lstr[i]);
			l[i].setFont(font.getFont());
			add(l[i]);
			l[i].setBounds(60, 35 * i + 40, 50, 25);
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
		tx[TX_TIME].setBounds(140, 40, 200, 25);
		tx[TX_AMOUNT].setBounds(140, 145, 200, 25);
		tx[TX_REMARK].setBounds(140, 215, 200, 25);

		// 下拉列表
		type.addItem("支出");
		type.addItem("收入");
		type.setFont(font.getFont());
		type.setBounds(140, 110, 80, 25);
		add(type);

		type.setRenderer(new CustomListCellRenderer());
		type.setOpaque(true);
		type.setBackground(ThemeColor.BLUE);
		type.setForeground(Color.WHITE);

		account.setFont(font.getFont());
		account.setBounds(140, 75, 100, 25);
		add(account);
		account.setRenderer(new CustomListCellRenderer());
		account.setOpaque(true);
		account.setBackground(ThemeColor.BLUE);
		account.setForeground(Color.WHITE);

		h2 = new H2_DB();
		// 账户名下拉列表
		String sql = "SELECT name FROM accounts";
		ResultSet rs = h2.query(sql);
		while (rs.next()) {
			account.addItem(rs.getString("name"));
		}
		// 标签下拉列表
		label.addItem(null);
		sql = "SELECT label FROM labels";
		rs = h2.query(sql);
		while (rs.next()) {
			label.addItem(rs.getString("label"));
		}
		h2.close();

		label.setFont(font.getFont());
		label.setBounds(140, 180, 100, 25);
		add(label);
		label.setRenderer(new CustomListCellRenderer());
		label.setOpaque(true);
		label.setBackground(ThemeColor.BLUE);
		label.setForeground(Color.WHITE);

		String[] bstr = { "插入", "保存", "退款", "删除", "清空" };
		for (int i = 0; i < bstr.length; i++) {
			btn[i] = new JButton(bstr[i]);
			btn[i].setFont(font.getFont());
			btn[i].setForeground(Color.WHITE);
			btn[i].setBackground(ThemeColor.BLUE);
			btn[i].addActionListener(this);
			this.add(btn[i]);
		}
		btn[BUTTON_EXIT].setBackground(Color.DARK_GRAY);
		btn[BUTTON_DEL].setBackground(ThemeColor.ORANGE);
		if (rds == null) {
			btn[BUTTON_INSERT].setBounds(130, 270, 80, 30);
			btn[BUTTON_EXIT].setBounds(230, 270, 80, 30);
		} else {
			for (int i = 1; i < bstr.length; i++) {
				btn[i].setBounds(-70 + 100 * i, 270, 80, 30);
			}
		}
		contentReset(rds);

		setBackground(Color.WHITE);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * 显示窗口
	 * 
	 * @return
	 */
	public boolean showDialog() {
		setVisible(true);
		return flag;
	}

	/**
	 * 根据选中的流水记录更新组件内容
	 * 
	 * @param rds 选中的流水记录
	 * @throws SQLException
	 */
	public void contentReset(RecordStructure rds) throws SQLException {
		this.rds = rds;
		// 时间
		tx[TX_TIME].setText(String.format("%1$tF %1$tT", Calendar.getInstance()));
		tx[TX_AMOUNT].setText(null);
		tx[TX_REMARK].setText(null);
		tx[TX_TIME].setEditable(true);
		// 若记录非空
		if (rds != null) {
			btn[BUTTON_INSERT].setEnabled(false);
			tx[TX_TIME].setText(rds.getCreatetime());
			tx[TX_AMOUNT].setText(String.valueOf(rds.getAmount()));
			tx[TX_REMARK].setText(rds.getRemark());
			type.setSelectedIndex(rds.getType() == -1 ? 0 : 1);
			account.setSelectedItem(rds.getName());
			label.setSelectedItem(rds.getLabel());
			tx[TX_TIME].setEditable(false);
		}
	}

	/**
	 * 新建流水
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	private void insert() throws SQLException, ParseException, NumberFormatException {
		// 时间
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = ft.parse(tx[TX_TIME].getText());
		// 金额
		float amount = Float.parseFloat(tx[TX_AMOUNT].getText());
		// 收入或支出
		int type = 0;
		if (this.type.getSelectedIndex() == 0) {
			type = -1;
		} else {
			type = 1;
		}
		String sql = String.format("INSERT INTO ledger VALUES ('%s', '%s', '%d', %.2f, '%s', '%s');", ft.format(date),
				account.getSelectedItem(), type, amount, label.getSelectedItem(), tx[TX_REMARK].getText());
		if (label.getSelectedItem() == null) {
			sql = String.format("INSERT INTO ledger VALUES ('%s', '%s', '%d', %.2f, null, '%s');", ft.format(date),
					account.getSelectedItem(), type, amount, tx[TX_REMARK].getText());
		}
		h2 = new H2_DB();
		logger.info(sql);
		h2.execute(sql);
		// 根据流水计算账户余额
		sql = String.format("UPDATE accounts SET balance = balance + %.2f WHERE accounts.`name` = '%s';", type * amount,
				account.getSelectedItem().toString());
		h2.execute(sql);
		h2.close();
	}

	/**
	 * 删除流水
	 * 
	 * @throws SQLException
	 */
	private void delete() throws SQLException {
		h2 = new H2_DB();
		logger.info("删除流水记录");
		// 恢复余额
		String sql = String.format("UPDATE accounts SET balance = balance - %.2f WHERE accounts.`name` = '%s';",
				rds.getType() * rds.getAmount(), rds.getName());
		logger.info(sql);
		h2.execute(sql);
		sql = String.format("DELETE FROM ledger WHERE createtime='%s'", rds.getCreatetime());
		logger.info(sql);
		h2.execute(sql);
		h2.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[BUTTON_INSERT]) {
			// 插入
			try {
				insert();
				dispose();
				flag = true;
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (ParseException | NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(e1);
				e1.printStackTrace();
			}
		} else if (e.getSource() == btn[BUTTON_EXIT]) {
			// 清除
			flag = false;
			dispose();
		} else if (e.getSource() == btn[BUTTON_DEL]) {
			// 删除
			try {
				delete();
				dispose();
				flag = true;
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == btn[BUTTON_SAVE]) {
			// 更新保存
			try {
				delete();
				insert();
				dispose();
				flag = true;
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			} catch (NumberFormatException | ParseException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(e1);
			}
		} else if (e.getSource() == btn[BUTTON_REFUND]) {

		}
	}

}
