package mdp.rest.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

public class Timetable {
	private int id;
	private ArrayList<StationArrivals> stations = new ArrayList<>();

	public Timetable() {
	}

	public Timetable(int id, ArrayList<StationArrivals> stations) {
		super();
		this.id = id;
		this.stations = stations;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<StationArrivals> getStations() {
		return stations;
	}

	public void setStations(ArrayList<StationArrivals> stations) {
		this.stations = stations;
	}

	public HashMap<String, String> toMap() {
		HashMap<String, String> map = new HashMap<>();
		Gson gson = new Gson();
		map.put("timetable", gson.toJson(this));

		return map;
	}
}
