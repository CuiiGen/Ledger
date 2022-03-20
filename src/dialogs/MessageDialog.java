package dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import design.DefaultFont;
import design.ThemeColor;

public class MessageDialog {

	private static DefaultFont font = new DefaultFont();

	public static void showError(final Component p, final String msg) {
		UIManager.put("OptionPane.buttonFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.messageFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.background", ThemeColor.APPLE);
		UIManager.put("Panel.background", ThemeColor.APPLE);
		JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showMessage(final Component p, final String msg) {
		UIManager.put("OptionPane.buttonFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.messageFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.background", ThemeColor.APPLE);
		UIManager.put("Panel.background", ThemeColor.APPLE);
		JOptionPane.showMessageDialog(p, msg, "通知", JOptionPane.INFORMATION_MESSAGE);

	}

	public static int showConfirm(final Component p, final String msg) {
		UIManager.put("OptionPane.buttonFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.messageFont", new FontUIResource(font.getFont()));
		UIManager.put("OptionPane.background", ThemeColor.APPLE);
		UIManager.put("Panel.background", ThemeColor.APPLE);
		return JOptionPane.showConfirmDialog(p, msg, "请确认", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

}
