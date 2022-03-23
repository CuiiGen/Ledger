package main;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.H2_DB;
import dialogs.MessageDialog;
import tool.LogHelper;

public class AppLauncher {

	public static void main(String[] args) {
		try {
			new MainFrame();
		} catch (SQLException e) {
			MessageDialog.showError(null, e.toString());
			Logger logger = LogManager.getLogger();
			logger.error(LogHelper.exceptionToString(e));
			MessageDialog.showError(null, "启动过程中数据库访问异常，需选择备份文件进行恢复！");
			if (H2_DB.restore()) {
				MessageDialog.showMessage(null, "恢复成功，请重新启动软件！");
			}
			System.exit(0);
		}
	}
}
