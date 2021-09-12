package mdp.rest.model;

import java.util.ArrayList;

public class Timetables {
	private ArrayList<Timetable> timetables = new ArrayList<>();

	public ArrayList<Timetable> getTimetables() {
		return timetables;
	}

	public void setTimetables(ArrayList<Timetable> timetables) {
		this.timetables = timetables;
	}
}
