package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.ThemeColor;
import dialogs.InfoDialog;
import dialogs.LabelsDialog;
import dialogs.MessageDialog;
import dialogs.TransferDialog;
import models.RecordStructure;
import panels.AccountsPanel;
import panels.LedgerPanel;
import panels.QueryConditions;
import panels.SortPanel;
import tool.LogHelper;

public class MainFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2630322136338626255L;

	// 菜单及菜单项
	private JMenu[] m = new JMenu[3];
	private JMenuItem[] mit = new JMenuItem[9];

	// 菜单及菜单项索引
	private static final int MENU_RECORD = 0, MENU_MANAGE = 1, MENU_HELP = 2;
	private static final int ITEM_LABEL = 0, ITEM_ACCOUNT = 1, ITEM_LEDGER = 2, ITEM_EXPORT = 3, ITEM_ABOUT = 4,
			ITEM_RECORD = 5, ITEM_TRANSFER = 6, ITEM_BACKUP = 7, ITEM_RESTORE = 8;

	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();

	// 面板
	private AccountsPanel accounts = null;
	private LedgerPanel ledgerPanel = null;
	private SortPanel sortPanel = null;

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
			m[i].setFont(font.getFont(14f));
		}
		// 菜单项
		String[] istr = { " 标签设置 ", " 删除选中账户 ", " 删除选中记录 ", " 导出CSV ", " 关于 ", " 记一笔账 ", " 转账 ", " 备份 ", " 恢复 " };
		for (int i = 0; i < istr.length; i++) {
			mit[i] = new JMenuItem(istr[i]);
			mit[i].setFont(font.getFont(14f));
			mit[i].addActionListener(this);
			mit[i].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
			mit[i].setBackground(Color.WHITE);
		}

		m[MENU_RECORD].add(mit[ITEM_RECORD]);
		m[MENU_RECORD].add(mit[ITEM_TRANSFER]);
		m[MENU_MANAGE].add(mit[ITEM_LABEL]);
		m[MENU_MANAGE].add(mit[ITEM_ACCOUNT]);
		m[MENU_MANAGE].add(mit[ITEM_LEDGER]);
		m[MENU_MANAGE].addSeparator();
		m[MENU_MANAGE].add(mit[ITEM_EXPORT]);
		m[MENU_MANAGE].add(mit[ITEM_BACKUP]);
		m[MENU_MANAGE].add(mit[ITEM_RESTORE]);

		m[MENU_HELP].add(mit[ITEM_ABOUT]);

		JPanel temtPanel = new JPanel(new BorderLayout());
		add(temtPanel, BorderLayout.CENTER);
		QueryConditions.init();
		// 账户
		accounts = new AccountsPanel(this);
		accounts.setPreferredSize(new Dimension(0, 350));
		temtPanel.add(accounts, BorderLayout.SOUTH);
		// 账本
		ledgerPanel = new LedgerPanel(this);
		temtPanel.add(ledgerPanel, BorderLayout.CENTER);
		// 筛选
		sortPanel = new SortPanel(this);
		add(sortPanel, BorderLayout.WEST);

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
		logger.info("主界面中更新面板内容");
		accounts.updateTable();
		ledgerPanel.updateTable();
	}

	/**
	 * 筛选流水
	 * 
	 * @throws SQLException
	 */
	public void updateLedger() throws SQLException {
		ledgerPanel.updateTable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mit[ITEM_ABOUT]) {
			// 关于
			MessageDialog.showMessage(this,
					"我的账本Ledger V3.5.0，由iamroot开发使用\r\n" + "时间：2021年8月26日\r\n" + "邮箱：cuigen@buaa.edu.cn");
		} else if (e.getSource() == mit[ITEM_LABEL]) {
			// 标签管理
			logger.info("打开标签管理对话框");
			try {
				new LabelsDialog(this, getLocation(), getSize());
				sortPanel.updateContent();
				ledgerPanel.updateTable();
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_ACCOUNT]) {
			// 删除账户
			logger.info("是否删除账户");
			try {
				if (accounts.deleteAccount()) {
					logger.info("已确认删除");
					updatePanel();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，删除失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_LEDGER]) {
			// 删除流水
			logger.info("是否删除流水");
			try {
				if (ledgerPanel.deleteLedger()) {
					logger.info("已确认删除");
					updatePanel();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，删除失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_EXPORT]) {
			// 导出
			logger.info("导出数据至CSV中");
			try {
				ledgerPanel.export();
				logger.info("导出成功");
				MessageDialog.showMessage(this, "成功导出至桌面下“流水.csv”！");
			} catch (IOException e1) {
				MessageDialog.showError(this, "导出失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_RECORD]) {
			// 记一笔账
			logger.info("打开记账对话框，未输出成功便为未记账");
			try {
				InfoDialog infoDialog = new InfoDialog(this, getLocation(), getSize(), null);
				if (infoDialog.showDialog()) {
					logger.info("记账成功");
					this.updatePanel();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，记录失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_TRANSFER]) {
			// 转账
			logger.info("打开转账对话框，未输出成功便为未转账");
			try {
				TransferDialog transferDialog = new TransferDialog(this, getLocation(), getSize());
				if (transferDialog.showDialog()) {
					logger.info("转账成功");
					this.updatePanel();
				}
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误，记录失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_BACKUP]) {
			// 备份
			try {
				H2_DB.backup();
				MessageDialog.showMessage(this, "备份成功！");
				logger.info("备份成功");
			} catch (SQLException e1) {
				MessageDialog.showError(this, "备份失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_RESTORE]) {
			// 恢复
			// 数据库文件重命名备份
			File file = new File("./database/Ledger.mv.db"), distFile = new File("./database/Ledger_old.mv.db"),
					temp = new File("./database/Ledger_temp.mv.db");
			try {
				temp.delete();
				file.renameTo(temp);
				// 恢复数据
				H2_DB.restore();
				// 判断是否恢复
				if (file.exists()) {
					// 删除临时数据库
					logger.info("数据库已恢复");
					MessageDialog.showMessage(this, "数据库恢复成功，原数据库文件重命名为“Ledger_old.mv.db”！");
					distFile.delete();
					temp.renameTo(distFile);
					// 更新页面
					QueryConditions.init();
					updatePanel();
					sortPanel.updateContent();
					MessageDialog.showMessage(this, "页面刷新完成！");
				} else {
					temp.renameTo(file);
					logger.info("数据库未恢复，复原旧数据库");
				}
			} catch (SQLException e1) {
				// 日志
				logger.error(LogHelper.exceptionToString(e1));
				// 删除文件
				file.delete();
				temp.renameTo(file);
				MessageDialog.showError(this, "页面刷新失败，恢复旧数据库！");
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
