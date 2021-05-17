package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import design.DefaultFont;
import design.ThemeColor;
import dialogs.InfoDialog;
import dialogs.LabelsDialog;
import dialogs.MessageDialog;
import models.RecordStructure;
import panels.AccountsPanel;
import panels.LedgerPanel;

public class MainFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2630322136338626255L;

	// 菜单及菜单项
	private JMenu[] m = new JMenu[3];
	private JMenuItem[] mit = new JMenuItem[7];

	// 菜单及菜单项索引
	private static final int MENU_RECORD = 0, MENU_MANAGE = 1, MENU_HELP = 2;
	private static final int ITEM_LABEL = 0, ITEM_ACCOUNT = 1, ITEM_LEDGER = 2, ITEM_EXPORT = 3, ITEM_ABOUT = 4,
			ITEM_RECORD = 5, ITEM_TRANSFER = 6;

	// 字体
	private DefaultFont font = new DefaultFont();

	private Logger logger = LogManager.getLogger();

	// 面板
	private AccountsPanel accounts = null;
	private LedgerPanel ledgerPanel = null;

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
		String[] mstr = { "记账", " 管理 ", " 帮助 " };
		for (int i = 0; i < mstr.length; i++) {
			m[i] = new JMenu(mstr[i]);
			bar.add(m[i]);
			m[i].setUI(new DefaultMenuUI(ThemeColor.BLUE, Color.WHITE));
			m[i].setFont(font.getFont());
		}
		// 菜单项
		String[] istr = { " 标签设置 ", " 删除选中账户 ", " 删除选中记录 ", " 导出CSV ", " 关于 ", " 记一笔账 ", " 转账 " };
		for (int i = 0; i < istr.length; i++) {
			mit[i] = new JMenuItem(istr[i]);
			mit[i].setFont(font.getFont());
			mit[i].addActionListener(this);
			mit[i].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
			mit[i].setBackground(Color.WHITE);
		}
		m[MENU_RECORD].add(mit[ITEM_RECORD]);
		m[MENU_RECORD].add(mit[ITEM_TRANSFER]);
		m[MENU_MANAGE].add(mit[ITEM_LABEL]);
		m[MENU_MANAGE].add(mit[ITEM_ACCOUNT]);
		m[MENU_MANAGE].add(mit[ITEM_LEDGER]);
		m[MENU_MANAGE].add(mit[ITEM_EXPORT]);
		m[MENU_HELP].add(mit[ITEM_ABOUT]);

		// 账户
		accounts = new AccountsPanel(this);
		add(accounts, BorderLayout.SOUTH);
		// 账本
		ledgerPanel = new LedgerPanel(this);
		add(ledgerPanel, BorderLayout.CENTER);

		validate();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * 更新面板信息
	 * 
	 * @throws SQLException
	 */
	public void updatePanel() throws SQLException {
		accounts.updateTable();
		ledgerPanel.updateTable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mit[ITEM_ABOUT]) {
			// 关于
			MessageDialog.showMessage(this,
					"我的账本Ledger V2.0，由iamroot开发使用\r\n" + "时间：2021年5月12日\r\n" + "邮箱：cuigen@buaa.edu.cn");
		} else if (e.getSource() == mit[ITEM_LABEL]) {
			// 标签管理
			try {
				new LabelsDialog(this, getLocation(), getSize());
			} catch (SQLException e1) {
				e1.printStackTrace();
				MessageDialog.showError(this, "数据库错误");
				logger.error(e1);
			}
		} else if (e.getSource() == mit[ITEM_ACCOUNT]) {
			// 删除账户
			try {
				if (accounts.deleteAccount()) {
					updatePanel();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				MessageDialog.showError(this, "删除失败！");
			}
		} else if (e.getSource() == mit[ITEM_LEDGER]) {
			// 删除流水
			try {
				if (ledgerPanel.deleteLedger()) {
					updatePanel();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				MessageDialog.showError(this, "删除失败！");
			}
		} else if (e.getSource() == mit[ITEM_EXPORT]) {
			// 导出

		} else if (e.getSource() == mit[ITEM_RECORD]) {
			try {
				InfoDialog infoDialog = new InfoDialog(this, getLocation(), getSize(), null);
				if (infoDialog.showDialog()) {
					this.updatePanel();
				}
			} catch (SQLException e1) {
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
