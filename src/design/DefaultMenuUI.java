package design;

import java.awt.Color;

import javax.swing.plaf.basic.BasicMenuUI;

public class DefaultMenuUI extends BasicMenuUI {
	public DefaultMenuUI(Color bgColor, Color fgColor) {
		super.selectionBackground = bgColor;
		super.selectionForeground = fgColor;
	}
}
