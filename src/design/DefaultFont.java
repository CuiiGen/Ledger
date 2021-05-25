package design;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

public class DefaultFont {

	private Font font = null;

	private static final int fontDefaultStyle = 0;
	private static final float fontDefaultSize = 15;

	/**
	 * 构造函数
	 */
	public DefaultFont() {
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File("./font/SourceHanSerifCN-Medium.otf"));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
