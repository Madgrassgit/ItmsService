package com.linkage.itms.ids.wsdl;

public class InterfaceServiceProxy implements InterfaceService_PortType {
  private String _endpoint = null;
  private InterfaceService_PortType interfaceService_PortType = null;
  
  public InterfaceServiceProxy() {
    _initInterfaceServiceProxy();
  }
  
  public InterfaceServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initInterfaceServiceProxy();
  }
  
  private void _initInterfaceServiceProxy() {
    try {
      interfaceService_PortType = (new InterfaceService_ServiceLocator()).getInterfaceServiceSOAP();
      if (interfaceService_PortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)interfaceService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)interfaceService_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (interfaceService_PortType != null)
      ((javax.xml.rpc.Stub)interfaceService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public InterfaceService_PortType getInterfaceService_PortType() {
    if (interfaceService_PortType == null)
      _initInterfaceServiceProxy();
    return interfaceService_PortType;
  }
  
  public java.lang.String interfaceService(java.lang.String requestMsg) throws java.rmi.RemoteException{
    if (interfaceService_PortType == null)
      _initInterfaceServiceProxy();
    return interfaceService_PortType.interfaceService(requestMsg);
  }
  
  
}