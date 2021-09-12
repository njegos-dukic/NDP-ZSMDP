package mdp.soap.server;

import java.io.Serializable;

public class Users implements Serializable {

	private static final long serialVersionUID = 1L;

	private User[] users;

	public User[] getUsers() {
		return users;
	}

	public void setUsers(User[] users) {
		this.users = users;
	}
}
