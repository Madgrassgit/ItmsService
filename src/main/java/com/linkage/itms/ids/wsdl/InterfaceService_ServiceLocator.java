/**
 * InterfaceService_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.linkage.itms.ids.wsdl;

import com.linkage.itms.Global;

public class InterfaceService_ServiceLocator extends org.apache.axis.client.Service implements InterfaceService_Service {

	private static final long serialVersionUID = 7390116535204537746L;

	public InterfaceService_ServiceLocator() {
    }


    public InterfaceService_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public InterfaceService_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for InterfaceServiceSOAP
    private java.lang.String InterfaceServiceSOAP_address = Global.HTTPSPEED_URL;

    public java.lang.String getInterfaceServiceSOAPAddress() {
        return InterfaceServiceSOAP_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String InterfaceServiceSOAPWSDDServiceName = Global.HTTPSPEED_SERVICENAMESOAP;

    public java.lang.String getInterfaceServiceSOAPWSDDServiceName() {
        return InterfaceServiceSOAPWSDDServiceName;
    }

    public void setInterfaceServiceSOAPWSDDServiceName(java.lang.String name) {
        InterfaceServiceSOAPWSDDServiceName = name;
    }

    public InterfaceService_PortType getInterfaceServiceSOAP() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(InterfaceServiceSOAP_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getInterfaceServiceSOAP(endpoint);
    }

    public InterfaceService_PortType getInterfaceServiceSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            InterfaceServiceSOAPBindingStub _stub = new InterfaceServiceSOAPBindingStub(portAddress, this);
            _stub.setPortName(getInterfaceServiceSOAPWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setInterfaceServiceSOAPEndpointAddress(java.lang.String address) {
        InterfaceServiceSOAP_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (InterfaceService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                InterfaceServiceSOAPBindingStub _stub = new InterfaceServiceSOAPBindingStub(new java.net.URL(InterfaceServiceSOAP_address), this);
                _stub.setPortName(getInterfaceServiceSOAPWSDDServiceName());
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
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("InterfaceServiceSOAP".equals(inputPortName)) {
            return getInterfaceServiceSOAP();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName(Global.HTTPSPEED_NAMESPACE, Global.HTTPSPEED_METHOD);
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName(Global.HTTPSPEED_NAMESPACE, Global.HTTPSPEED_SERVICENAMESOAP));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("InterfaceServiceSOAP".equals(portName)) {
            setInterfaceServiceSOAPEndpointAddress(address);
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
