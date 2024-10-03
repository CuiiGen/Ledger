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
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import main.MainFrame;
import models.CustomListCellRenderer;
import models.RecordStructure;
import panels.QueryConditions;
import tool.LogHelper;

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
	private JButton[] btn = new JButton[2];
	private static int BUTTON_OK = 0, BUTTON_EXIT = 1;
	// 是否有效记录
	private JCheckBox isValid = new JCheckBox("有效记录");
	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();
	// 标志位
	private boolean flag = false;

	// 保存流水记录方便删除和修改
	private RecordStructure rds = null;
	// 判断是否为退款窗口，否则则是修改信息窗口
	// `rds == null`表示新建流水，优先级最高
	private int purpose = 0;

	// 调用该函数的目的：新建、更新、退款或再来一笔
	public static int PUR_NEW = 0, PUR_UPDATE = 1, PUR_REFUND = 2, PUR_ONE_MORE = 3;

	public InfoDialog(MainFrame frame, Point p, Dimension d, RecordStructure rds, int purpose) throws SQLException {
		// 父类构造函数
		super(frame, "流水记录信息新建或设置", true);
		// 布局设置
		setLayout(null);
		setResizable(false);
		// 窗口显示位置
		final int w = 440, h = 370;
		setBounds(p.x + (d.width - w) / 2, p.y + (d.height - h) / 2, w, h);
		// 标签
		JLabel[] l = new JLabel[6];
		String[] lstr = { "时间：", "账户：", "类型：", "金额：", "标签：", "备注：" };
		for (int i = 0; i < l.length; i++) {
			l[i] = new JLabel(lstr[i]);
			l[i].setFont(font.getFont());
			add(l[i]);
			l[i].setBounds(50, 35 * i + 40, 50, 25);
		}
		// 输入框
		for (int i = 0; i < tx.length; i++) {
			tx[i] = new JTextField();
			tx[i].setFont(font.getFont());
			tx[i].setHorizontalAlignment(JTextField.CENTER);
			tx[i].setSelectedTextColor(Color.WHITE);
			tx[i].setSelectionColor(ThemeColor.BLUE);
			tx[i].addActionListener(this);
			add(tx[i]);
		}
		tx[TX_TIME].setBounds(120, 40, 200, 25);
		tx[TX_AMOUNT].setBounds(120, 145, 200, 25);
		tx[TX_REMARK].setBounds(120, 215, 200, 25);

		// 下拉列表
		type.addItem("支出");
		type.addItem("收入");
		type.setFont(font.getFont());
		type.setRenderer(new CustomListCellRenderer());
		type.setOpaque(true);
		type.setBackground(ThemeColor.BLUE);
		type.setForeground(Color.WHITE);
		type.setBounds(120, 110, 80, 25);
		add(type);
		// 账户名
		account.setFont(font.getFont());
		account.setRenderer(new CustomListCellRenderer());
		account.setOpaque(true);
		account.setBackground(ThemeColor.BLUE);
		account.setForeground(Color.WHITE);
		account.setBounds(120, 75, 120, 25);
		add(account);
		// 是否有效记录
		isValid.setFont(font.getFont());
		add(isValid);
		isValid.setBounds(220, 110, 120, 25);
		isValid.setBackground(null);
		isValid.setSelected(true);

		try (H2_DB h2 = new H2_DB()) {
			// 账户名下拉列表
			String sql = "SELECT name FROM accounts";
			ResultSet rs = h2.query(sql);
			while (rs.next()) {
				account.addItem(rs.getString("name"));
			}
			// 标签下拉列表
			label.addItem(QueryConditions.nullPopItem);
			sql = "SELECT label FROM labels ORDER BY createtime DESC";
			rs = h2.query(sql);
			while (rs.next()) {
				label.addItem(rs.getString("label"));
			}
			h2.close();
		}

		label.setFont(font.getFont());
		label.setBounds(120, 180, 100, 25);
		add(label);
		label.setRenderer(new CustomListCellRenderer());
		label.setOpaque(true);
		label.setBackground(ThemeColor.BLUE);
		label.setForeground(Color.WHITE);

		// 设置成员变量
		this.rds = rds;
		this.purpose = purpose;

		// 确认按钮显示文字及窗口标题设置
		String btn_label = null;
		if (this.purpose == PUR_NEW || this.purpose == PUR_ONE_MORE) {
			setTitle("新建流水");
			btn_label = "插入";
		} else if (this.purpose == PUR_REFUND) {
			setTitle("退款信息修改及确认");
			btn_label = "退款";
		} else {
			setTitle("流水信息确认");
			btn_label = "保存";
		}
		// 按钮其他设置
		String[] bstr = { btn_label, "退出" };
		for (int i = 0; i < bstr.length; i++) {
			btn[i] = new JButton(bstr[i]);
			btn[i].setFont(font.getFont(1));
			btn[i].setForeground(Color.WHITE);
			btn[i].setBackground(ThemeColor.BLUE);
			btn[i].addActionListener(this);
			this.add(btn[i]);
		}
		btn[BUTTON_EXIT].setBackground(Color.DARK_GRAY);
		btn[BUTTON_OK].setBounds(130, 270, 80, 30);
		btn[BUTTON_EXIT].setBounds(230, 270, 80, 30);

		// 内容设置
		contentReset();

		getContentPane().setBackground(ThemeColor.APPLE);

		// 关闭设置
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
	 * @throws SQLException
	 */
	private void contentReset() throws SQLException {
		// 时间
		tx[TX_TIME].setText(String.format("%1$tF %1$tT", Calendar.getInstance()));
		// 若记录非空
		if (rds != null) {
			tx[TX_AMOUNT].setText(String.valueOf(rds.getAmount()));
			tx[TX_REMARK].setText(rds.getRemark());
			type.setSelectedIndex(rds.getType() == -1 ? 0 : 1);
			account.setSelectedItem(rds.getName());
			label.setSelectedItem(rds.getLabel());
			isValid.setSelected(rds.getIsValid());
			if (purpose == PUR_REFUND) {
				// 不计入账单
				isValid.setSelected(false);
				// 退款需要切换收入/支出状态
				type.setSelectedIndex(1 - type.getSelectedIndex());
				// 退款时收支类型及退款标签不可更改
				type.setEnabled(false);
				label.setEnabled(false);
				isValid.setEnabled(false);
			} else {
				// 修改信息则显示之前时间
				tx[TX_TIME].setText(rds.getCreatetime());
			}
		}
	}

	/**
	 * 插入或保存流水
	 * 
	 * @param date          流水交易时间，调用该函数新建流水前需要校验时间输入是否准确，修改保存时无需校验
	 * @param reimbursement 相关联报销单号，为0表示null
	 * @throws SQLException
	 * @throws NumberFormatException
	 */
	private void insertUnsafe(H2_DB h2, Date date, int reimbursement) throws SQLException, NumberFormatException {
		logger.info("插入流水记录");
		// 时间
		SimpleDateFormat ft1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 金额
		float amount = Float.parseFloat(tx[TX_AMOUNT].getText());
		// 收入或支出
		int type = 0;
		if (this.type.getSelectedIndex() == 0) {
			type = -1;
		} else {
			type = 1;
		}
		// 报销
		String reimbursementSQL = reimbursement == 0 ? "null" : String.valueOf(reimbursement);
		// label
		String sql = null;
		String i = isValid.isSelected() ? "o" : "i";
		if (label.getSelectedItem() == QueryConditions.nullPopItem) {
			sql = String.format("INSERT INTO ledger VALUES ('%s', '%s', '%s', '%d', %.2f, null, '%s', %s);", i,
					ft1.format(date), account.getSelectedItem(), type, amount, tx[TX_REMARK].getText(),
					reimbursementSQL);
		} else {
			sql = String.format("INSERT INTO ledger VALUES ('%s', '%s', '%s', '%d', %.2f, '%s', '%s', %s);", i,
					ft1.format(date), account.getSelectedItem(), type, amount, label.getSelectedItem(),
					tx[TX_REMARK].getText(), reimbursementSQL);
		}
		// 执行SQL
		logger.info(sql);
		h2.execute(sql);
		// 根据流水计算账户余额
		sql = String.format("UPDATE accounts SET balance = balance + %.2f WHERE accounts.`name` = '%s';", type * amount,
				account.getSelectedItem().toString());
		logger.info(sql);
		h2.execute(sql);
	}

	/**
	 * 删除流水
	 * 
	 * @throws SQLException
	 */
	private void deleteUnsafe(H2_DB h2) throws SQLException {
		logger.info("删除流水记录");
		// 恢复余额
		String sql = String.format("UPDATE accounts SET balance = balance - %.2f WHERE accounts.`name` = '%s';",
				rds.getType() * rds.getAmount(), rds.getName());
		logger.info(sql);
		h2.execute(sql);
		sql = String.format("DELETE FROM ledger WHERE createtime='%s'", rds.getCreatetime());
		logger.info(sql);
		h2.execute(sql);
	}

	private void insert(Date date, int reimbursement) throws SQLException, NumberFormatException {
		try (H2_DB h2 = new H2_DB()) {
			h2.setAutoCommit(false);
			this.insertUnsafe(h2, date, reimbursement);
			h2.commit();
			h2.close();
		}
	}

	/**
	 * update information by deleting and then inserting a new one
	 * 
	 * @param date
	 * @throws SQLException
	 * @throws NumberFormatException
	 */
	private void update(Date date, int reimbursement) throws SQLException, NumberFormatException {
		try (H2_DB h2 = new H2_DB()) {
			h2.setAutoCommit(false);
			this.deleteUnsafe(h2);
			this.insertUnsafe(h2, date, reimbursement);
			h2.commit();
			h2.close();
		}
	}

	/**
	 * 退款
	 * 
	 * @throws SQLException
	 */
	private void refundUnsafe(H2_DB h2) throws SQLException, NumberFormatException {
		logger.info("退款记录保存");
		// 原纪录备注更改
		String sql = String.format("UPDATE ledger SET isValid = 'i', remark = '%s' WHERE createtime = '%s'",
				rds.getRemark() + "，已退款！", rds.getCreatetime());
		logger.info("原纪录备注更改");
		logger.info(sql);
		h2.execute(sql);
	}

	private void refund(Date date, int reimbursement) throws SQLException, NumberFormatException {
		try (H2_DB h2 = new H2_DB()) {
			h2.setAutoCommit(false);
			this.refundUnsafe(h2);
			this.insertUnsafe(h2, date, reimbursement);
			h2.commit();
			h2.close();
		}
	}

	private float arithmetic(String formula) throws NumberFormatException {
		// numbers and operators
		Deque<Float> nums = new LinkedList<Float>();
		Deque<Character> operators = new LinkedList<Character>();
		// cast to array of char
		char[] chr = formula.toCharArray();
		// operator of the first number
		if (chr[0] >= '0' && chr[0] <= '9' || chr[0] == '.') {
			operators.push('+');
		} else if (chr[0] == '+' || chr[0] == '-') {
		} else {
			throw new NumberFormatException("算式有误，第一个字符错误！");
		}

		int i = 0, j = 1;
		for (; i < formula.length(); i = j, j++) {
			if (chr[i] >= '0' && chr[i] <= '9' || chr[i] == '.') {
				for (; j < formula.length() && (chr[j] >= '0' && chr[j] <= '9' || chr[j] == '.'); j++)
					;
				// from i to j-1
				float num = Float.parseFloat(formula.substring(i, j));
				if (!operators.isEmpty()) {
					char opr = operators.peek();
					if (opr == '*') {
						operators.pop();
						num = nums.pop() * num;
					} else if (opr == '/') {
						operators.pop();
						num = nums.pop() / num;
					}
					nums.push(num);
				}
			} else if (chr[i] == '+' || chr[i] == '-' || chr[i] == '*' || chr[i] == '/') {
				operators.push(chr[i]);
			} else {
				throw new NumberFormatException("算式有误，无效字符！");
			}
		}
		float result = 0;
		if (operators.size() != nums.size()) {
			throw new NumberFormatException("算式有误，数值和计算符数量不匹配");
		}
		while (!nums.isEmpty()) {
			if (operators.pop() == '+') {
				result += nums.pop();
			} else {
				result -= nums.pop();

			}
		}
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// 退出界面，不在进行操作
		if (e.getSource() == btn[BUTTON_EXIT]) {
			flag = false;
			dispose();
			return;
		}
		// 是否确认
		boolean confirmed = e.getSource() == btn[BUTTON_OK] || e.getSource() == tx[TX_REMARK]
				|| e.getSource() == tx[TX_TIME];
		// 时间校验和格式化
		Date date = null;
		SimpleDateFormat ft1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
				ft2 = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			if (tx[TX_TIME].getText().matches("\\d{14}")) {
				date = ft2.parse(tx[TX_TIME].getText());
			} else if (tx[TX_TIME].getText().matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
				date = ft1.parse(tx[TX_TIME].getText());
			} else {
				throw new ParseException(tx[TX_TIME].getText(), 0);
			}
		} catch (ParseException e1) {
			MessageDialog.showError(this, "交易时间输入错误！");
			logger.error(LogHelper.exceptionToString(e1));
			return;
		}
		// 操作选择
		if ((purpose == PUR_NEW || purpose == PUR_ONE_MORE) && confirmed) {
			// 插入
			try {
				if (MessageDialog.showConfirm(this, "流水时间无法更改，确认为：" + ft1.format(date)) == JOptionPane.YES_OPTION) {
					logger.info("确认插入");
					insert(date, 0);
					flag = true;
					dispose();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，插入失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (purpose == PUR_UPDATE && confirmed) {
			// 更新保存
			try {
				update(date, rds.getReimbursement());
				dispose();
				flag = true;
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，插入失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (purpose == PUR_REFUND && confirmed) {
			// 退款
			try {
				if (MessageDialog.showConfirm(this, "确认该订单已退款？") == JOptionPane.YES_OPTION) {
					refund(date, rds.getReimbursement());
					flag = true;
					dispose();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，插入失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (NumberFormatException e1) {
				MessageDialog.showError(this, "数据格式错误！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == tx[TX_AMOUNT]) {
			String formula = tx[TX_AMOUNT].getText();
			try {
				if (!formula.isEmpty()) {
					tx[TX_AMOUNT].setText(String.format("%.2f", arithmetic(formula)));
				}
			} catch (NumberFormatException e2) {
				MessageDialog.showError(this, e2.getMessage());
			}
		}
	}
}
