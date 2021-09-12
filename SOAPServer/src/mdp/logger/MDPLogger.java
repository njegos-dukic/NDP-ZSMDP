package mdp.logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import mdp.soap.server.UserManagementService;

public class MDPLogger {

	public static final Logger logger = Logger.getLogger("MDP-Logger");
	private static FileHandler logFileHandler;

	public static String RESOURCES_ROOT = new UserManagementService().getClass().getClassLoader().getResource("../resources/").getPath().toString();
	public static String RESOURCES_FILENAME = "app.properties";

	private static Properties prop = new Properties();

	static {
		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES_ROOT + RESOURCES_FILENAME);
			prop.load(input);
			logFileHandler = new FileHandler(prop.getProperty("LOG_FILE"));
			logger.addHandler(logFileHandler);
		} catch (FileNotFoundException e1) {
			MDPLogger.log(Level.INFO, "Propperties file not found.");
		} catch (IOException e) {
			MDPLogger.log(Level.INFO, "Error opening properties file.");
		}
	}

	public static void log(Level lv, String error, Object e) {
		logger.log(lv, error, e);
	}

	public static void log(Level lv, String error) {
		logger.log(lv, error);
	}
}
