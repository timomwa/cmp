/**
 * ChargingResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ChargingProcess.com.ibm.sdp.services.charging.vo;

public class ChargingResponse  implements java.io.Serializable {
    private java.lang.String status;

    private java.lang.String transactionId;

    private CocLib.com.ibm.sdp.vo.ErrorDetails error;

    private java.lang.String cpTransactionId;

    public ChargingResponse() {
    }

    public ChargingResponse(
           java.lang.String status,
           java.lang.String transactionId,
           CocLib.com.ibm.sdp.vo.ErrorDetails error,
           java.lang.String cpTransactionId) {
           this.status = status;
           this.transactionId = transactionId;
           this.error = error;
           this.cpTransactionId = cpTransactionId;
    }


    /**
     * Gets the status value for this ChargingResponse.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this ChargingResponse.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the transactionId value for this ChargingResponse.
     * 
     * @return transactionId
     */
    public java.lang.String getTransactionId() {
        return transactionId;
    }


    /**
     * Sets the transactionId value for this ChargingResponse.
     * 
     * @param transactionId
     */
    public void setTransactionId(java.lang.String transactionId) {
        this.transactionId = transactionId;
    }


    /**
     * Gets the error value for this ChargingResponse.
     * 
     * @return error
     */
    public CocLib.com.ibm.sdp.vo.ErrorDetails getError() {
        return error;
    }


    /**
     * Sets the error value for this ChargingResponse.
     * 
     * @param error
     */
    public void setError(CocLib.com.ibm.sdp.vo.ErrorDetails error) {
        this.error = error;
    }


    /**
     * Gets the cpTransactionId value for this ChargingResponse.
     * 
     * @return cpTransactionId
     */
    public java.lang.String getCpTransactionId() {
        return cpTransactionId;
    }


    /**
     * Sets the cpTransactionId value for this ChargingResponse.
     * 
     * @param cpTransactionId
     */
    public void setCpTransactionId(java.lang.String cpTransactionId) {
        this.cpTransactionId = cpTransactionId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ChargingResponse)) return false;
        ChargingResponse other = (ChargingResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.transactionId==null && other.getTransactionId()==null) || 
             (this.transactionId!=null &&
              this.transactionId.equals(other.getTransactionId()))) &&
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError()))) &&
            ((this.cpTransactionId==null && other.getCpTransactionId()==null) || 
             (this.cpTransactionId!=null &&
              this.cpTransactionId.equals(other.getCpTransactionId())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getTransactionId() != null) {
            _hashCode += getTransactionId().hashCode();
        }
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        if (getCpTransactionId() != null) {
            _hashCode += getCpTransactionId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ChargingResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/vo", "ChargingResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "transactionId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("", "error"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://CocLib/com/ibm/sdp/vo", "ErrorDetails"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cpTransactionId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "cpTransactionId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
