/**
 * UserManagementServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp.soap.server;

public interface UserManagementServiceService extends javax.xml.rpc.Service {
    public java.lang.String getUserManagementServiceAddress();

    public mdp.soap.server.UserManagementService getUserManagementService() throws javax.xml.rpc.ServiceException;

    public mdp.soap.server.UserManagementService getUserManagementService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
