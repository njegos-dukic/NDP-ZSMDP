/**
 * User.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp.soap.server;

public class User  implements java.io.Serializable {
    private int idZS;

    private boolean isOnline;

    private java.lang.String password;

    private int port;

    private java.lang.String stationName;

    private java.lang.String username;

    public User() {
    }

    public User(
           int idZS,
           boolean isOnline,
           java.lang.String password,
           int port,
           java.lang.String stationName,
           java.lang.String username) {
           this.idZS = idZS;
           this.isOnline = isOnline;
           this.password = password;
           this.port = port;
           this.stationName = stationName;
           this.username = username;
    }


    /**
     * Gets the idZS value for this User.
     *
     * @return idZS
     */
    public int getIdZS() {
        return idZS;
    }


    /**
     * Sets the idZS value for this User.
     *
     * @param idZS
     */
    public void setIdZS(int idZS) {
        this.idZS = idZS;
    }


    /**
     * Gets the isOnline value for this User.
     *
     * @return isOnline
     */
    public boolean isIsOnline() {
        return isOnline;
    }


    /**
     * Sets the isOnline value for this User.
     *
     * @param isOnline
     */
    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }


    /**
     * Gets the password value for this User.
     *
     * @return password
     */
    public java.lang.String getPassword() {
        return password;
    }


    /**
     * Sets the password value for this User.
     *
     * @param password
     */
    public void setPassword(java.lang.String password) {
        this.password = password;
    }


    /**
     * Gets the port value for this User.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }


    /**
     * Sets the port value for this User.
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Gets the stationName value for this User.
     *
     * @return stationName
     */
    public java.lang.String getStationName() {
        return stationName;
    }


    /**
     * Sets the stationName value for this User.
     *
     * @param stationName
     */
    public void setStationName(java.lang.String stationName) {
        this.stationName = stationName;
    }


    /**
     * Gets the username value for this User.
     *
     * @return username
     */
    public java.lang.String getUsername() {
        return username;
    }


    /**
     * Sets the username value for this User.
     *
     * @param username
     */
    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    private java.lang.Object __equalsCalc = null;
    @Override
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            this.idZS == other.getIdZS() &&
            this.isOnline == other.isIsOnline() &&
            ((this.password==null && other.getPassword()==null) ||
             (this.password!=null &&
              this.password.equals(other.getPassword()))) &&
            this.port == other.getPort() &&
            ((this.stationName==null && other.getStationName()==null) ||
             (this.stationName!=null &&
              this.stationName.equals(other.getStationName()))) &&
            ((this.username==null && other.getUsername()==null) ||
             (this.username!=null &&
              this.username.equals(other.getUsername())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    @Override
	public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getIdZS();
        _hashCode += (isIsOnline() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getPassword() != null) {
            _hashCode += getPassword().hashCode();
        }
        _hashCode += getPort();
        if (getStationName() != null) {
            _hashCode += getStationName().hashCode();
        }
        if (getUsername() != null) {
            _hashCode += getUsername().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(User.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://server.soap.mdp", "User"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idZS");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "idZS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isOnline");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "isOnline"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("password");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "password"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("port");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "port"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stationName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "stationName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("username");
        elemField.setXmlName(new javax.xml.namespace.QName("http://server.soap.mdp", "username"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
