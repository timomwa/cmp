/**
 * Charging.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging;

public interface Charging extends java.rmi.Remote {
    public ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingResponse charge(ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequest inputMsg) throws java.rmi.RemoteException, CocLib.com.ibm.sdp.vo.ServiceException;
}
