package tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultProperties {

	private static String PROPERTIES_FILE_PATH = "ledger.properties";
	private static Logger logger = LogManager.getLogger();

	// Properties List
	// ledger.onlyValid
	// backup.path
	// cryptography.algorithm

	/**
	 * 加载配置文件
	 * 
	 * @return
	 */
	private static Properties load() {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(PROPERTIES_FILE_PATH));
			logger.info("已读取配置文件到软件中");
		} catch (FileNotFoundException e) {
			logger.error("文件不存在");
			logger.error(LogHelper.exceptionToString(e));
		} catch (IOException e) {
			logger.error("文件读取失败");
			logger.error(LogHelper.exceptionToString(e));
		}
		return p;
	}

	/**
	 * @param key           键
	 * @param defaultReturn 如果对应值不存在返回的默认值
	 * @return
	 */
	public static String getProperty(String key, String defaultReturn) {
		Properties p = load();
		// 日志
		logger.info(String.format("键：%s，默认值：%s", key, defaultReturn));
		// 获取对应值
		String property = p.getProperty(key);
		// 判断
		if (property != null) {
			return property;
		} else {
			logger.warn("键值对不存在");
			p.setProperty(key, defaultReturn);
			store(p);
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
	public static boolean getProperty(String key, boolean defaultReturn) {
		Properties p = load();
		// 日志
		logger.info(String.format("键：%s，默认值：%s", key, defaultReturn));
		// 获取对应值
		String property = p.getProperty(key);
		// 保存结果
		boolean bool = defaultReturn;
		// 判断键值是否存在
		if (property != null) {
			bool = Boolean.parseBoolean(property);
		} else {
			logger.warn("键值对不存在");
		}
		// 重写键值对
		p.setProperty(key, String.valueOf(bool));
		store(p);
		return bool;
	}

	/**
	 * 设置并写入配置文件
	 * 
	 * @param key
	 * @param value
	 */
	public static void setProperty(String key, String value) {
		Properties p = load();
		p.setProperty(key, value);
		store(p);
	}

	/**
	 * @param p
	 * @throws IOException
	 */
	private static void store(Properties p) {
		try {
			FileWriter fw = new FileWriter(PROPERTIES_FILE_PATH, false);
			p.store(fw, "The Properties File For Ledger.APP");
			fw.close();
			logger.info("配置文件写入本地\n");
		} catch (IOException e) {
			logger.error("文件写入失败\n");
			logger.error(LogHelper.exceptionToString(e));
		}
	}
}
