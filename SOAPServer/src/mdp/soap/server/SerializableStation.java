package mdp.soap.server;

import java.io.Serializable;

public class SerializableStation implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idTS;
	private String name = null;

	public SerializableStation() {
	}

	public SerializableStation(int idTS, String name) {
		this.idTS = idTS;
		this.name = name;

	}

	public int getIdTS() {
		return idTS;
	}

	public void setIdTS(int idTS) {
		this.idTS = idTS;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
