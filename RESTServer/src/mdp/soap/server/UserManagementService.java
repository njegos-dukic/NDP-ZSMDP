/**
 * UserManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp.soap.server;

public interface UserManagementService extends java.rmi.Remote {
    public boolean addUser(java.lang.String username, java.lang.String password, int idZS, java.lang.String stationName) throws java.rmi.RemoteException;
    public mdp.soap.server.Users getAllUsers() throws java.rmi.RemoteException;
    public mdp.soap.server.SerializableStation[] getAllStations() throws java.rmi.RemoteException;
    public boolean isOnline(java.lang.String username) throws java.rmi.RemoteException;
    public void setOnline(java.lang.String username) throws java.rmi.RemoteException;
    public boolean removeUser(java.lang.String username) throws java.rmi.RemoteException;
    public mdp.soap.server.SerializableStation getByIdZSMDP(int id) throws java.rmi.RemoteException;
    public java.lang.String[] getAllActiveUsers(int zsmdpId) throws java.rmi.RemoteException;
    public int[] getAllZsmdpIds() throws java.rmi.RemoteException;
    public int getIdZsmdp(java.lang.String username) throws java.rmi.RemoteException;
    public boolean checkCredentials(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public void logOutUser(java.lang.String username) throws java.rmi.RemoteException;
    public java.lang.String[] getAllActiveUsersByStationName(java.lang.String name) throws java.rmi.RemoteException;
    public int getPort(java.lang.String username) throws java.rmi.RemoteException;
}
