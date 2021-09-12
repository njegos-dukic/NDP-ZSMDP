package mdp.soap.server;

public class UserManagementServiceProxy implements mdp.soap.server.UserManagementService {
  private String _endpoint = null;
  private mdp.soap.server.UserManagementService userManagementService = null;

  public UserManagementServiceProxy() {
    _initUserManagementServiceProxy();
  }

  public UserManagementServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initUserManagementServiceProxy();
  }

  private void _initUserManagementServiceProxy() {
    try {
      userManagementService = (new mdp.soap.server.UserManagementServiceServiceLocator()).getUserManagementService();
      if (userManagementService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)userManagementService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)userManagementService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }

    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }

  public String getEndpoint() {
    return _endpoint;
  }

  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (userManagementService != null)
      ((javax.xml.rpc.Stub)userManagementService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

  }

  public mdp.soap.server.UserManagementService getUserManagementService() {
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService;
  }

  @Override
public boolean addUser(java.lang.String username, java.lang.String password, int idZS, java.lang.String stationName) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.addUser(username, password, idZS, stationName);
  }

  @Override
public mdp.soap.server.Users getAllUsers() throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getAllUsers();
  }

  @Override
public mdp.soap.server.SerializableStation[] getAllStations() throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getAllStations();
  }

  @Override
public boolean isOnline(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.isOnline(username);
  }

  @Override
public void setOnline(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    userManagementService.setOnline(username);
  }

  @Override
public boolean removeUser(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.removeUser(username);
  }

  @Override
public mdp.soap.server.SerializableStation getByIdZSMDP(int id) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getByIdZSMDP(id);
  }

  @Override
public java.lang.String[] getAllActiveUsers(int zsmdpId) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getAllActiveUsers(zsmdpId);
  }

  @Override
public int[] getAllZsmdpIds() throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getAllZsmdpIds();
  }

  @Override
public int getIdZsmdp(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getIdZsmdp(username);
  }

  @Override
public boolean checkCredentials(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.checkCredentials(username, password);
  }

  @Override
public void logOutUser(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    userManagementService.logOutUser(username);
  }

  @Override
public java.lang.String[] getAllActiveUsersByStationName(java.lang.String name) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getAllActiveUsersByStationName(name);
  }

  @Override
public int getPort(java.lang.String username) throws java.rmi.RemoteException{
    if (userManagementService == null)
      _initUserManagementServiceProxy();
    return userManagementService.getPort(username);
  }


}