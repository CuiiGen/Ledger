package main;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		}
	}

}
