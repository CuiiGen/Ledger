package tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultProperties {

	private static String PROPERTIES_FILE_PATH = "ledger.properties";
	public static Properties p = new Properties();

	// Properties List
	// ledger.isValidShown
	// ledger.version

	public static void load() throws IOException {
		p.load(new FileInputStream(PROPERTIES_FILE_PATH));
	}

	public static void store() throws IOException {
		FileOutputStream dest = new FileOutputStream(PROPERTIES_FILE_PATH);
		p.store(dest, "The Properties File For Ledger.APP");
		dest.close();
	}
}
