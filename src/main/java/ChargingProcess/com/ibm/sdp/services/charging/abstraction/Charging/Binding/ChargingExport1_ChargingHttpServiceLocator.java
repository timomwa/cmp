/**
 * ChargingExport1_ChargingHttpServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding;

public class ChargingExport1_ChargingHttpServiceLocator extends org.apache.axis.client.Service implements ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpService {

    public ChargingExport1_ChargingHttpServiceLocator() {
    }


    public ChargingExport1_ChargingHttpServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ChargingExport1_ChargingHttpServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ChargingHttpService_ChargingHttpPort
    private java.lang.String ChargingHttpService_ChargingHttpPort_address = "http://MDSPOWER68.in.ibm.com:9081/ChargingStub/ChargingExport1_ChargingHttpService";

    public java.lang.String getChargingHttpService_ChargingHttpPortAddress() {
        return ChargingHttpService_ChargingHttpPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ChargingHttpService_ChargingHttpPortWSDDServiceName = "ChargingHttpService_ChargingHttpPort";

    public java.lang.String getChargingHttpService_ChargingHttpPortWSDDServiceName() {
        return ChargingHttpService_ChargingHttpPortWSDDServiceName;
    }

    public void setChargingHttpService_ChargingHttpPortWSDDServiceName(java.lang.String name) {
        ChargingHttpService_ChargingHttpPortWSDDServiceName = name;
    }

    public ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging getChargingHttpService_ChargingHttpPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ChargingHttpService_ChargingHttpPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getChargingHttpService_ChargingHttpPort(endpoint);
    }

    public ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging getChargingHttpService_ChargingHttpPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpBindingStub _stub = new ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpBindingStub(portAddress, this);
            _stub.setPortName(getChargingHttpService_ChargingHttpPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setChargingHttpService_ChargingHttpPortEndpointAddress(java.lang.String address) {
        ChargingHttpService_ChargingHttpPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging.class.isAssignableFrom(serviceEndpointInterface)) {
                ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpBindingStub _stub = new ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpBindingStub(new java.net.URL(ChargingHttpService_ChargingHttpPort_address), this);
                _stub.setPortName(getChargingHttpService_ChargingHttpPortWSDDServiceName());
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
        if ("ChargingHttpService_ChargingHttpPort".equals(inputPortName)) {
            return getChargingHttpService_ChargingHttpPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging/Binding", "ChargingExport1_ChargingHttpService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging/Binding", "ChargingHttpService_ChargingHttpPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ChargingHttpService_ChargingHttpPort".equals(portName)) {
            setChargingHttpService_ChargingHttpPortEndpointAddress(address);
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
