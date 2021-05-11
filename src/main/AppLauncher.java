package main;

import java.sql.SQLException;

public class AppLauncher {

	public static void main(String[] args) {
		try {
			new MainFrame();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
