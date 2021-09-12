package mdp.soap.server;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private int idZS;
	private String stationName;
	private boolean isOnline;
	private int port;
	private static int ordinal = 1;

	public User() {
		this.port = 9000 + ordinal;
		User.ordinal++;
	}

	public User(String username, String password, int idZS, String stationName) {
		this.username = username;
		this.password = password;
		this.idZS = idZS;
		this.stationName = stationName;
		this.isOnline = false;
		this.port = 9000 + ordinal;
		User.ordinal++;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getIdZS() {
		return idZS;
	}

	public void setIdZS(int idZS) {
		this.idZS = idZS;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public boolean getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idZS, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		User other = (User) obj;
		return idZS == other.idZS && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", idZS=" + idZS + "]";
	}
}
