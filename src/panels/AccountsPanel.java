package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.MessageDialog;
import main.MainFrame;
import models.AccountStructure;
import tool.LogHelper;

public class AccountsPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6063182420495622194L;

	// 表格及数据列表
	private ArrayList<AccountStructure> array = new ArrayList<>();
	private JTable table = new JTable(new AccountsModel(array)) {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8938369871665016790L;

		public void editingStopped(ChangeEvent e) {
			int r = getEditingRow();
			int c = getEditingColumn();
			String pre = table.getValueAt(r, c).toString();
			super.editingStopped(e);
			String newName = table.getValueAt(r, c).toString();
			// 如果未更改则直接退出后续处理
			if (pre.equals(newName)) {
				return;
			}
			try {
				// 检验账户名是否重复
				if (isNameUnique(newName) == false) {
					MessageDialog.showError(f, "注意账户名不可重复！");
					logger.error("输入了重复账户名：" + newName);
					// 重置表格
					updateTable();
					return;
				}
				logger.info("经校验，输入的账户名当前数据库中不存在，可用");
				if (r == array.size() - 1) {
					// 新建
					logger.info("准备新建账户");
					if (table.getValueAt(r, c).equals("") || table.getValueAt(r, c).equals("双击输入账户名")) {
						logger.error("账户名不能空或为双击输入账户名\n");
					} else {
						insertAccount(newName);
						updateTable();
						logger.info("账户名非禁止，新建成功\n");
					}
				} else {
					// 更新
					logger.info("更新账户名");
					String sql = String.format("UPDATE `accounts` SET `name`='%s' WHERE `name`='%s'", newName, pre);
					h2 = new H2_DB();
					logger.info(sql);
					h2.execute(sql);
					h2.close();
					// 如果账户名修改设计筛选条件中账户名则进行更新
					if (QueryConditions.getInstance().getName().equals(pre)) {
						QueryConditions.getInstance().setName(newName);
					}
					f.updateLedger();
					logger.info("账户名更新成功\n");
				}
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库访问错误，注意账户名不可重复！");
				logger.error(LogHelper.exceptionToString(e1));
				table.setValueAt(pre, r, c);
				table.updateUI();
			}
		};
	};

	// 内部类
	private class CellRenderer extends DefaultTableCellRenderer implements MouseMotionListener, MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1108124244265230115L;

		int at = -1;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setHorizontalAlignment(SwingConstants.CENTER);
			if (at == row) {
				setBackground(ThemeColor.LIGHT_BLUE);
			} else if (row % 2 == 0) {
				setBackground(Color.WHITE);
			} else {
				setBackground(ThemeColor.LIGHT_GRAY);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			at = table.rowAtPoint(e.getPoint());
			table.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				int r = table.getSelectedRow();
				if (r == array.size() - 1) {
					return;
				}
				try {
					QueryConditions.getInstance().setName(table.getValueAt(r, 0).toString());
					f.updateLedger();
				} catch (SQLException e1) {
					MessageDialog.showError(this, "数据库访问错误");
					logger.error(LogHelper.exceptionToString(e1));
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
			at = -1;
			table.repaint();
		}
	};

	// 数据库
	private H2_DB h2 = null;
	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();
	// 父窗口
	private MainFrame f = null;

	public AccountsPanel(MainFrame frame) throws SQLException {
		logger.info("账户表格初始化 - 开始");
		// 布局管理
		setLayout(new BorderLayout());
		// 父窗口
		f = frame;
		// 更新列表和表格
		updateTable();
		// 表头设置
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.setFont(font.getFont(1));
		header.setBackground(ThemeColor.BLUE);
		header.setForeground(Color.WHITE);
		header.setPreferredSize(new Dimension(header.getWidth(), 30));
		// 行高
		table.setFont(font.getFont());
		table.setRowHeight(32);
		// 居中显示
		CellRenderer tcr = new CellRenderer();
		table.setDefaultRenderer(Object.class, tcr);
		// 颜色设置
		table.setSelectionBackground(ThemeColor.LIGHT_BLUE);
		table.setSelectionForeground(Color.WHITE);
		// 网格线
		table.setShowVerticalLines(false);
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		// 单行选择模式
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// 鼠标监听
		table.addMouseMotionListener(tcr);
		table.addMouseListener(tcr);
		// 滑动面板
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setOpaque(true);
		scrollPane.getViewport().setBackground(Color.WHITE);

		Border tb1 = BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "账户信息显示", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, font.getFont(0, 13f));
		scrollPane.setBorder(tb1);
		add(scrollPane, BorderLayout.CENTER);

		logger.info("账户表格初始化 - 完成");
	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
		logger.info("刷新账户表格");
		h2 = new H2_DB();
		String sql = "SELECT * FROM `accounts` ORDER BY `accounts`.`createtime` ASC;";
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		array.clear();
		while (rs.next()) {
			array.add(new AccountStructure(rs.getString("name"), rs.getString("createtime"), rs.getFloat("balance")));
		}
		h2.close();
		// 表格内容更新
		table.setModel(new AccountsModel(array));

		// 列宽设置
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(1).setMaxWidth(260);
		cm.getColumn(1).setMinWidth(240);
		cm.getColumn(0).setMinWidth(160);
		cm.getColumn(0).setMaxWidth(180);
	}

	/**
	 * @param name 账户名
	 * @throws SQLException
	 */
	private void insertAccount(String name) throws SQLException {
		logger.info("开始新建账户");
		String sql = String.format("INSERT INTO `accounts`(`name`) VALUES ('%s')", name);
		h2 = new H2_DB();
		logger.info(sql);
		h2.execute(sql);
		h2.close();
	}

	/**
	 * 检验当前账户名是否重复
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	private boolean isNameUnique(String name) throws SQLException {
		H2_DB h2 = new H2_DB();
		boolean result = h2.isUnique("accounts", "name", name);
		h2.close();
		return result;
	}

	/**
	 * 删除账户
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteAccount() throws SQLException {
		// 当前选中行
		int r = table.getSelectedRow();
		logger.info("删除当前选中账户，当前选中行为：" + r);
		// 开始判断
		if (r < 0) {
			return false;
		}
		// 判断是否存在关联记录决定是否可删除
		h2 = new H2_DB();
		boolean independent = h2.isUnique("ledger", "name", array.get(r).getName());
		h2.close();
		if (independent == false) {
			MessageDialog.showError(f, "当前账户存在关联记录，禁止删除");
			return false;
		}
		// 用户确认删除
		if (MessageDialog.showConfirm(f, "确认删除当前账户？\r\n注意仅在账户无关联流水，且余额清零时可删除！") == JOptionPane.YES_OPTION) {
			logger.info("确认删除流水记录");
			String sql = String.format("DELETE FROM `accounts` WHERE `name`='%s'", array.get(r).getName());
			logger.info(sql);
			h2 = new H2_DB();
			h2.execute(sql);
			h2.close();
			// 如果删除账户涉及当前筛选条件则改为全部
			if (QueryConditions.getInstance().getName().equals(array.get(r).getName())) {
				QueryConditions.getInstance().setName("%");
			}
			updateTable();
			return true;
		}
		logger.info("取消删除");
		return false;
	}
}

class AccountsModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2453177164571829047L;

	private static String[] title = { "账户名", "创建时间", "当前余额" };

	private ArrayList<AccountStructure> array = new ArrayList<>();

	public AccountsModel(ArrayList<AccountStructure> array) {
		this.array = array;
		float amount = 0;
		for (AccountStructure a : array) {
			amount += a.getAmount();
		}
		this.array.add(new AccountStructure("双击输入账户名", "--", amount));
	}

	@Override
	public String getColumnName(int column) {
		return title[column];
	}

	@Override
	public int getRowCount() {
		return array.size();
	}

	@Override
	public int getColumnCount() {
		return title.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object o = null;
		switch (columnIndex) {
		case 0:
			o = array.get(rowIndex).getName();
			break;
		case 1:
			o = array.get(rowIndex).getCreatetime();
			break;
		case 2:
			o = String.format("%.2f", array.get(rowIndex).getAmount());
			break;
		}
		return o;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			array.get(rowIndex).setName(aValue.toString());
		}
	}
}
