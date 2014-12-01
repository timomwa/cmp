package ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging;

public class ChargingProxy implements ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging {
  private String _endpoint = null;
  private ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging charging = null;
  
  public ChargingProxy() {
    _initChargingProxy();
  }
  
  public ChargingProxy(String endpoint) {
    _endpoint = endpoint;
    _initChargingProxy();
  }
  
  private void _initChargingProxy() {
    try {
      charging = (new ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Binding.ChargingExport1_ChargingHttpServiceLocator()).getChargingHttpService_ChargingHttpPort();
      if (charging != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)charging)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)charging)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (charging != null)
      ((javax.xml.rpc.Stub)charging)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.Charging getCharging() {
    if (charging == null)
      _initChargingProxy();
    return charging;
  }
  
  public ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingResponse charge(ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequest inputMsg) throws java.rmi.RemoteException, CocLib.com.ibm.sdp.vo.ServiceException{
    if (charging == null)
      _initChargingProxy();
    return charging.charge(inputMsg);
  }
  
  
}