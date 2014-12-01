/**
 * ChargingRequestEventType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ChargingProcess.com.ibm.sdp.services.charging.vo;

public class ChargingRequestEventType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ChargingRequestEventType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "Content Purchase";
    public static final java.lang.String _value2 = "CSR Move To Purchased";
    public static final java.lang.String _value3 = "CSR Purchase";
    public static final java.lang.String _value4 = "CSR Subscription Purchase";
    public static final java.lang.String _value5 = "Event Based Purchase";
    public static final java.lang.String _value6 = "Gifting";
    public static final java.lang.String _value7 = "Licence Renewal";
    public static final java.lang.String _value8 = "Purchase Refund";
    public static final java.lang.String _value9 = "Subscription Purchase";
    public static final java.lang.String _value10 = "ReSubscription";
    public static final ChargingRequestEventType value1 = new ChargingRequestEventType(_value1);
    public static final ChargingRequestEventType value2 = new ChargingRequestEventType(_value2);
    public static final ChargingRequestEventType value3 = new ChargingRequestEventType(_value3);
    public static final ChargingRequestEventType value4 = new ChargingRequestEventType(_value4);
    public static final ChargingRequestEventType value5 = new ChargingRequestEventType(_value5);
    public static final ChargingRequestEventType value6 = new ChargingRequestEventType(_value6);
    public static final ChargingRequestEventType value7 = new ChargingRequestEventType(_value7);
    public static final ChargingRequestEventType value8 = new ChargingRequestEventType(_value8);
    public static final ChargingRequestEventType value9 = new ChargingRequestEventType(_value9);
    public static final ChargingRequestEventType value10 = new ChargingRequestEventType(_value10);
    public java.lang.String getValue() { return _value_;}
    public static ChargingRequestEventType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ChargingRequestEventType enumeration = (ChargingRequestEventType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ChargingRequestEventType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ChargingRequestEventType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/vo", ">ChargingRequest>eventType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
