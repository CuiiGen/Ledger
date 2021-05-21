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
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.MessageDialog;
import main.MainFrame;
import models.AccountStructure;

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
			try {
				if (r == array.size() - 1) {
					// 新建
					if (table.getValueAt(r, c).equals("") || table.getValueAt(r, c).equals("点击新建账户")) {
					} else {
						insertAccount(table.getValueAt(r, c).toString());
					}
				} else {
					// 更新
					String sql = String.format("UPDATE `accounts` SET `name`='%s' WHERE `name`='%s'",
							table.getValueAt(r, c), pre);
					h2 = new H2_DB();
					logger.info(sql);
					h2.execute(sql);
					h2.close();
				}
				f.updatePanel();
			} catch (SQLException e1) {
				MessageDialog.showError(f, "数据库错误，注意账户名不可重复！");
				logger.error(e1);
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
				setBackground(ThemeColor.ORANGE);
			} else if (row % 2 == 0) {
				setBackground(ThemeColor.LIGHT_GRAY);
			} else {
				setBackground(Color.WHITE);
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
		// 布局管理
		setLayout(new BorderLayout());
		f = frame;
		// 更新列表和表格
		updateTable();
		// 表格设置
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(font.getFont(1));
		table.getTableHeader().setBackground(ThemeColor.BLUE);
		table.getTableHeader().setForeground(Color.WHITE);
		// 行高
		table.setFont(font.getFont());
		table.setRowHeight(27);
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

		Border tb1 = BorderFactory.createTitledBorder(new LineBorder(Color.DARK_GRAY), "账户信息显示", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, font.getFont());
		scrollPane.setBorder(tb1);
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.setBackground(Color.WHITE);

		validate();

	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
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
		cm.getColumn(1).setMaxWidth(240);
		cm.getColumn(1).setMinWidth(220);
		cm.getColumn(0).setMinWidth(120);
		cm.getColumn(0).setMaxWidth(160);
	}

	/**
	 * @param name 账户名
	 * @throws SQLException
	 */
	private void insertAccount(String name) throws SQLException {
		String sql = String.format("INSERT INTO `accounts`(`name`) VALUES ('%s')", name);
		h2 = new H2_DB();
		logger.info(sql);
		h2.execute(sql);
		h2.close();
	}

	/**
	 * 删除账户
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteAccount() throws SQLException {
		int r = table.getSelectedRow();
		if (r < 0) {
			return false;
		}
		if (array.get(r).getAmount() != 0) {
			MessageDialog.showError(f, "账户余额清零前禁止删除！");
			return false;
		}
		if (MessageDialog.showConfirm(f, "确认删除当前账户？\r\n注意仅在账户无关联流水，即余额清零时可删除！") == JOptionPane.YES_OPTION) {
			String sql = String.format("DELETE FROM `accounts` WHERE `name`='%s'", array.get(r).getName());
			h2 = new H2_DB();
			logger.info(sql);
			h2.execute(sql);
			h2.close();
			updateTable();
			return true;
		}
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
		this.array.add(new AccountStructure("点击新建账户", "--", amount));
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
			o = array.get(rowIndex).getAmount();
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

class DefaultMemuItemUI extends BasicMenuItemUI {
	public DefaultMemuItemUI(Color bgColor, Color fgColor) {
		super.selectionBackground = bgColor;
		super.selectionForeground = fgColor;
	}
}

class DefaultMenuUI extends BasicMenuUI {
	public DefaultMenuUI(Color bgColor, Color fgColor) {
		super.selectionBackground = bgColor;
		super.selectionForeground = fgColor;
	}
}
