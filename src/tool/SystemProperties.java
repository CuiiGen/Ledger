package tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemProperties {

	// directory of property file
	private static final String PROPERTIES_FILE_PATH = "ledger.properties";
	// system properties from file
	private Properties fileProperties = new Properties();
	// default properties
	private Properties defaultProperties = new Properties();

	private Logger logger = LogManager.getLogger();

	// Properties List
	// ledger.onlyValid
	// backup.path
	// cryptography.algorithm
	private SystemProperties() {
		try {
			fileProperties.load(new FileInputStream(PROPERTIES_FILE_PATH));
			logger.info("已读取配置文件到软件中");
		} catch (FileNotFoundException e) {
			logger.error("文件不存在");
			logger.error(LogHelper.exceptionToString(e));
		} catch (IOException e) {
			logger.error("文件读取失败");
			logger.error(LogHelper.exceptionToString(e));
		}
		// default keys and values
		defaultProperties.setProperty("cryptography.algorithm", "SHA-512");
		defaultProperties.setProperty("backup.path", "./backup/");
		defaultProperties.setProperty("ledger.onlyValid", "false");
		if (fileProperties.isEmpty()) {
			fileProperties = (Properties) defaultProperties.clone();
		}
	}

	public String getString(String key) {
		// 日志
		String defaultReturn = defaultProperties.getProperty(key);
		logger.info(String.format("键：%s，默认值：%s", key, defaultReturn));
		// 获取对应值
		String property = fileProperties.getProperty(key);
		// 判断
		if (property != null) {
			return property;
		} else {
			logger.warn("键值对不存在");
			fileProperties.setProperty(key, defaultReturn);
			this.store();
			return defaultReturn;
		}
	}

	public boolean getBoolean(String key) {
		boolean bool = Boolean.parseBoolean(defaultProperties.getProperty(key));
		// 日志
		logger.info(String.format("键：%s，默认值：%s", key, bool));
		// 获取对应值
		String property = fileProperties.getProperty(key);
		// 判断键值是否存在
		if (property != null) {
			return Boolean.parseBoolean(property);
		} else {
			logger.warn("键值对不存在");
			// 重写键值对
			fileProperties.setProperty(key, String.valueOf(bool));
			this.store();
			return bool;
		}
	}

	public void setProperty(String key, String value) {
		logger.info(String.format("key: %s, modify value to %s.", key, value));
		fileProperties.setProperty(key, value);
		this.store();
	}

	/**
	 * Store the System properties to local file
	 */
	private void store() {
		try {
			FileWriter fw = new FileWriter(PROPERTIES_FILE_PATH, false);
			fileProperties.store(fw, "The Properties File For Ledger.APP");
			fw.close();
			logger.info("配置文件写入本地\n");
		} catch (IOException e) {
			logger.error("文件写入失败\n");
			logger.error(LogHelper.exceptionToString(e));
		}
	}

	private static volatile SystemProperties instance;

	public static SystemProperties getInstance() {
		if (instance == null) {
			synchronized (SystemProperties.class) {
				if (instance == null) {
					instance = new SystemProperties();
				}
			}
		}
		return instance;
	}

}
