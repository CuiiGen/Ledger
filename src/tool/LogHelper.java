package tool;

import java.util.ArrayList;

public class LogHelper {

	/**
	 * 将抛出的异常格式化输出
	 * 
	 * @param e Exception
	 * @return
	 */
	public static String exceptionToString(Exception e) {
		ArrayList<String> list = new ArrayList<>();
		list.add(e.toString());
		for (StackTraceElement element : e.getStackTrace()) {
			list.add(element.toString());
		}
		return String.join("\n\t", list);
	}
}
