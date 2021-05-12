package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.AccountsDialog;
import dialogs.LabelsDialog;
import dialogs.MessageDialog;
import models.RecordStructure;
import panels.InfoPanel;

public class MainFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2630322136338626255L;

	// 菜单及菜单项
	private JMenu[] m = new JMenu[2];
	private JMenuItem[] mit = new JMenuItem[4];

	// 菜单及菜单项索引
	private static final int MENU_MANAGE = 0, MENU_HELP = 1;
	private static final int ITEM_LABEL = 0, ITEM_ACCOUNT = 1, ITEM_EXPORT = 2, ITEM_ABOUT = 3;

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
			if (e.getClickCount() == 1) {
				try {
					infoPanel.contentReset(array.get(table.getSelectedRow()));
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

	// 详细信息显示面板
	private InfoPanel infoPanel = null;

	private H2_DB h2 = null;

	private Logger logger = LogManager.getLogger();

	public MainFrame() throws SQLException {

		super("我的账本Ledger");

		// 设置窗口显示在正中央占比0.7
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		int x = (int) (width * 0.15);
		int y = (int) (height * 0.15);
		setBounds(x, y, (int) (width * 0.7), (int) (height * 0.7));

		setLayout(new BorderLayout());

		// TODO 程序小图标
		setIconImage(getToolkit().getImage("./icon/FileHub.png"));

		// 菜单栏
		JMenuBar bar = new JMenuBar();
		add(bar, BorderLayout.NORTH);
		bar.setBackground(Color.WHITE);
		// 菜单
		String[] mstr = { " 管理 ", " 帮助 " };
		for (int i = 0; i < mstr.length; i++) {
			m[i] = new JMenu(mstr[i]);
			bar.add(m[i]);
			m[i].setUI(new DefaultMenuUI(ThemeColor.BLUE, Color.WHITE));
			m[i].setFont(font.getFont());
		}
		// 菜单项
		String[] istr = { " 标签 ", " 账户 ", " 导出CSV ", " 关于 " };
		for (int i = 0; i < istr.length; i++) {
			mit[i] = new JMenuItem(istr[i]);
			mit[i].setFont(font.getFont());
			mit[i].addActionListener(this);
			mit[i].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
			mit[i].setBackground(Color.WHITE);
		}
		m[MENU_MANAGE].add(mit[ITEM_LABEL]);
		m[MENU_MANAGE].add(mit[ITEM_ACCOUNT]);
		m[MENU_MANAGE].add(mit[ITEM_EXPORT]);
		m[MENU_HELP].add(mit[ITEM_ABOUT]);

		// 更新表格
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
		scrollPane.setBounds(50, 50, 600, 250);
		add(scrollPane, BorderLayout.CENTER);

		// 信息面板
		infoPanel = new InfoPanel(this);
		add(infoPanel, BorderLayout.SOUTH);

		validate();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * 更新表格
	 * 
	 * @throws SQLException
	 */
	public void updateTable() throws SQLException {
		h2 = new H2_DB();
		String sql = "SELECT * FROM ledger ORDER BY createtime DESC";
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mit[ITEM_ABOUT]) {
			// 关于
			MessageDialog.showMessage(this,
					"我的账本Ledger V1.0，由iamroot开发使用\r\n" + "时间：2021年2月20日\r\n" + "邮箱：cuigen@buaa.edu.cn");
		} else if (e.getSource() == mit[ITEM_LABEL]) {
			// 标签管理
			try {
				new LabelsDialog(this, getLocation(), getSize());
				infoPanel.contentReset(null);
			} catch (SQLException e1) {
				e1.printStackTrace();
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == mit[ITEM_ACCOUNT]) {
			// 账户管理
			try {
				new AccountsDialog(this, getLocation(), getSize());
				infoPanel.contentReset(null);
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == mit[ITEM_EXPORT]) {
			ArrayList<String> list = new ArrayList<>();
			list.add("记账时间,相关账户,类型,金额,标签,备注");
			for (RecordStructure rds : array) {
				list.add(rds.toString());
			}
			try {
				FileSystemView fsv = FileSystemView.getFileSystemView();
				File file = new File(fsv.getHomeDirectory().getAbsolutePath() + "\\流水.csv");
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(String.join("\r\n", list).getBytes());
				fos.flush();
				fos.close();
				MessageDialog.showMessage(this, "导出成功！");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
