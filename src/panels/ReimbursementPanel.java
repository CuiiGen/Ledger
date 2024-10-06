package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.DefaultMemuItemUI;
import design.ThemeColor;
import dialogs.MessageDialog;
import main.MainFrame;
import models.ReimbursementStructure;
import tool.LogHelper;
import tool.SystemProperties;

public class ReimbursementPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3735937482804924316L;
	// 数据
	private ArrayList<ReimbursementStructure> array = new ArrayList<>();
	// 显示表格
	private JTable table = new JTable(new ReimbursementModel(array)) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2669628508210252492L;

		public void editingStopped(ChangeEvent e) {
			int r = getEditingRow();
			int c = getEditingColumn();
			String id = table.getValueAt(r, 0).toString();
			String pre = table.getValueAt(r, c).toString();
			super.editingStopped(e);
			String newName = table.getValueAt(r, c).toString();
			// 如果未更改则直接退出后续处理
			if (pre.equals(newName)) {
				return;
			}
			logger.info("更新报销单备注");
			String sql = String.format("UPDATE `reimbursements` SET `name`='%s' WHERE `no`=%s", newName, id);
			try (H2_DB h2 = new H2_DB()) {
				logger.info(sql);
				h2.execute(sql);
				h2.close();
			} catch (SQLException e1) {
				logger.error(LogHelper.exceptionToString(e1));
				MessageDialog.showError(this, "数据库访问错误，报销名称修改失败！");
			}
		}
	};
	private JScrollPane scrollPane = new JScrollPane(table);

	// 弹出式菜单
	private JPopupMenu pop = new JPopupMenu();
	private JMenuItem[] items = new JMenuItem[2];
	private static final int ITEM_MARK_COMPLETE = 0, ITEM_DEL = 1;

	// 鼠标所在行
	int at = -1;

	// 内部类
	private class CellRenderer extends DefaultTableCellRenderer implements MouseMotionListener, MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1108124244265230115L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (array.get(row).isComplete()) {
				setForeground(Color.LIGHT_GRAY);
			} else {
				setForeground(Color.BLACK);
			}
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
			at = table.rowAtPoint(e.getPoint());
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
				table.setRowSelectionInterval(at, at);
				pop.show(e.getComponent(), e.getX(), e.getY());
			} else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				int r = table.getSelectedRow();
				try {
					QueryConditions.getInstance().setReimbursement(Integer.parseInt(table.getValueAt(r, 0).toString()));
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

	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();
	private JTextField name = new JTextField();
	private JButton btn = new JButton("新建报销单");
	// 父窗口
	private MainFrame f = null;

	public ReimbursementPanel(MainFrame frame) throws SQLException {
		logger.info("报销表格初始化 - 开始");
		f = frame;
		// 布局管理器
		setLayout(new BorderLayout());
		// 更新表格
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
		table.setRowHeight(SystemProperties.getInstance().getInt("theme.rowHeight"));
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
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setBackground(Color.WHITE);
		// 白色背景
		scrollPane.getViewport().setOpaque(true);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		// 新增按钮
		btn.setFont(font.getFont());
		btn.setForeground(Color.WHITE);
		btn.setBackground(ThemeColor.BLUE);
		btn.addActionListener(this);
		// 命名框
		name.setFont(font.getFont());
		name.setOpaque(true);
		name.setSelectedTextColor(Color.WHITE);
		name.setSelectionColor(ThemeColor.BLUE);
		name.setBackground(Color.WHITE);
		name.setHorizontalAlignment(JLabel.LEFT);
		// box layout
		Box hbox = Box.createHorizontalBox();
		hbox.add(btn);
		hbox.add(Box.createHorizontalStrut(15));
		hbox.add(name);
		add(hbox, BorderLayout.NORTH);

		// 弹出式菜单
		String[] istr = { " 修改完成状态 ", " 删除 " };
		for (int i = 0; i < istr.length; i++) {
			items[i] = new JMenuItem(istr[i]);
			items[i].setFont(font.getFont());
			items[i].addActionListener(this);
			items[i].setBackground(Color.WHITE);
			items[i].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
		}
		pop.add(items[ITEM_MARK_COMPLETE]);
		// 删除
		items[ITEM_DEL].setUI(new DefaultMemuItemUI(ThemeColor.RED, Color.WHITE));
		pop.add(items[ITEM_DEL]);

		logger.info("报销表格初始化 - 完成");
	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
		// 根据筛选条件生成SQL并查询
		String sql = "SELECT * FROM view_reimbursement";
		logger.info("报销表格更新");
		try (H2_DB h2 = new H2_DB()) {
			logger.info(sql);
			ResultSet rs = h2.query(sql);
			array.clear();
			while (rs.next()) {
				ReimbursementStructure st = new ReimbursementStructure();
				st.setNumber(rs.getInt("no"));
				st.setName(rs.getString("name"));
				st.setComplete(rs.getBoolean("complete"));
				st.setBalance(rs.getFloat("balance"));
				array.add(st);
			}
			h2.close();
		}
		// 表格内容更新
		table.setModel(new ReimbursementModel(array));
	}

	/**
	 * 删除当前选中记录
	 * 
	 * @return
	 * @throws SQLException
	 */
	private void deleteLedger() throws SQLException {
		// 当前选中行
		int r = table.getSelectedRow();
		logger.info("删除当前选中记录，当前选中行为：" + r);
		// 开始判断
		if (r < 0) {
			return;
		}
		if (MessageDialog.showConfirm(this, "确认删除当前报销记录？\r\n注意删除后无法复原！") == JOptionPane.YES_OPTION) {
			logger.info("确认删除报销记录");
			try (H2_DB h2 = new H2_DB()) {
				h2.setAutoCommit(false);
				// 恢复余额
				ReimbursementStructure st = array.get(r);
				String sql = String.format(
						"UPDATE `ledger` SET `reimbursement` = null WHERE `ledger`.`reimbursement` = %d;",
						st.getNumber());
				logger.info("恢复相关账户关联");
				logger.info(sql);
				h2.execute(sql);
				// 删除报销记录
				sql = String.format("DELETE FROM `reimbursements` WHERE `no`=%d;", st.getNumber());
				logger.info("删除报销记录");
				logger.info(sql);
				h2.execute(sql);
				h2.commit();
				h2.close();
			}
			return;
		} else {
			logger.info("取消删除");
			return;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == items[ITEM_DEL]) {
			// 删除报销
			logger.info("点击删除报销的菜单项，询问是否删除报销？");
			try {
				deleteLedger();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，删除报销单失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == items[ITEM_MARK_COMPLETE]) {
			// 修改状态
			try (H2_DB h2 = new H2_DB()) {
				String sql = String.format("UPDATE `reimbursements` SET `complete` = %s WHERE `no` = %d;",
						!array.get(table.getSelectedRow()).isComplete(), array.get(table.getSelectedRow()).getNumber());
				logger.info(sql);
				h2.execute(sql);
				h2.close();
			} catch (SQLException e1) {
				logger.error(LogHelper.exceptionToString(e1));
				MessageDialog.showError(this, "数据库访问错误，修改报销状态失败！");
			}
		} else if (e.getSource() == btn) {
			if (name.getText().isEmpty()) {
				return;
			}
			// 新建
			try (H2_DB h2 = new H2_DB()) {
				String sql = String.format("INSERT INTO `reimbursements` VALUES(DEFAULT, '%s', DEFAULT);",
						name.getText());
				logger.info(sql);
				h2.execute(sql);
				h2.close();
				name.setText(null);
			} catch (SQLException e1) {
				logger.error(LogHelper.exceptionToString(e1));
				MessageDialog.showError(this, "数据库访问错误，新增报销失败！");
			}
		}
		try {
			f.updateLedger();
		} catch (SQLException e1) {
			logger.error(LogHelper.exceptionToString(e1));
			MessageDialog.showError(this, "数据库访问错误，刷新报销失败！");
		}
	}
}

class ReimbursementModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2453177164571829047L;

	private static String[] title = { "#", "备注", "结余金额", "是否已经完成" };

	private ArrayList<ReimbursementStructure> array = new ArrayList<>();

	public ReimbursementModel(ArrayList<ReimbursementStructure> array) {
		this.array = array;
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
			o = String.valueOf(array.get(rowIndex).getNumber());
			break;
		case 1:
			o = array.get(rowIndex).getName();
			break;
		case 2:
			o = String.valueOf(array.get(rowIndex).getBalance());
			break;
		case 3:
			o = array.get(rowIndex).isComplete() ? "已完成" : "流程中";
			break;
		}
		return o;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 1) {
			array.get(rowIndex).setName(aValue.toString());
		}
	}

}
