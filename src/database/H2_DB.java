package database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.RunScript;
import org.h2.tools.Script;

import dialogs.FileChooserDialog;
import dialogs.MessageDialog;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import tool.LogHelper;

public class H2_DB {

	private static final String url = "jdbc:h2:file:./database/Ledger";
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
			logger.error(LogHelper.exceptionToString(e));
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
	 * @throws IOException
	 */
	public static void backup() throws SQLException, IOException {
		// 时间格式化
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		// 文件名格式化
		String filename = String.format("./backup/backup_%s", ft.format(Calendar.getInstance().getTime()));
		// 导出文件
		Script.process(url, user, pw, filename + ".sql", "", "");
		// 加密压缩
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		ZipFile zipFile = new ZipFile(filename + ".zip", pw.toCharArray());
		zipFile.addFile(filename, zipParameters);
		zipFile.close();
	}

	/**
	 * 恢复备份
	 * 
	 * @throws SQLException
	 */
	public static void restore() throws SQLException {
		File file = FileChooserDialog.openFileChooser(null);
		if (file != null) {
			LogManager.getLogger().info("待恢复文件路径：" + file.getAbsolutePath());
			RunScript.execute(url, user, pw, file.getAbsolutePath(), Charset.forName("GBK"), false);
			checkusers();
		}
	}

	/**
	 * 检查用户
	 * 
	 * @throws SQLException
	 */
	public static void checkusers() throws SQLException {
		H2_DB h2 = new H2_DB();
		String sql = "SELECT * FROM INFORMATION_SCHEMA.USERS";
		ResultSet rs = h2.query(sql);
		// 用户名列表
		ArrayList<String> list = new ArrayList<>();
		while (rs.next()) {
			list.add(rs.getString(1));
		}
		for (String username : list) {
			if (user.equals(username.toLowerCase())) {
				continue;
			} else {
				LogManager.getLogger().info("删除用户：" + username);
				h2.execute(String.format("DROP USER IF EXISTS %s", username));
			}
		}
		h2.close();
	}
}
