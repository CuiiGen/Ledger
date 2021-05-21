package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.InfoDialog;
import dialogs.MessageDialog;
import main.MainFrame;
import models.RecordStructure;

public class LedgerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4577861621524184964L;
	private ArrayList<RecordStructure> array = new ArrayList<>();
	private JTable table = new JTable(new RecordsModel(array));

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
			int TYPE_COLUMN = 2;
			if (table.getValueAt(row, TYPE_COLUMN).equals("收入")) {
				setForeground(ThemeColor.ORANGE);
			} else {
				setForeground(Color.BLACK);
			}
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
			if (e.getClickCount() == 2) {
				try {
					if (showInfoDialog(array.get(table.getSelectedRow()))) {
						f.updatePanel();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
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

	private H2_DB h2 = null;

	private Logger logger = LogManager.getLogger();

	private MainFrame f = null;

	public LedgerPanel(MainFrame frame) throws SQLException {
		setLayout(new BorderLayout());
		f = frame;
		// 更新表格
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
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
		h2 = new H2_DB();
		String sql = "SELECT * FROM ledger ORDER BY createtime DESC";
		logger.info(sql);
		ResultSet rs = h2.query(sql);
		array.clear();
		while (rs.next()) {
			array.add(new RecordStructure(rs.getString("createtime"), rs.getString("name"),
					Integer.parseInt(rs.getString("type")), rs.getFloat("amount"), rs.getString("label"),
					rs.getString("remark")));
		}
		h2.close();
		// 表格内容更新
		table.setModel(new RecordsModel(array));
	}

	/**
	 * 删除当前选中记录
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteLedger() throws SQLException {
		int r = table.getSelectedRow();
		if (r < 0) {
			return false;
		}
		if (MessageDialog.showConfirm(f, "确认删除当前拉流水记录？\r\n注意删除后无法复原！") == JOptionPane.YES_OPTION) {
			h2 = new H2_DB();
			logger.info("删除流水记录");
			// 恢复余额
			RecordStructure rds = array.get(r);
			String sql = String.format("UPDATE accounts SET balance = balance - %.2f WHERE accounts.`name` = '%s';",
					rds.getType() * rds.getAmount(), rds.getName());
			logger.info(sql);
			h2.execute(sql);
			// 删除流水记录
			sql = String.format("DELETE FROM ledger WHERE createtime='%s'", rds.getCreatetime());
			logger.info(sql);
			h2.execute(sql);
			h2.close();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 导出数据
	 * 
	 * @throws IOException
	 */
	public void export() throws IOException {
		ArrayList<String> list = new ArrayList<>();
		list.add("记账时间,相关账户,类型,金额,标签,备注");
		for (RecordStructure rds : array) {
			list.add(rds.toString());
		}
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File file = new File(fsv.getHomeDirectory().getAbsolutePath() + "\\流水.csv");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(String.join("\r\n", list).getBytes());
		fos.flush();
		fos.close();
		MessageDialog.showMessage(this, "成功导出至桌面下“流水.csv”！");
	}

	/**
	 * @param rds
	 * @return
	 * @throws SQLException
	 */
	private boolean showInfoDialog(RecordStructure rds) throws SQLException {
		InfoDialog dialog = new InfoDialog(f, f.getLocation(), f.getSize(), rds);
		return dialog.showDialog();
	}
}

class RecordsModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2453177164571829047L;

	private static String[] title = { "记账时间", "相关账户", "类型", "金额", "标签", "备注" };

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
			o = array.get(rowIndex).getCreatetime();
			break;
		case 1:
			o = array.get(rowIndex).getName();
			break;
		case 2:
			o = array.get(rowIndex).getType() == -1 ? "支出" : "收入";
			break;
		case 3:
			o = array.get(rowIndex).getAmount();
			break;
		case 4:
			o = array.get(rowIndex).getLabel();
			break;
		case 5:
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
