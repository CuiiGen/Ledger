package design;

import java.awt.Color;

import javax.swing.plaf.basic.BasicMenuItemUI;

public class DefaultMemuItemUI extends BasicMenuItemUI {
	public DefaultMemuItemUI(Color bgColor, Color fgColor) {
		super.selectionBackground = bgColor;
		super.selectionForeground = fgColor;
	}
}
