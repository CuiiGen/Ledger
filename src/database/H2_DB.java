package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Script;

import dialogs.MessageDialog;

public class H2_DB {

	private static String url = "jdbc:h2:file:./database/Ledger";
	private static final String user = "root";
	private static final String pw = "sH6AkexU93exhBB";

	private Connection connection = null;
	private Statement statement = null;

	private Logger logger = LogManager.getLogger();

	public H2_DB() throws SQLException {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(url, user, pw);
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (ClassNotFoundException e) {
			// 大概率不会发生不进行抛出处理
			e.printStackTrace();
			logger.error(e);
			MessageDialog.showError(null, e.toString());
		}
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (connection != null && connection.isClosed() == false)
			connection.close();
		if (statement != null && statement.isClosed() == false)
			statement.close();
		connection = null;
		statement = null;
	}

	/**
	 * @param sql 查询语句
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet query(String sql) throws SQLException {
		ResultSet rs = statement.executeQuery(sql);
		return rs;
	}

	/**
	 * @param sql 增删改语句
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		statement.execute(sql);
	}

	/**
	 * 备份
	 * 
	 * @throws SQLException
	 */
	public static void backup() throws SQLException {
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		String filename = String.format("./database/backup_%s.zip", ft.format(Calendar.getInstance().getTime()));
		Script.process(url, user, pw, filename, "", "compression zip");
	}

	public static void restore() {

	}
}
