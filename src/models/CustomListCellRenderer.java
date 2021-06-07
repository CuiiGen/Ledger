package models;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import design.ThemeColor;

public class CustomListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -268019275375633761L;
	//
	private DefaultListCellRenderer defaultCellRenderer = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		JLabel label = (JLabel) defaultCellRenderer.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if (isSelected) {
			// 选中状态
			label.setForeground(Color.WHITE);
			label.setBackground(ThemeColor.BLUE);
		} else {
			// 非选中状态
			label.setForeground(Color.BLACK);
			label.setBackground(Color.WHITE);
		}
		// 选择结束状态
		list.setSelectionForeground(Color.WHITE);
		list.setSelectionBackground(ThemeColor.BLUE);

		return label;

	}
}
