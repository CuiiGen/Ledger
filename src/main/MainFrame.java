package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import design.DefaultFont;
import design.DefaultMemuItemUI;
import design.DefaultMenuUI;
import design.ThemeColor;
import dialogs.InfoDialog;
import dialogs.LabelsDialog;
import dialogs.MessageDialog;
import dialogs.TransferDialog;
import panels.AccountsPanel;
import panels.LedgerPanel;
import panels.PiePanel;
import panels.PlotPanel;
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
	private JMenuItem[] mit = new JMenuItem[10];

	// 菜单及菜单项索引
	private static final int MENU_RECORD = 0, MENU_MANAGE = 1, MENU_HELP = 2;
	private static final int ITEM_LABEL = 0, ITEM_ACCOUNT = 1, ITEM_CHECK = 2, ITEM_EXPORT = 3, ITEM_ABOUT = 4,
			ITEM_RECORD = 5, ITEM_TRANSFER = 6, ITEM_BACKUP = 7, ITEM_RESTORE = 8, ITEM_LOG = 9;

	// 字体
	private DefaultFont font = new DefaultFont();
	// 日志
	private Logger logger = LogManager.getLogger();

	// 面板
	private AccountsPanel accounts = null;
	private LedgerPanel ledgerPanel = null;
	private SortPanel sortPanel = null;
	// 折线图
	private PlotPanel monthlyCost = null;
	private PiePanel piePanel = null;

	public MainFrame() throws SQLException {

		super("我的账本Ledger");
		logger.info("主界面初始化 - 开始");

		// 设置窗口显示在正中央占比0.7
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		int x = (int) (width * 0.15);
		int y = (int) (height * 0.15);
		setBounds(x, y, (int) (width * 0.7), (int) (height * 0.7));

		setLayout(new BorderLayout());

		// 程序小图标
		setIconImage(getToolkit().getImage("./icon/Ledger.png"));

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
		String[] istr = { " 标签设置 ", " 删除选中账户 ", " 对账 ", " 导出CSV ", " 关于 ", " 记一笔账 ", " 转账 ", " 备份 ", " 恢复 ", " 查看日志 " };
		for (int i = 0; i < istr.length; i++) {
			mit[i] = new JMenuItem(istr[i]);
			mit[i].setFont(font.getFont(14f));
			mit[i].addActionListener(this);
			mit[i].setUI(new DefaultMemuItemUI(ThemeColor.BLUE, Color.WHITE));
			mit[i].setBackground(Color.WHITE);
		}

		// Ctrl + N
		mit[ITEM_RECORD].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		// Ctrl + T
		mit[ITEM_TRANSFER].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		// Ctrl + B
		mit[ITEM_BACKUP].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		// Ctrl + R
		mit[ITEM_RESTORE].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		// Ctrl + E
		mit[ITEM_EXPORT].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

		m[MENU_RECORD].add(mit[ITEM_RECORD]);
		m[MENU_RECORD].add(mit[ITEM_TRANSFER]);
		m[MENU_MANAGE].add(mit[ITEM_LABEL]);
		m[MENU_MANAGE].add(mit[ITEM_ACCOUNT]);
		m[MENU_MANAGE].addSeparator();
		m[MENU_MANAGE].add(mit[ITEM_EXPORT]);
		m[MENU_MANAGE].add(mit[ITEM_BACKUP]);
		m[MENU_MANAGE].add(mit[ITEM_RESTORE]);
		m[MENU_MANAGE].addSeparator();
		m[MENU_MANAGE].add(mit[ITEM_CHECK]);

		m[MENU_HELP].add(mit[ITEM_LOG]);
		m[MENU_HELP].add(mit[ITEM_ABOUT]);

		// 筛选
		sortPanel = new SortPanel(this);
		add(sortPanel, BorderLayout.WEST);

		// 临时面板辅助布局使用
		JPanel temtPanel = new JPanel(new BorderLayout());
		add(temtPanel, BorderLayout.CENTER);
		// 标签面板
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabPane.setFont(font.getFont(1, 12f));
		tabPane.setOpaque(true);
		tabPane.setBackground(Color.WHITE);
		UIManager.put("TabbedPane.borderHightlightColor", Color.WHITE);
		UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
		UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
		temtPanel.add(tabPane, BorderLayout.CENTER);
		// 账户
		accounts = new AccountsPanel(this);
		accounts.setPreferredSize(new Dimension(0, 350));
		temtPanel.add(accounts, BorderLayout.SOUTH);
		// 账本
		ledgerPanel = new LedgerPanel(this);
		tabPane.addTab("流水表格", ledgerPanel);
		piePanel = new PiePanel();
		tabPane.add("支出情况统计", piePanel);
		monthlyCost = new PlotPanel();
		tabPane.addTab("每月支出情况", monthlyCost);

		// 显示窗口
		validate();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		logger.info("主界面初始化 - 完成\n");
	}

	/**
	 * 更新表格及折线图的信息
	 * 
	 * @throws SQLException
	 */
	public void updateAllPanel() throws SQLException {
		logger.info("主界面中账户和流水表更新，以及折线图更新");
		// 账户
		accounts.updateTable();
		// 流水记录相关
		updateLedger();
	}

	/**
	 * 更新流水记录表及折线图
	 * 
	 * @throws SQLException
	 */
	public void updateLedger() throws SQLException {
		logger.info("主界面中流水表和折线图更新");
		// 流水
		ledgerPanel.updateTable();
		// 折线图
		piePanel.updatePlot();
		monthlyCost.updatePlot();
	}

	/**
	 * 校验账本是否平账
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String checkLedger() throws SQLException {
		H2_DB h2 = new H2_DB();
		String sql = "SELECT `accounts`.*, `check`.`amount` FROM `accounts` LEFT JOIN "
				+ "( SELECT `name`, SUM(`amount` * CAST(`type` AS CHAR)) AS `amount` FROM `ledger` GROUP BY `name` ) AS `check` "
				+ "ON `check`.`name` = `accounts`.`name`"
				+ "WHERE ABS(`check`.`amount` - `accounts`.`balance`) > 1E-7 OR `check`.`amount` IS NULL;";
		ResultSet rs = h2.query(sql);
		ArrayList<String> err = new ArrayList<>();
		while (rs.next()) {
			String e = String.format("%s：%3.2f，应为%3.2f", rs.getString(1), rs.getDouble(3), rs.getDouble(4));
			logger.error(e);
			err.add(e);
		}
		h2.close();
		return String.join("\n", err);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mit[ITEM_ABOUT]) {
			// 关于
			MessageDialog.showMessage(this, "我的账本Ledger V3.7.1，由iamroot开发\r\n时间：2022年3月18日\r\n邮箱：cuigen@buaa.edu.cn");
		} else if (e.getSource() == mit[ITEM_LABEL]) {
			// 标签管理
			logger.info("打开标签管理对话框");
			try {
				new LabelsDialog(this, getLocation(), getSize());
				sortPanel.updateContent();
				// 更新表格及折线图
				updateLedger();
				logger.info("关闭对话框，刷新界面\n");
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问错误");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_ACCOUNT]) {
			// 删除账户
			logger.info("点击删除账户的菜单项，询问是否删除账户？");
			try {
				if (accounts.deleteAccount()) {
					updateLedger();
					logger.info("已确认删除\n");
				} else {
					logger.info("已取消删除\n");
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
				logger.info("导出成功\n");
				MessageDialog.showMessage(this, "成功导出至桌面下“流水.csv”！");
			} catch (IOException e1) {
				MessageDialog.showError(this, "导出失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_RECORD]) {
			// 记一笔账
			logger.info("打开记账对话框");
			try {
				InfoDialog infoDialog = new InfoDialog(this, getLocation(), getSize(), null, false);
				if (infoDialog.showDialog()) {
					// 界面更新
					updateAllPanel();
					logger.info("记账成功\n");
				} else {
					logger.info("记账取消\n");
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
					// 界面更新
					updateAllPanel();
					logger.info("转账成功\n");
				} else {
					logger.info("取消转账\n");
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
				logger.info("数据库备份成功\n");
			} catch (SQLException e1) {
				MessageDialog.showError(this, "数据库访问失败！");
				logger.error(LogHelper.exceptionToString(e1));
			} catch (IOException e1) {
				MessageDialog.showError(this, "备份文件写入失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_RESTORE]) {
			// 恢复
			// 数据库文件重命名备份
			File file = new File("./database/Ledger.mv.db"), distFile = new File("./database/Ledger_old.mv.db"),
					temp = new File("./database/Ledger_temp.mv.db");
			try {
				logger.info("准备恢复数据库，首先保存原数据库为temp.mv.db");
				if (temp.exists() == false || temp.delete()) {
					file.renameTo(temp);
				} else {
					logger.error("文件读写失败，恢复过程取消，数据库未变动！");
					MessageDialog.showError(this, "文件读写失败，恢复过程取消，数据库未变动！");
					return;
				}
				// 恢复数据
				logger.info("即将选择SQL文件进行恢复");
				H2_DB.restore();
				// 判断是否恢复
				if (file.exists()) {
					// 删除临时数据库
					logger.info("Ledger.mv.db存在，待刷新页面");
					distFile.delete();
					temp.renameTo(distFile);
					MessageDialog.showMessage(this, "数据库恢复成功，原数据库文件重命名为“Ledger_old.mv.db”！");
					// 筛选条件重置
					QueryConditions.init();
					// 更新页面
					updateAllPanel();
					sortPanel.updateContent();
					MessageDialog.showMessage(this, "页面刷新完成！");
					logger.info("页面刷新成功\n");
				} else {
					temp.renameTo(file);
					logger.info("Ledger.mv.db存在不存在，复原旧数据库\n");
					MessageDialog.showMessage(this, "数据库未恢复，复原旧数据库");
				}
			} catch (SQLException e1) {
				// 日志
				logger.error(LogHelper.exceptionToString(e1));
				// 删除文件
				if (file.exists() == false || file.delete()) {
					temp.renameTo(file);
					logger.info("恢复过程出错，复原旧数据库\n");
					MessageDialog.showError(this, "恢复过程出错，恢复旧数据库！");
				} else {
					logger.info("恢复过程出错，复原旧数据库出错\n");
					MessageDialog.showError(this, "恢复过程出错，恢复旧数据库出错，需手动恢复！");
				}
			}
		} else if (e.getSource() == mit[ITEM_LOG]) {
			try {
				logger.info("查看日志\n");
				File file = new File("./database/Ledger.trace.db");
				if (file.exists()) {
					Runtime.getRuntime().exec("notepad ./database/Ledger.trace.db");
				}
				Runtime.getRuntime().exec("notepad ./logs/info.log");
			} catch (IOException e1) {
				logger.error(LogHelper.exceptionToString(e1));
			}
		} else if (e.getSource() == mit[ITEM_CHECK]) {
			logger.info("开始进行对账……");
			try {
				String msg = checkLedger();
				if (msg.isEmpty()) {
					logger.info("账本无问题\n");
					MessageDialog.showMessage(this, "账本无问题！");
				} else {
					logger.error(msg);
					MessageDialog.showError(this, msg);
				}
			} catch (SQLException e1) {
				logger.error("数据库访问失败！\n");
				MessageDialog.showError(this, "数据库访问失败！");
				logger.error(LogHelper.exceptionToString(e1));
			}
		}
	}
}
