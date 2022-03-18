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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.DefaultMemuItemUI;
import design.ThemeColor;
import dialogs.InfoDialog;
import dialogs.MessageDialog;
import main.MainFrame;
import models.RecordStructure;
import tool.LogHelper;

public class LedgerPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4577861621524184964L;
	// 数据
	private ArrayList<RecordStructure> array = new ArrayList<>();
	// 显示表格
	private JTable table = new JTable(new RecordsModel(array));
	private JScrollPane scrollPane = new JScrollPane(table);

	// 弹出式菜单
	private JPopupMenu pop = new JPopupMenu();
	private JMenuItem[] items = new JMenuItem[2];
	private static final int ITEM_REFUND = 0, ITEM_DEL = 1;

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
			if (column < 6) {
				setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				setHorizontalAlignment(SwingConstants.LEFT);
			}
			if (array.get(row).getIsValid() == false) {
				setForeground(Color.LIGHT_GRAY);
			} else if (array.get(row).getType() == 1) {
				setForeground(ThemeColor.ORANGE);
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
			}
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				// 被选中的行
				int selectedRow = table.getSelectedRow();
				if (table.getSelectedColumn() > 0) {
					// 打开流水记录详情对话框
					logger.info("打开流水记录详情对话框");
					try {
						if (showInfoDialog(array.get(selectedRow), false)) {
							f.updateAllPanel();
							logger.info("有信息修改，刷新页面\n");
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						logger.info(LogHelper.exceptionToString(e1));
					}
				} else {
					// 修改isValid
					try {
						// 记录滚轴位置
						int scrollValue = scrollPane.getVerticalScrollBar().getValue();
						logger.info("修改有效性标记");
						h2 = new H2_DB();
						String t = "o";
						if (array.get(selectedRow).getIsValid()) {
							t = "i";
						}
						String sql = String.format("UPDATE ledger SET `isValid` = '%s' WHERE createtime = '%s'", t,
								array.get(selectedRow).getCreatetime());
						logger.info(sql);
						h2.execute(sql);
						h2.close();
						f.updateLedger();
						// 页面刷新后保持原视图
						scrollPane.getVerticalScrollBar().setValue(scrollValue);
						table.setRowSelectionInterval(selectedRow, selectedRow);
						// 日志
						logger.info("\n");
					} catch (SQLException e1) {
						e1.printStackTrace();
						logger.info(LogHelper.exceptionToString(e1));
						MessageDialog.showError(f, "编辑出错，数据库访问异常！");
					}
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
	// 数据库
	private H2_DB h2 = null;
	// 日志
	private Logger logger = LogManager.getLogger();
	// 父窗口
	private MainFrame f = null;
	private JLabel balence = new JLabel();

	public LedgerPanel(MainFrame frame) throws SQLException {
		logger.info("流水表格初始化 - 开始");
		// 布局管理器
		setLayout(new BorderLayout());
		// 父窗口
		f = frame;
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
		add(scrollPane, BorderLayout.CENTER);
		// 余额
		balence.setFont(font.getFont(2, 13f));
		balence.setOpaque(true);
		balence.setBackground(Color.WHITE);
		balence.setHorizontalAlignment(JLabel.RIGHT);
		add(balence, BorderLayout.NORTH);
		// 弹出式菜单
		String[] istr = { " 退款 ", " 删除 " };
		for (int i = 0; i < istr.length; i++) {
			items[i] = new JMenuItem(istr[i]);
			items[i].setFont(font.getFont(14f));
			items[i].addActionListener(this);
			items[i].setBackground(Color.WHITE);
		}
		// 退款
		items[ITEM_REFUND].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
		pop.add(items[ITEM_REFUND]);
		pop.addSeparator();
		// 删除
		items[ITEM_DEL].setUI(new DefaultMemuItemUI(ThemeColor.RED, Color.WHITE));
		pop.add(items[ITEM_DEL]);

		logger.info("流水表格初始化 - 完成");
	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
		// 根据筛选条件生成SQL并查询
		String sql = QueryConditions.getSQL();
		logger.info("流水表格更新");
		h2 = new H2_DB();
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		array.clear();
		while (rs.next()) {
			array.add(new RecordStructure(rs.getString("createtime"), rs.getString("name"),
					Integer.parseInt(rs.getString("type")), rs.getFloat("amount"), rs.getString("label"),
					rs.getString("remark"), rs.getString("isValid")));
		}
		h2.close();
		// 金额状态更新
		float in = 0, out = 0;
		for (RecordStructure r : array) {
			if (r.getIsValid()) {
				if (r.getType() == 1) {
					in += r.getAmount();
				} else {
					out += r.getAmount();
				}
			}
		}
		balence.setText(String.format("当前总收入金额￥%.2f，总支出金额￥%.2f    ", in, out));
		// 表格内容更新
		table.setModel(new RecordsModel(array));
		// 列宽设置
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setMaxWidth(30);
		cm.getColumn(0).setMinWidth(40);
		cm.getColumn(1).setMaxWidth(220);
		cm.getColumn(1).setMinWidth(200);
		cm.getColumn(2).setMaxWidth(180);
		cm.getColumn(2).setMinWidth(150);
		cm.getColumn(3).setMaxWidth(120);
		cm.getColumn(3).setMinWidth(100);
		cm.getColumn(4).setMaxWidth(120);
		cm.getColumn(4).setMinWidth(100);
		cm.getColumn(5).setMaxWidth(150);
		cm.getColumn(5).setMinWidth(130);

		scrollPane.getVerticalScrollBar().setValue(0);
	}

	/**
	 * 导出数据
	 * 
	 * @throws IOException
	 */
	public void export() throws IOException {
		ArrayList<String> list = new ArrayList<>();
		list.add("#,记账时间,相关账户,类型,金额,标签,备注");
		for (RecordStructure rds : array) {
			list.add(rds.toString());
		}
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File file = new File(fsv.getHomeDirectory().getAbsolutePath() + "\\流水.csv");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(String.join("\r\n", list).getBytes("GBK"));
		fos.flush();
		fos.close();
	}

	/**
	 * 删除当前选中记录
	 * 
	 * @return
	 * @throws SQLException
	 */
	private boolean deleteLedger() throws SQLException {
		// 当前选中行
		int r = table.getSelectedRow();
		logger.info("删除当前选中记录，当前选中行为：" + r);
		// 开始判断
		if (r < 0) {
			return false;
		}
		if (MessageDialog.showConfirm(f, "确认删除当前拉流水记录？\r\n注意删除后无法复原！") == JOptionPane.YES_OPTION) {
			logger.info("确认删除流水记录");
			h2 = new H2_DB();
			h2.setAutoCommit(false);
			// 恢复余额
			RecordStructure rds = array.get(r);
			String sql = String.format("UPDATE accounts SET balance = balance - %.2f WHERE accounts.`name` = '%s';",
					rds.getType() * rds.getAmount(), rds.getName());
			logger.info("恢复相关账户余额");
			logger.info(sql);
			h2.execute(sql);
			// 删除流水记录
			sql = String.format("DELETE FROM ledger WHERE createtime='%s'", rds.getCreatetime());
			logger.info("删除流水记录");
			logger.info(sql);
			h2.execute(sql);
			h2.commit();
			h2.close();
			return true;
		} else {
			logger.info("取消删除");
			return false;
		}
	}

	/**
	 * 打开流水记录编辑界面
	 * 
	 * @param rds
	 * @return
	 * @throws SQLException
	 */
	private boolean showInfoDialog(RecordStructure rds, boolean isRefund) throws SQLException {
		InfoDialog dialog = new InfoDialog(f, f.getLocation(), f.getSize(), rds, isRefund);
		return dialog.showDialog();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == items[ITEM_DEL]) {
			// 删除流水
			logger.info("点击删除流水的菜单项，询问是否删除流水？");
			try {
				if (deleteLedger()) {
					f.updateAllPanel();
					logger.info("已确认删除\n");
				} else {
					logger.info("已取消删除\n");
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，删除失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == items[ITEM_REFUND]) {
			// 退款
			try {
				RecordStructure r = array.get(table.getSelectedRow()).clone();
				r.reverseType();
				r.setLabel("退款");
				r.setRemark("退款");
				if (showInfoDialog(r, true)) {
					f.updateAllPanel();
					logger.info("已完成退款\n");
				} else {
					logger.info("取消退款\n");
				}
			} catch (SQLException e1) {
				logger.error(LogHelper.exceptionToString(e1));
				MessageDialog.showError(this, "数据库访问错误，退款失败！");
			} catch (CloneNotSupportedException e1) {
				logger.error(LogHelper.exceptionToString(e1));
				e1.printStackTrace();
			}
		}
	}
}

class RecordsModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2453177164571829047L;

	private static String[] title = { "#", "记账时间", "相关账户", "类型", "金额", "标签", "备注" };

	private ArrayList<RecordStructure> array = new ArrayList<>();

	public RecordsModel(ArrayList<RecordStructure> array) {
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
			o = array.get(rowIndex).getIsValid() ? "o" : "";
			break;
		case 1:
			o = array.get(rowIndex).getCreatetime();
			break;
		case 2:
			o = array.get(rowIndex).getName();
			break;
		case 3:
			o = array.get(rowIndex).getType() == -1 ? "支出" : "收入";
			break;
		case 4:
			o = array.get(rowIndex).getAmount();
			break;
		case 5:
			o = array.get(rowIndex).getLabel();
			break;
		case 6:
			o = array.get(rowIndex).getRemark();
			break;
		}
		return o;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
