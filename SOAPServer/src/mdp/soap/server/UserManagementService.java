package mdp.soap.server;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import mdp.logger.MDPLogger;

public class UserManagementService {
	String USERS_FILE = "";
	XMLEncoder encoder = null;
	XMLDecoder decoder = null;
	HashMap<String, User> users = new HashMap<>();

	public String RESOURCES_ROOT = getClass().getClassLoader().getResource("../resources/").getPath().toString();
	public static String RESOURCES_FILENAME = "app.properties";

	public UserManagementService() {

		Properties prop = new Properties();

		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES_ROOT + RESOURCES_FILENAME);
			prop.load(input);
		} catch (FileNotFoundException e1) {
			MDPLogger.log(Level.INFO, "Propperties file not found.");
		} catch (IOException e) {
			MDPLogger.log(Level.INFO, "Error opening properties file.");
		}

		USERS_FILE = prop.getProperty("USERS_FILE");

		Path path = Paths.get(USERS_FILE);
		if (!Files.exists(path))
			try {
				Files.createFile(path);
			} catch (IOException e) {
				MDPLogger.log(Level.WARNING, "Error creating users.xml file.");
			}

		deserializeUsers();
	}

	public boolean addUser(String username, String password, int idZS, String stationName) {
		if (users.containsKey(username) || "".equals(password))
			return false;

		users.put(username, new User(username, digestString(password), idZS, stationName));
		serializeUsers();
		return true;
	}

	public Users getAllUsers() {
		deserializeUsers();
		return Util.getAllUsers(users);
	}

	public int[] getAllZsmdpIds() {
		deserializeUsers();
		return Util.getDistinct(users);
	}

	public SerializableStation[] getAllStations() {
		return Util.getAllStations(users);
	}

	public boolean checkCredentials(String username, String password) {
		deserializeUsers();

		if (username == null || password == null || users.get(username) == null || users.get(username).getIsOnline())
			return false;

		boolean areValid = users.containsKey(username) && users.get(username).getPassword().equals(password);
		if (areValid)
			users.get(username).setIsOnline(true);

		serializeUsers();
		return areValid;
	}

	public int getPort(String username) {
		return users.get(username).getPort();
	}

	public int getIdZsmdp(String username) {
		return users.get(username).getIdZS();
	}

	public void logOutUser(String username) {
		System.out.println("Logging out " + username + "...");
		deserializeUsers();
		users.get(username).setIsOnline(false);
		serializeUsers();
	}

	public String[] getAllActiveUsers(int zsmdpId) {
		return Util.getActiveUsers(users, zsmdpId);
	}

	public String[] getAllActiveUsersByStationName(String name) {
		return getAllActiveUsers(Util.getStationIdByName(users, name));
	}

	public void setOnline(String username) {
		users.get(username).setIsOnline(true);
		serializeUsers();
	}

	public boolean isOnline(String username) {
		deserializeUsers();
		return users.get(username).getIsOnline();
	}

	public boolean removeUser(String username) {
		deserializeUsers();
		users.remove(username);
		serializeUsers();
		return true;
	}

	public SerializableStation getByIdZSMDP(int id) {
		return Util.getSerializableStationByZSMDPId(users, id);
	}

	private String digestString(String input) {
		StringBuilder hexString = new StringBuilder("");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

			hexString = new StringBuilder(2 * encodedhash.length);
			for (byte element : encodedhash) {
				String hex = Integer.toHexString(0xff & element);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
		} catch (NoSuchAlgorithmException e) {
			MDPLogger.log(Level.WARNING, "No such algorithm while digesting password.");
		}

		return hexString.toString();
	}

	private void serializeUsers() {
		try {
			Files.delete(Paths.get(USERS_FILE));

			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(USERS_FILE)));

			encoder.writeObject(users);
			encoder.flush();
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error serializing users.");
		} finally {
			encoder.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void deserializeUsers() {
		try {
			users.clear();
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(USERS_FILE)));

			boolean reading = true;
			while (reading) {
				try {
					users = ((HashMap<String, User>) decoder.readObject());
				} catch (ArrayIndexOutOfBoundsException e) {
					reading = false;
				}
			}
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error deserializing users.");
		} finally {
			decoder.close();
		}
	}
}
