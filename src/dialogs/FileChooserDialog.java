package dialogs;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import design.DefaultFont;

/**
 * @author CuiGen 文件及路径选择对话框
 */
public class FileChooserDialog {

	private static final LookAndFeel original = UIManager.getLookAndFeel();
	private static final String system = UIManager.getSystemLookAndFeelClassName();
	// 字体
	private static DefaultFont font = new DefaultFont();

	/**
	 * 打开文件的文件选择器
	 * 
	 * @param parent
	 * @return
	 */
	public static File openFileChooser(Component parent) {
		try {
			UIManager.setLookAndFeel(system);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// 桌面
		JFileChooser fileChooser = new JFileChooser("./database/");
		fileChooser.setFont(font.getFont());
		// 打开
		fileChooser.setApproveButtonText("上传");
		// 文件过滤器
		fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP压缩文件", "sql"));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// 待返回文件
		File f = null;
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			f = fileChooser.getSelectedFile();
		}
		try {
			UIManager.setLookAndFeel(original);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		return f != null && f.exists() ? f : null;
	}
}
