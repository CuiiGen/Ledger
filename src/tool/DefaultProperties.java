package tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultProperties {
	// directory of property file
	private static String PROPERTIES_FILE_PATH = "ledger.properties";
	// store default properties
	private static Properties defaultProperties = new Properties();
	// system properties from file
	public static Properties SystemProperties = new Properties();

	private static Logger logger = LogManager.getLogger();

	// Properties List
	// ledger.onlyValid
	// backup.path
	// cryptography.algorithm
	public DefaultProperties() {
		try {
			SystemProperties.load(new FileInputStream(PROPERTIES_FILE_PATH));
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
		if (SystemProperties.isEmpty()) {
			SystemProperties = (Properties) defaultProperties.clone();
		}
	}

	/**
	 * @param key           键
	 * @param defaultReturn 如果对应值不存在返回的默认值
	 * @return
	 */
	public static String getString(String key) {
		// 日志
		String defaultReturn = defaultProperties.getProperty(key);
		logger.info(String.format("键：%s，默认值：%s", key, defaultReturn));
		// 获取对应值
		String property = SystemProperties.getProperty(key);
		// 判断
		if (property != null) {
			return property;
		} else {
			logger.warn("键值对不存在");
			SystemProperties.setProperty(key, defaultReturn);
			store();
			return defaultReturn;
		}
	}

	/**
	 * 获取配置文件中的boolean项
	 * 
	 * @param key           键
	 * @param defaultReturn 如果对应值不存在时返回的默认值
	 * @return
	 */
	public static boolean getBoolean(String key) {
		boolean bool = Boolean.parseBoolean(defaultProperties.getProperty(key));
		// 日志
		logger.info(String.format("键：%s，默认值：%s", key, bool));
		// 获取对应值
		String property = SystemProperties.getProperty(key);
		// 判断键值是否存在
		if (property != null) {
			return Boolean.parseBoolean(property);
		} else {
			logger.warn("键值对不存在");
			// 重写键值对
			SystemProperties.setProperty(key, String.valueOf(bool));
			store();
			return bool;
		}
	}

	/**
	 * 设置并写入配置文件
	 * 
	 * @param key
	 * @param value
	 */
	public static void setProperty(String key, String value) {
		logger.info(String.format("key: %s, modify value to %s.", key, value));
		SystemProperties.setProperty(key, value);
		store();
	}

	/**
	 * @param p
	 * @throws IOException
	 */
	private static void store() {
		try {
			FileWriter fw = new FileWriter(PROPERTIES_FILE_PATH, false);
			SystemProperties.store(fw, "The Properties File For Ledger.APP");
			fw.close();
			logger.info("配置文件写入本地\n");
		} catch (IOException e) {
			logger.error("文件写入失败\n");
			logger.error(LogHelper.exceptionToString(e));
		}
	}
}
