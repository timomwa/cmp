/**
 * ServiceException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package CocLib.com.ibm.sdp.vo;

public class ServiceException  extends org.apache.axis.AxisFault  implements java.io.Serializable {
    private java.lang.String messageId;

    private java.lang.String text;

    public ServiceException() {
    }

    public ServiceException(
           java.lang.String messageId,
           java.lang.String text) {
        this.messageId = messageId;
        this.text = text;
    }


    /**
     * Gets the messageId value for this ServiceException.
     * 
     * @return messageId
     */
    public java.lang.String getMessageId() {
        return messageId;
    }


    /**
     * Sets the messageId value for this ServiceException.
     * 
     * @param messageId
     */
    public void setMessageId(java.lang.String messageId) {
        this.messageId = messageId;
    }


    /**
     * Gets the text value for this ServiceException.
     * 
     * @return text
     */
    public java.lang.String getText() {
        return text;
    }


    /**
     * Sets the text value for this ServiceException.
     * 
     * @param text
     */
    public void setText(java.lang.String text) {
        this.text = text;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServiceException)) return false;
        ServiceException other = (ServiceException) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.messageId==null && other.getMessageId()==null) || 
             (this.messageId!=null &&
              this.messageId.equals(other.getMessageId()))) &&
            ((this.text==null && other.getText()==null) || 
             (this.text!=null &&
              this.text.equals(other.getText())));
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
        if (getMessageId() != null) {
            _hashCode += getMessageId().hashCode();
        }
        if (getText() != null) {
            _hashCode += getText().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    } 

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServiceException.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://CocLib/com/ibm/sdp/vo", "ServiceException"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("messageId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "messageId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("text");
        elemField.setXmlName(new javax.xml.namespace.QName("", "text"));
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
