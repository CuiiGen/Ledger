package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import tool.DefaultProperties;
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
	 * 设置事务是否自动提交
	 * 
	 * 对于多表操作应关闭自动提交
	 * 
	 * @param isAutoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean isAutoCommit) throws SQLException {
		connection.setAutoCommit(isAutoCommit);
	}

	/**
	 * 提交操作
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		connection.commit();
	}

	/**
	 * 操作回滚，实际使用过程中关闭连接自动回滚取消提交
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		connection.rollback();
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
	 * 判断表中某一字段是否存在与待检验字符串的重复值
	 * 
	 * @param table 表名
	 * @param key   字段名
	 * @param value 待检验字符串
	 * @throws SQLException
	 */
	public boolean isUnique(String table, String key, String value) throws SQLException {
		String sql = String.format("SELECT * FROM `%s` WHERE `%s`='%s'", table, key, value);
		// 日志输出
		logger.info(sql);
		// 执行
		execute(sql);
		// 结果
		ResultSet rs = query(sql);
		rs.last();
		// 获得所有行
		int row = rs.getRow();
		return row == 0;
	}

	/**
	 * 备份
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void backup() throws SQLException, IOException {
		// 日志管理
		Logger logger = LogManager.getLogger();
		// 时间格式化
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		// 文件名格式化
		String filename = String.format("./backup/backup_%s", ft.format(Calendar.getInstance().getTime()));
		File sqlFile = new File(filename + ".sql");
		// 导出文件
		Script.process(url, user, pw, filename + ".sql", "", "");
		logger.info("SQL文件导出成功");

		// 计算校验文件
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			FileInputStream fis = new FileInputStream(sqlFile);
			byte[] buffer = new byte[8192];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				md.update(buffer, 0, len);
			}
			fis.close();
			String sha = String.format("%032x %s", new BigInteger(1, md.digest()), sqlFile.getName());
			FileOutputStream fos = new FileOutputStream(filename + ".sql.sha512");
			fos.write(sha.getBytes());
			fos.flush();
			fos.close();
			logger.info(sha);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// 加密压缩
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		ZipFile zipFile = new ZipFile(filename + ".zip", pw.toCharArray());
		zipFile.addFile(sqlFile, zipParameters);
		zipFile.close();
//		数据备份后暂时不删除原SQL文件
//		sqlFile.delete();
		logger.info("文件完成压缩");
	}

	/**
	 * 恢复备份
	 * 
	 * @throws SQLException
	 */
	public static boolean restore() {
		// 数据库文件重命名备份
		File file = new File("./database/Ledger.mv.db"), distFile = new File("./database/Ledger_old.mv.db"),
				temp = new File("./database/Ledger_temp.mv.db");
		Logger logger = LogManager.getLogger();
		try {
			// 恢复前准备
			logger.info("准备恢复数据库，首先保存原数据库为temp.mv.db");
			if (temp.exists() == false || temp.delete()) {
				file.renameTo(temp);
			} else {
				logger.error("文件读写失败，恢复过程取消，数据库未变动！");
				MessageDialog.showError(null, "文件读写失败，恢复过程取消，数据库未变动！");
				return false;
			}
			// 恢复数据
			logger.info("即将选择SQL文件进行恢复");
			String filePath = DefaultProperties.p.getProperty("backup.path");
			if (filePath == null) {
				filePath = "./backup/";
			}
			File sqlFile = FileChooserDialog.openFileChooser(null, filePath);
			if (sqlFile != null) {
				DefaultProperties.p.setProperty("backup.path", sqlFile.getParent());
				logger.info("待恢复文件路径：" + sqlFile.getAbsolutePath());
				RunScript.execute(url, user, pw, sqlFile.getAbsolutePath(), Charset.forName("GBK"), false);
				checkUsers();
				logger.info("Ledger.mv.db存在，待后续刷新页面");
				distFile.delete();
				temp.renameTo(distFile);
				MessageDialog.showMessage(null, "数据库恢复成功，原数据库文件重命名为“Ledger_old.mv.db”！");
				return true;
			} else {
				DefaultProperties.p.setProperty("backup.path", filePath);
				logger.info("未选中任何SQL文件，待恢复为原数据库");
				temp.renameTo(file);
				MessageDialog.showMessage(null, "数据库未恢复，复原旧数据库");
				return false;
			}
		} catch (SQLException e1) {
			// 日志
			logger.error(LogHelper.exceptionToString(e1));
			// 删除文件
			if (file.exists() == false || file.delete()) {
				logger.debug(temp.renameTo(file));
				logger.info("恢复过程出错，复原旧数据库\n");
				MessageDialog.showError(null, "恢复过程出错，恢复旧数据库！");
			} else {
				logger.info("恢复过程出错，复原旧数据库出错\n");
				MessageDialog.showError(null, "恢复过程出错，恢复旧数据库出错，需手动恢复！");
			}
			return false;
		}
	}

	/**
	 * 检查用户
	 * 
	 * @throws SQLException
	 */
	private static void checkUsers() throws SQLException {
		H2_DB h2 = new H2_DB();
		String sql = "SELECT * FROM INFORMATION_SCHEMA.USERS";
		LogManager.getLogger().info(sql);
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
