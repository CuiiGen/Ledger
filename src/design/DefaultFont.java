package design;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dialogs.MessageDialog;
import tool.LogHelper;

public class DefaultFont {

	// 默认不加粗不斜体
	private static final int fontDefaultStyle = 0;
	// 默认15号大小
	private static final float fontDefaultSize = 15;
	// 字体文件是否存在
	private static boolean isFontFileExists = true;

	private Font font = new Font("默认字体", 0, (int) fontDefaultSize);

	/**
	 * 构造函数
	 */
	public DefaultFont() {
		if (isFontFileExists) {
			try {
				font = Font.createFont(Font.TRUETYPE_FONT, new File("./font/SourceHanSerifCN-Medium.otf"));
			} catch (FontFormatException | IOException e) {
				isFontFileExists = false;
				Logger logger = LogManager.getLogger();
				logger.error("字体文件加载错误！");
				logger.error(LogHelper.exceptionToString(e));
				MessageDialog.showError(null, "自定义字体文件加载失败，恢复默认字体！");
			}
		}
	}

	/**
	 * @param style 字形
	 * @param size  字号
	 * @return
	 */
	public Font getFont(int style, float size) {
		return font.deriveFont(style, size);
	}

	/**
	 * 
	 * @return
	 */
	public Font getFont() {
		return font.deriveFont(fontDefaultStyle, fontDefaultSize);
	}

	/**
	 * 
	 * @param style
	 * @return
	 */
	public Font getFont(int style) {
		return font.deriveFont(style, fontDefaultSize);

	}

	/**
	 * 
	 * @param size
	 * @return
	 */
	public Font getFont(float size) {
		return font.deriveFont(fontDefaultStyle, size);
	}

}
