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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.ThemeColor;
import design.DefaultFont;
import main.MainFrame;
import models.LabelStructure;
import panels.QueryConditions;
import tool.LogHelper;

public class LabelsDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6063182420495622194L;

	// 表格及数据列表
	private ArrayList<LabelStructure> array = new ArrayList<>();
	private JTable table = new JTable(new LabelsModel(array)) {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8938369871665016790L;

		public void editingStopped(ChangeEvent e) {
			int r = getEditingRow();
			int c = getEditingColumn();
			// 旧值
			String pre = table.getValueAt(r, c).toString();
			super.editingStopped(e);
			if (pre.equals("退款") || pre.equals("转账")) {
				table.setValueAt(pre, r, 0);
				MessageDialog.showError(this, "默认标签，禁止修改！");
				return;
			}
			if (QueryConditions.getLabel().equals(pre)) {
				QueryConditions.setLabel(table.getValueAt(r, c).toString());
			}
			try {
				h2 = new H2_DB();
				String sql = "";
				if (isTagUnique(table.getValueAt(r, c).toString())) {
					// 修改后标签无重复
					sql = String.format("UPDATE `labels` SET `label`='%s' WHERE `label`='%s'", table.getValueAt(r, c),
							pre);
					logger.info("标签不重复");
				} else {
					// 修改后标签重复
					if (MessageDialog.showConfirm(this, "修改后标签重复，是否整合两重复标签？") == JOptionPane.NO_OPTION) {
						return;
					}
					sql = String.format(
							"UPDATE `ledger` SET `label`='%1$s' WHERE `label`='%2$s';DELETE FROM `labels` WHERE `label`='%2$s'",
							table.getValueAt(r, c), pre);
					logger.info("标签重复");
				}
				logger.info(sql);
				h2.execute(sql);
				h2.close();
				updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(LogHelper.exceptionToString(e1));
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
	// 父窗口
	private MainFrame f = null;

	public LabelsDialog(final MainFrame frame, final Point p, final Dimension d) throws SQLException {
		// 父类构造函数
		super(frame, "标签管理", true);
		f = frame;
		// 布局管理
		setLayout(null);
		setResizable(false);
		// 窗口位置显示
		final int w = 700, h = 420;
		setBounds(p.x + (d.width - w) / 2, p.y + (d.height - h) / 2, w, h);
		// 更新列表和表格
		updateTable();
		// 表格设置
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.setFont(font.getFont(1));
		header.setBackground(ThemeColor.BLUE);
		header.setForeground(Color.WHITE);
		header.setPreferredSize(new Dimension(header.getWidth(), 30));
		// 行高
		table.setFont(font.getFont());
		table.setRowHeight(30);
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
		add(scrollPane);
		scrollPane.setBounds(50, 25, 600, 250);

		// 输入框设置
		tx.setFont(font.getFont());
		tx.setSelectedTextColor(Color.WHITE);
		tx.setSelectionColor(ThemeColor.BLUE);
		add(tx);
		// 标签
		JLabel label = new JLabel("新建标签名");
		label.setFont(font.getFont());
		add(label);
		label.setBounds(50, 290, 110, 30);
		tx.setBounds(170, 290, 200, 30);
		// 按键初始化
		btn[BUTTON_INSERT] = new JButton("添加");
		btn[BUTTON_DEL] = new JButton("删除");
		btn[BUTTON_CLOSE] = new JButton("关闭");
		for (final JButton b : btn) {
			b.setFont(font.getFont(1));
			b.setForeground(Color.WHITE);
			b.setBackground(Color.DARK_GRAY);
			b.addActionListener(this);
			add(b);
		}
		btn[BUTTON_INSERT].setBackground(ThemeColor.BLUE);
		btn[BUTTON_INSERT].setForeground(Color.WHITE);
		btn[BUTTON_INSERT].setBounds(150, 340, 100, 30);
		btn[BUTTON_DEL].setBounds(300, 340, 100, 30);
		btn[BUTTON_CLOSE].setBounds(450, 340, 100, 30);

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
		logger.info("刷新标签表格");
		h2 = new H2_DB();
		String sql = "SELECT * FROM `view_labels`";
		ResultSet rs = h2.query(sql);
		logger.info(sql);
		array.clear();
		while (rs.next()) {
			array.add(new LabelStructure(rs.getString("label"), rs.getString("createtime"), rs.getFloat("amount"),
					rs.getInt("count")));
		}
		h2.close();
		// 表格内容更新
		table.setModel(new LabelsModel(array));

		// 列宽设置
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(1).setMaxWidth(240);
		cm.getColumn(1).setMinWidth(220);
		cm.getColumn(0).setMinWidth(120);
		cm.getColumn(0).setMaxWidth(160);

	}

	/**
	 * 插入标签
	 * 
	 * @throws SQLException
	 */
	private void insertLabel() throws SQLException {
		logger.info("开始新建标签");
		if (tx.getText().isEmpty()) {
			logger.error("标签为空");
		} else if (isTagUnique(tx.getText())) {
			String sql = String.format("INSERT INTO `labels`(`label`) VALUES ('%s')", tx.getText());
			tx.setText(null);
			h2 = new H2_DB();
			logger.info(sql);
			h2.execute(sql);
			h2.close();
		} else {
			MessageDialog.showError(this, "标签重复");
		}
	}

	/**
	 * 删除标签
	 * 
	 * @return
	 * @throws SQLException
	 */
	private boolean deleteLabel() throws SQLException {
		// 当前选中行
		int r = table.getSelectedRow();
		logger.info("删除当前选中标签，当前选中行为：" + r);
		// 未选中
		if (r < 0) {
			return false;
		}
		// 默认标签不删除
		String l = array.get(r).getLabel();
		if (l.equals("退款") || l.equals("转账")) {
			logger.error("默认标签，禁止删除！");
			MessageDialog.showError(this, "默认标签，禁止删除！");
			return false;
		}
		if (array.get(r).getAmount() != 0) {
			MessageDialog.showError(f, "标签存在对应记录，禁止删除！");
			return false;
		}
		// 确认删除
		if (MessageDialog.showConfirm(this, "确认删除当前标签？") == JOptionPane.YES_OPTION) {
			logger.info("确认删除标签");
			String sql = String.format("DELETE FROM `labels` WHERE `label`='%s'", array.get(r).getLabel());
			h2 = new H2_DB();
			logger.info(sql);
			h2.execute(sql);
			h2.close();
			if (QueryConditions.getLabel().equals(l)) {
				QueryConditions.setLabel("全部");
			}
			return true;
		} else {
			logger.info("取消删除");
			return false;
		}
	}

	/**
	 * 检验当前标签是否重复
	 * 
	 * @param tag 标签
	 * @return
	 * @throws SQLException
	 */
	private static boolean isTagUnique(String tag) throws SQLException {
		String sql = String.format("SELECT * FROM `labels` WHERE `label`='%s'", tag);
		// 静态类定义对象
		H2_DB h2 = new H2_DB();
		Logger logger = LogManager.getLogger();
		// 日志输出
		logger.info(sql);
		// 执行
		h2.execute(sql);
		// 结果
		ResultSet rs = h2.query(sql);
		rs.last();
		int row = rs.getRow();
		h2.close();
		return row == 0;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == btn[BUTTON_CLOSE]) {
			// 关闭窗口
			dispose();
		} else if (e.getSource() == btn[BUTTON_INSERT]) {
			// 插入
			try {
				insertLabel();
				updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "或重复插入，插入失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == btn[BUTTON_DEL]) {
			// 删除
			try {
				if (deleteLabel()) {
					logger.info("已删除，刷新表格");
					updateTable();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问失败，删除失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		}
	}
}

class LabelsModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2453177164571829047L;

	private static String[] title = { "标签名", "创建时间", "累计金额", "计数（笔）" };

	private ArrayList<LabelStructure> array = new ArrayList<>();

	public LabelsModel(ArrayList<LabelStructure> array) {
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
			o = array.get(rowIndex).getLabel();
			break;
		case 1:
			o = array.get(rowIndex).getCreatetime();
			break;
		case 2:
			o = array.get(rowIndex).getAmount();
			break;
		case 3:
			o = array.get(rowIndex).getCount();
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
			array.get(rowIndex).setLabel(aValue.toString());
		}
	}

}