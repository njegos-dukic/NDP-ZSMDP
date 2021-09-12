package mdp.rest.model;

public class StationArrivals {
	private Station station;
	private String expectedArrival;
	private String postedArrival;

	public StationArrivals() {
	}

	public StationArrivals(Station station, String expectedArrival, String postedArrival) {
		super();
		this.station = station;
		this.expectedArrival = expectedArrival;
		this.postedArrival = postedArrival;
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public String getExpectedArrival() {
		return expectedArrival;
	}

	public void setExpectedArrival(String expectedArrival) {
		this.expectedArrival = expectedArrival;
	}

	public String getPostedArrival() {
		return postedArrival;
	}

	public void setPostedArrival(String postedArrival) {
		this.postedArrival = postedArrival;
	}

}
