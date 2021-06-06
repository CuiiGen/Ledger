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
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import main.MainFrame;
import models.CustomListCellRenderer;
import tool.LogHelper;

public class TransferDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9004702478997478324L;

	// 输入框
	private JTextField[] tx = new JTextField[3];
	private static int TX_TIME = 0, TX_AMOUNT = 1, TX_REMARK = 2;
	// 下拉列表
	private JComboBox<String> from = new JComboBox<>(), to = new JComboBox<>();
	// 按钮
	private JButton[] btn = new JButton[2];
	private static int BUTTON_INSERT = 0, BUTTON_EXIT = 1;

	private DefaultFont font = new DefaultFont();

	private H2_DB h2 = null;
	// 标志位
	private boolean flag = false;

	private Logger logger = LogManager.getLogger();

	public TransferDialog(MainFrame frame, Point p, Dimension d) throws SQLException {
		// 父类构造函数
		super(frame, "转账汇款", true);
		// 布局设置
		setLayout(null);
		setResizable(false);
		// 窗口显示位置
		final int w = 440, h = 280;
		setBounds(p.x + (d.width - w) / 2, p.y + (d.height - h) / 2, w, h);
		// 标签
		JLabel[] l = new JLabel[4];
		String[] lstr = { "时间：", "账户：", "金额：", "备注：" };
		for (int i = 0; i < l.length; i++) {
			l[i] = new JLabel(lstr[i]);
			l[i].setFont(font.getFont());
			add(l[i]);
			l[i].setBounds(40, 35 * i + 40, 50, 25);
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
		tx[TX_TIME].setBounds(100, 40, 200, 25);
		tx[TX_TIME].setText(String.format("%1$tF %1$tT", Calendar.getInstance()));
		tx[TX_AMOUNT].setBounds(100, 110, 200, 25);
		tx[TX_REMARK].setBounds(100, 145, 200, 25);

		// 付款
		from.setFont(font.getFont());
		from.setBounds(100, 75, 120, 25);
		add(from);
		from.setRenderer(new CustomListCellRenderer());
		from.setOpaque(true);
		from.setBackground(ThemeColor.BLUE);
		from.setForeground(Color.WHITE);
		// 箭头
		JLabel arrow = new JLabel(">>");
		add(arrow);
		arrow.setFont(font.getFont(1));
		arrow.setBounds(220, 75, 25, 25);
		arrow.setHorizontalAlignment(SwingConstants.CENTER);
		// 收款
		to.setFont(font.getFont());
		to.setBounds(245, 75, 120, 25);
		add(to);
		to.setRenderer(new CustomListCellRenderer());
		to.setOpaque(true);
		to.setBackground(ThemeColor.BLUE);
		to.setForeground(Color.WHITE);

		h2 = new H2_DB();
		// 账户名下拉列表
		String sql = "SELECT name FROM accounts";
		ResultSet rs = h2.query(sql);
		while (rs.next()) {
			from.addItem(rs.getString("name"));
			to.addItem(rs.getString("name"));
		}
		h2.close();
		// 按钮设置
		String[] bstr = { "确定", "退出" };
		for (int i = 0; i < bstr.length; i++) {
			btn[i] = new JButton(bstr[i]);
			btn[i].setFont(font.getFont(1));
			btn[i].setForeground(Color.WHITE);
			btn[i].setBackground(ThemeColor.BLUE);
			btn[i].addActionListener(this);
			this.add(btn[i]);
		}
		btn[BUTTON_EXIT].setBackground(Color.DARK_GRAY);
		btn[BUTTON_INSERT].setBounds(130, 200, 80, 30);
		btn[BUTTON_EXIT].setBounds(230, 200, 80, 30);

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
	 * 新建转账相关的两条流水
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	private void insert() throws SQLException, ParseException, NumberFormatException {
		// 时间
		SimpleDateFormat ft1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
				ft2 = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = null;
		if (tx[TX_TIME].getText().matches("\\d{14}")) {
			date = ft2.parse(tx[TX_TIME].getText());
		} else if (tx[TX_TIME].getText().matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			date = ft1.parse(tx[TX_TIME].getText());
		} else {
			throw new ParseException(tx[TX_TIME].getText(), 0);
		}
		// 金额
		float amount = Float.parseFloat(tx[TX_AMOUNT].getText());
		// 数据库初始化
		h2 = new H2_DB();
		// 付款
		String sql = String.format("INSERT INTO ledger VALUES ('i', '%s', '%s', '%d', %.2f, '%s', '%s');", ft1.format(date),
				from.getSelectedItem(), -1, amount, "转账", "转账付款：" + tx[TX_REMARK].getText());
		logger.info("转账付款记录");
		logger.info(sql);
		h2.execute(sql);
		// 根据流水计算账户余额
		sql = String.format("UPDATE accounts SET balance = balance + %.2f WHERE accounts.`name` = '%s';", -amount,
				from.getSelectedItem());
		logger.info("转账付款账户修改金额");
		h2.execute(sql);
		// 收款
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, 1);
		sql = String.format("INSERT INTO ledger VALUES ('i', '%s', '%s', '%d', %.2f, '%s', '%s');",
				String.format("%1$tF %1$tT", c), to.getSelectedItem(), 1, amount, "转账",
				"转账收款：" + tx[TX_REMARK].getText());
		logger.info("转账收款记录");
		logger.info(sql);
		h2.execute(sql);
		// 根据流水计算账户余额
		sql = String.format("UPDATE accounts SET balance = balance + %.2f WHERE accounts.`name` = '%s';", amount,
				to.getSelectedItem());
		logger.info("转账收款账户修改金额");
		h2.execute(sql);
		h2.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn[BUTTON_EXIT]) {
			// 退出
			flag = false;
			dispose();
		} else if (e.getSource() == btn[BUTTON_INSERT]) {
			// 确认转账
			logger.info("开始进行转账流程");
			try {
				if (MessageDialog.showConfirm(this, "确认转账信息正确？") == JOptionPane.YES_OPTION) {
					logger.info("确认进行转账");
					insert();
					flag = true;
					dispose();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，保存失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (ParseException | NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		}

	}

}
