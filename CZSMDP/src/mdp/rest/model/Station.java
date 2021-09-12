package mdp.rest.model;

import java.util.HashMap;

public class Station {
	private int id;
	private String name;

	public Station() {
	}

	public Station(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> toMap() {
		HashMap<String, String> map = new HashMap<>();
		map.put("id", this.id + "");
		map.put("name", this.name);

		return map;
	}
}
