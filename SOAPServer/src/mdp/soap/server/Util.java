package mdp.soap.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Util {
	public static int[] getDistinct(HashMap<String, User> users) {
		return users.entrySet().stream().mapToInt(user -> user.getValue().getIdZS()).distinct().toArray();
	}

	public static String[] getActiveUsers(HashMap<String, User> users, int id) {
		return users.entrySet().stream()
				.filter(user -> user.getValue().getIdZS() == id && user.getValue().getIsOnline())
				.map(user -> user.getValue().getUsername()).toArray(String[]::new);
	}

	public static Users getAllUsers(HashMap<String, User> users) {
		Users usersWrapper = new Users();
		usersWrapper.setUsers(users.values().stream().toArray(User[]::new));

		return usersWrapper;
	}

	public static int getStationIdByName(HashMap<String, User> users, String name) {
		return users.entrySet().stream().filter(u -> u.getValue().getStationName().equals(name)).findFirst().get()
				.getValue().getIdZS();
	}

	public static SerializableStation[] getAllStations(HashMap<String, User> users) {
		ArrayList<SerializableStation> stations = new ArrayList<>();

		for (User u : users.values()) {
			SerializableStation station = new SerializableStation(u.getIdZS(), u.getStationName());
			if (!stations.contains(station))
				stations.add(station);
		}

		SerializableStation[] returnStations = new SerializableStation[stations.size()];

		for (int i = 0; i < stations.size(); i++)
			returnStations[i] = stations.get(i);

		return returnStations;
	}

	public static SerializableStation getSerializableStationByZSMDPId(HashMap<String, User> users, int id) {
		User user = users.entrySet().stream().filter(u -> u.getValue().getIdZS() == id).findFirst().get().getValue();

		return new SerializableStation(user.getIdZS(), user.getStationName());
	}
}
