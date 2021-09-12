/**
 * UserManagementServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp.soap.server;

public class UserManagementServiceServiceLocator extends org.apache.axis.client.Service implements mdp.soap.server.UserManagementServiceService {

    public UserManagementServiceServiceLocator() {
    }


    public UserManagementServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UserManagementServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for UserManagementService
    private java.lang.String UserManagementService_address = "http://localhost:8080/SOAPServer/services/UserManagementService";

    @Override
	public java.lang.String getUserManagementServiceAddress() {
        return UserManagementService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UserManagementServiceWSDDServiceName = "UserManagementService";

    public java.lang.String getUserManagementServiceWSDDServiceName() {
        return UserManagementServiceWSDDServiceName;
    }

    public void setUserManagementServiceWSDDServiceName(java.lang.String name) {
        UserManagementServiceWSDDServiceName = name;
    }

    @Override
	public mdp.soap.server.UserManagementService getUserManagementService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UserManagementService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUserManagementService(endpoint);
    }

    @Override
	public mdp.soap.server.UserManagementService getUserManagementService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            mdp.soap.server.UserManagementServiceSoapBindingStub _stub = new mdp.soap.server.UserManagementServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getUserManagementServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUserManagementServiceEndpointAddress(java.lang.String address) {
        UserManagementService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (mdp.soap.server.UserManagementService.class.isAssignableFrom(serviceEndpointInterface)) {
                mdp.soap.server.UserManagementServiceSoapBindingStub _stub = new mdp.soap.server.UserManagementServiceSoapBindingStub(new java.net.URL(UserManagementService_address), this);
                _stub.setPortName(getUserManagementServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("UserManagementService".equals(inputPortName)) {
            return getUserManagementService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    @Override
	public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://server.soap.mdp", "UserManagementServiceService");
    }

    private java.util.HashSet ports = null;

    @Override
	public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://server.soap.mdp", "UserManagementService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("UserManagementService".equals(portName)) {
            setUserManagementServiceEndpointAddress(address);
        }
        else
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
