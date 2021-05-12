package dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.ThemeColor;
import design.DefaultFont;
import main.MainFrame;
import models.AccountStructure;

public class AccountsDialog extends JDialog implements ActionListener {

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
			String sql = String.format("UPDATE `accounts` SET `name`='%s' WHERE `name`='%s'", table.getValueAt(r, c),
					pre);
			try {
				h2 = new H2_DB();
				logger.info(sql);
				h2.execute(sql);
				h2.close();
				updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		};
	};

	// 内部类
	private class CellRenderer extends DefaultTableCellRenderer implements MouseMotionListener, MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1108124244265230115L;

		// 鼠标所在行
		int at = -1;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setHorizontalAlignment(SwingConstants.CENTER);
			if (at == row) {
				setBackground(ThemeColor.LIGHT_BLUE);
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

	// 输入扩展名及备注
	private JTextField tx = new JTextField();
	// 三个按键
	private JButton[] btn = new JButton[3];
	private static final int BUTTON_INSERT = 0, BUTTON_DEL = 1, BUTTON_CLOSE = 2;
	// 数据库
	private H2_DB h2 = null;
	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();

	public AccountsDialog(final MainFrame frame, final Point p, final Dimension d) throws SQLException {
		// 父类构造函数
		super(frame, "账户管理", true);
		// 布局管理
		setLayout(null);
		setResizable(false);
		// 窗口位置显示
		final int w = 700, h = 500;
		setBounds(p.x + (d.width - w) / 2, p.y + (d.height - h) / 2, w, h);
		// 更新列表和表格
		updateTable();
		// 表格设置
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(font.getFont());
		table.getTableHeader().setBackground(Color.WHITE);
		// 行高
		table.setFont(font.getFont());
		table.setRowHeight(27);
		// 居中显示
		CellRenderer tcr = new CellRenderer();
		table.setDefaultRenderer(Object.class, tcr);
		// 颜色设置
		table.setSelectionBackground(ThemeColor.BLUE);
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
		add(scrollPane);
		scrollPane.setBounds(50, 50, 600, 250);

		// 输入框设置
		tx.setFont(font.getFont());
		tx.setSelectedTextColor(Color.WHITE);
		tx.setSelectionColor(ThemeColor.BLUE);
		add(tx);
		// 标签
		JLabel label = new JLabel("新建账户名");
		label.setFont(font.getFont());
		add(label);
		label.setBounds(50, 330, 110, 30);
		tx.setBounds(170, 330, 200, 30);
		// 按键初始化
		btn[BUTTON_INSERT] = new JButton("添加");
		btn[BUTTON_DEL] = new JButton("删除");
		btn[BUTTON_CLOSE] = new JButton("关闭");
		for (final JButton b : btn) {
			b.setFont(font.getFont());
			b.setForeground(Color.DARK_GRAY);
			b.setBackground(Color.LIGHT_GRAY);
			b.addActionListener(this);
			add(b);
		}
		btn[BUTTON_INSERT].setBackground(ThemeColor.BLUE);
		btn[BUTTON_INSERT].setForeground(Color.WHITE);
		btn[BUTTON_INSERT].setBounds(150, 400, 100, 30);
		btn[BUTTON_DEL].setBounds(300, 400, 100, 30);
		btn[BUTTON_CLOSE].setBounds(450, 400, 100, 30);

		// 窗口显示
		validate();

		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	private void updateTable() throws SQLException {
		h2 = new H2_DB();
		String sql = "SELECT * FROM `accounts` ORDER BY `accounts`.`createtime` DESC;";
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

	private void insertAccount() throws SQLException {
		if (tx.getText().isEmpty()) {
		} else {
			String sql = String.format("INSERT INTO `accounts`(`name`) VALUES ('%s')", tx.getText());
			tx.setText(null);
			h2 = new H2_DB();
			logger.info(sql);
			h2.execute(sql);
			h2.close();
		}
	}

	private void deleteAccount() throws SQLException {
		int r = table.getSelectedRow();
		if (r > -1) {
			String sql = String.format("DELETE FROM `accounts` WHERE `name`='%s'", array.get(r).getName());
			h2 = new H2_DB();
			logger.info(sql);
			h2.execute(sql);
			h2.close();
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == btn[BUTTON_CLOSE]) {
			dispose();
		} else if (e.getSource() == btn[BUTTON_INSERT]) {
			try {
				insertAccount();
				updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "或重复插入，插入失败！");
				logger.error(e);
			}
		} else if (e.getSource() == btn[BUTTON_DEL]) {
			try {
				deleteAccount();
				updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "或有关联流水，删除失败！");
				logger.error(e);
			}
		}
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