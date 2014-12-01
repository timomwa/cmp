/**
 * ChargingRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ChargingProcess.com.ibm.sdp.services.charging.vo;

public class ChargingRequest  implements java.io.Serializable {
    private ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestOperation operation;

    private java.lang.String userId;

    private java.lang.String contentId;

    private java.lang.String itemName;

    private java.lang.String contentDescription;

    private java.lang.String circleId;

    private java.lang.String lineOfBusiness;

    private java.lang.String customerSegment;

    private java.lang.String contentMediaType;

    private java.lang.String serviceId;

    private java.lang.String parentId;

    private java.lang.String actualPrice;

    private java.lang.String basePrice;

    private java.lang.String discountApplied;

    private java.lang.String paymentMethod;

    private java.lang.String revenuePercent;

    private java.lang.String netShare;

    private java.lang.String cpId;

    private java.lang.String customerClass;

    private ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestEventType eventType;

    private java.lang.String localTimeStamp;

    private java.lang.String transactionId;

    private java.lang.String subscriptionTypeCode;

    private java.lang.String subscriptionName;

    private java.lang.String parentType;

    private java.lang.String deliveryChannel;

    private java.lang.String subscriptionExternalId;

    private java.lang.String contentSize;

    private java.lang.String currency;

    private java.lang.String copyrightId;

    private java.lang.String sMSkeyword;

    private java.lang.String srcCode;

    private java.lang.String contentUrl;

    private java.lang.String subscriptiondays;

    private java.lang.String cpTransactionId;

    private java.lang.String copyrightDescription;

    public ChargingRequest() {
    }

    public ChargingRequest(
           ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestOperation operation,
           java.lang.String userId,
           java.lang.String contentId,
           java.lang.String itemName,
           java.lang.String contentDescription,
           java.lang.String circleId,
           java.lang.String lineOfBusiness,
           java.lang.String customerSegment,
           java.lang.String contentMediaType,
           java.lang.String serviceId,
           java.lang.String parentId,
           java.lang.String actualPrice,
           java.lang.String basePrice,
           java.lang.String discountApplied,
           java.lang.String paymentMethod,
           java.lang.String revenuePercent,
           java.lang.String netShare,
           java.lang.String cpId,
           java.lang.String customerClass,
           ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestEventType eventType,
           java.lang.String localTimeStamp,
           java.lang.String transactionId,
           java.lang.String subscriptionTypeCode,
           java.lang.String subscriptionName,
           java.lang.String parentType,
           java.lang.String deliveryChannel,
           java.lang.String subscriptionExternalId,
           java.lang.String contentSize,
           java.lang.String currency,
           java.lang.String copyrightId,
           java.lang.String sMSkeyword,
           java.lang.String srcCode,
           java.lang.String contentUrl,
           java.lang.String subscriptiondays,
           java.lang.String cpTransactionId,
           java.lang.String copyrightDescription) {
           this.operation = operation;
           this.userId = userId;
           this.contentId = contentId;
           this.itemName = itemName;
           this.contentDescription = contentDescription;
           this.circleId = circleId;
           this.lineOfBusiness = lineOfBusiness;
           this.customerSegment = customerSegment;
           this.contentMediaType = contentMediaType;
           this.serviceId = serviceId;
           this.parentId = parentId;
           this.actualPrice = actualPrice;
           this.basePrice = basePrice;
           this.discountApplied = discountApplied;
           this.paymentMethod = paymentMethod;
           this.revenuePercent = revenuePercent;
           this.netShare = netShare;
           this.cpId = cpId;
           this.customerClass = customerClass;
           this.eventType = eventType;
           this.localTimeStamp = localTimeStamp;
           this.transactionId = transactionId;
           this.subscriptionTypeCode = subscriptionTypeCode;
           this.subscriptionName = subscriptionName;
           this.parentType = parentType;
           this.deliveryChannel = deliveryChannel;
           this.subscriptionExternalId = subscriptionExternalId;
           this.contentSize = contentSize;
           this.currency = currency;
           this.copyrightId = copyrightId;
           this.sMSkeyword = sMSkeyword;
           this.srcCode = srcCode;
           this.contentUrl = contentUrl;
           this.subscriptiondays = subscriptiondays;
           this.cpTransactionId = cpTransactionId;
           this.copyrightDescription = copyrightDescription;
    }


    /**
     * Gets the operation value for this ChargingRequest.
     * 
     * @return operation
     */
    public ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestOperation getOperation() {
        return operation;
    }


    /**
     * Sets the operation value for this ChargingRequest.
     * 
     * @param operation
     */
    public void setOperation(ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestOperation operation) {
        this.operation = operation;
    }


    /**
     * Gets the userId value for this ChargingRequest.
     * 
     * @return userId
     */
    public java.lang.String getUserId() {
        return userId;
    }


    /**
     * Sets the userId value for this ChargingRequest.
     * 
     * @param userId
     */
    public void setUserId(java.lang.String userId) {
        this.userId = userId;
    }


    /**
     * Gets the contentId value for this ChargingRequest.
     * 
     * @return contentId
     */
    public java.lang.String getContentId() {
        return contentId;
    }


    /**
     * Sets the contentId value for this ChargingRequest.
     * 
     * @param contentId
     */
    public void setContentId(java.lang.String contentId) {
        this.contentId = contentId;
    }


    /**
     * Gets the itemName value for this ChargingRequest.
     * 
     * @return itemName
     */
    public java.lang.String getItemName() {
        return itemName;
    }


    /**
     * Sets the itemName value for this ChargingRequest.
     * 
     * @param itemName
     */
    public void setItemName(java.lang.String itemName) {
        this.itemName = itemName;
    }


    /**
     * Gets the contentDescription value for this ChargingRequest.
     * 
     * @return contentDescription
     */
    public java.lang.String getContentDescription() {
        return contentDescription;
    }


    /**
     * Sets the contentDescription value for this ChargingRequest.
     * 
     * @param contentDescription
     */
    public void setContentDescription(java.lang.String contentDescription) {
        this.contentDescription = contentDescription;
    }


    /**
     * Gets the circleId value for this ChargingRequest.
     * 
     * @return circleId
     */
    public java.lang.String getCircleId() {
        return circleId;
    }


    /**
     * Sets the circleId value for this ChargingRequest.
     * 
     * @param circleId
     */
    public void setCircleId(java.lang.String circleId) {
        this.circleId = circleId;
    }


    /**
     * Gets the lineOfBusiness value for this ChargingRequest.
     * 
     * @return lineOfBusiness
     */
    public java.lang.String getLineOfBusiness() {
        return lineOfBusiness;
    }


    /**
     * Sets the lineOfBusiness value for this ChargingRequest.
     * 
     * @param lineOfBusiness
     */
    public void setLineOfBusiness(java.lang.String lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }


    /**
     * Gets the customerSegment value for this ChargingRequest.
     * 
     * @return customerSegment
     */
    public java.lang.String getCustomerSegment() {
        return customerSegment;
    }


    /**
     * Sets the customerSegment value for this ChargingRequest.
     * 
     * @param customerSegment
     */
    public void setCustomerSegment(java.lang.String customerSegment) {
        this.customerSegment = customerSegment;
    }


    /**
     * Gets the contentMediaType value for this ChargingRequest.
     * 
     * @return contentMediaType
     */
    public java.lang.String getContentMediaType() {
        return contentMediaType;
    }


    /**
     * Sets the contentMediaType value for this ChargingRequest.
     * 
     * @param contentMediaType
     */
    public void setContentMediaType(java.lang.String contentMediaType) {
        this.contentMediaType = contentMediaType;
    }


    /**
     * Gets the serviceId value for this ChargingRequest.
     * 
     * @return serviceId
     */
    public java.lang.String getServiceId() {
        return serviceId;
    }


    /**
     * Sets the serviceId value for this ChargingRequest.
     * 
     * @param serviceId
     */
    public void setServiceId(java.lang.String serviceId) {
        this.serviceId = serviceId;
    }


    /**
     * Gets the parentId value for this ChargingRequest.
     * 
     * @return parentId
     */
    public java.lang.String getParentId() {
        return parentId;
    }


    /**
     * Sets the parentId value for this ChargingRequest.
     * 
     * @param parentId
     */
    public void setParentId(java.lang.String parentId) {
        this.parentId = parentId;
    }


    /**
     * Gets the actualPrice value for this ChargingRequest.
     * 
     * @return actualPrice
     */
    public java.lang.String getActualPrice() {
        return actualPrice;
    }


    /**
     * Sets the actualPrice value for this ChargingRequest.
     * 
     * @param actualPrice
     */
    public void setActualPrice(java.lang.String actualPrice) {
        this.actualPrice = actualPrice;
    }


    /**
     * Gets the basePrice value for this ChargingRequest.
     * 
     * @return basePrice
     */
    public java.lang.String getBasePrice() {
        return basePrice;
    }


    /**
     * Sets the basePrice value for this ChargingRequest.
     * 
     * @param basePrice
     */
    public void setBasePrice(java.lang.String basePrice) {
        this.basePrice = basePrice;
    }


    /**
     * Gets the discountApplied value for this ChargingRequest.
     * 
     * @return discountApplied
     */
    public java.lang.String getDiscountApplied() {
        return discountApplied;
    }


    /**
     * Sets the discountApplied value for this ChargingRequest.
     * 
     * @param discountApplied
     */
    public void setDiscountApplied(java.lang.String discountApplied) {
        this.discountApplied = discountApplied;
    }


    /**
     * Gets the paymentMethod value for this ChargingRequest.
     * 
     * @return paymentMethod
     */
    public java.lang.String getPaymentMethod() {
        return paymentMethod;
    }


    /**
     * Sets the paymentMethod value for this ChargingRequest.
     * 
     * @param paymentMethod
     */
    public void setPaymentMethod(java.lang.String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    /**
     * Gets the revenuePercent value for this ChargingRequest.
     * 
     * @return revenuePercent
     */
    public java.lang.String getRevenuePercent() {
        return revenuePercent;
    }


    /**
     * Sets the revenuePercent value for this ChargingRequest.
     * 
     * @param revenuePercent
     */
    public void setRevenuePercent(java.lang.String revenuePercent) {
        this.revenuePercent = revenuePercent;
    }


    /**
     * Gets the netShare value for this ChargingRequest.
     * 
     * @return netShare
     */
    public java.lang.String getNetShare() {
        return netShare;
    }


    /**
     * Sets the netShare value for this ChargingRequest.
     * 
     * @param netShare
     */
    public void setNetShare(java.lang.String netShare) {
        this.netShare = netShare;
    }


    /**
     * Gets the cpId value for this ChargingRequest.
     * 
     * @return cpId
     */
    public java.lang.String getCpId() {
        return cpId;
    }


    /**
     * Sets the cpId value for this ChargingRequest.
     * 
     * @param cpId
     */
    public void setCpId(java.lang.String cpId) {
        this.cpId = cpId;
    }


    /**
     * Gets the customerClass value for this ChargingRequest.
     * 
     * @return customerClass
     */
    public java.lang.String getCustomerClass() {
        return customerClass;
    }


    /**
     * Sets the customerClass value for this ChargingRequest.
     * 
     * @param customerClass
     */
    public void setCustomerClass(java.lang.String customerClass) {
        this.customerClass = customerClass;
    }


    /**
     * Gets the eventType value for this ChargingRequest.
     * 
     * @return eventType
     */
    public ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestEventType getEventType() {
        return eventType;
    }


    /**
     * Sets the eventType value for this ChargingRequest.
     * 
     * @param eventType
     */
    public void setEventType(ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestEventType eventType) {
        this.eventType = eventType;
    }


    /**
     * Gets the localTimeStamp value for this ChargingRequest.
     * 
     * @return localTimeStamp
     */
    public java.lang.String getLocalTimeStamp() {
        return localTimeStamp;
    }


    /**
     * Sets the localTimeStamp value for this ChargingRequest.
     * 
     * @param localTimeStamp
     */
    public void setLocalTimeStamp(java.lang.String localTimeStamp) {
        this.localTimeStamp = localTimeStamp;
    }


    /**
     * Gets the transactionId value for this ChargingRequest.
     * 
     * @return transactionId
     */
    public java.lang.String getTransactionId() {
        return transactionId;
    }


    /**
     * Sets the transactionId value for this ChargingRequest.
     * 
     * @param transactionId
     */
    public void setTransactionId(java.lang.String transactionId) {
        this.transactionId = transactionId;
    }


    /**
     * Gets the subscriptionTypeCode value for this ChargingRequest.
     * 
     * @return subscriptionTypeCode
     */
    public java.lang.String getSubscriptionTypeCode() {
        return subscriptionTypeCode;
    }


    /**
     * Sets the subscriptionTypeCode value for this ChargingRequest.
     * 
     * @param subscriptionTypeCode
     */
    public void setSubscriptionTypeCode(java.lang.String subscriptionTypeCode) {
        this.subscriptionTypeCode = subscriptionTypeCode;
    }


    /**
     * Gets the subscriptionName value for this ChargingRequest.
     * 
     * @return subscriptionName
     */
    public java.lang.String getSubscriptionName() {
        return subscriptionName;
    }


    /**
     * Sets the subscriptionName value for this ChargingRequest.
     * 
     * @param subscriptionName
     */
    public void setSubscriptionName(java.lang.String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }


    /**
     * Gets the parentType value for this ChargingRequest.
     * 
     * @return parentType
     */
    public java.lang.String getParentType() {
        return parentType;
    }


    /**
     * Sets the parentType value for this ChargingRequest.
     * 
     * @param parentType
     */
    public void setParentType(java.lang.String parentType) {
        this.parentType = parentType;
    }


    /**
     * Gets the deliveryChannel value for this ChargingRequest.
     * 
     * @return deliveryChannel
     */
    public java.lang.String getDeliveryChannel() {
        return deliveryChannel;
    }


    /**
     * Sets the deliveryChannel value for this ChargingRequest.
     * 
     * @param deliveryChannel
     */
    public void setDeliveryChannel(java.lang.String deliveryChannel) {
        this.deliveryChannel = deliveryChannel;
    }


    /**
     * Gets the subscriptionExternalId value for this ChargingRequest.
     * 
     * @return subscriptionExternalId
     */
    public java.lang.String getSubscriptionExternalId() {
        return subscriptionExternalId;
    }


    /**
     * Sets the subscriptionExternalId value for this ChargingRequest.
     * 
     * @param subscriptionExternalId
     */
    public void setSubscriptionExternalId(java.lang.String subscriptionExternalId) {
        this.subscriptionExternalId = subscriptionExternalId;
    }


    /**
     * Gets the contentSize value for this ChargingRequest.
     * 
     * @return contentSize
     */
    public java.lang.String getContentSize() {
        return contentSize;
    }


    /**
     * Sets the contentSize value for this ChargingRequest.
     * 
     * @param contentSize
     */
    public void setContentSize(java.lang.String contentSize) {
        this.contentSize = contentSize;
    }


    /**
     * Gets the currency value for this ChargingRequest.
     * 
     * @return currency
     */
    public java.lang.String getCurrency() {
        return currency;
    }


    /**
     * Sets the currency value for this ChargingRequest.
     * 
     * @param currency
     */
    public void setCurrency(java.lang.String currency) {
        this.currency = currency;
    }


    /**
     * Gets the copyrightId value for this ChargingRequest.
     * 
     * @return copyrightId
     */
    public java.lang.String getCopyrightId() {
        return copyrightId;
    }


    /**
     * Sets the copyrightId value for this ChargingRequest.
     * 
     * @param copyrightId
     */
    public void setCopyrightId(java.lang.String copyrightId) {
        this.copyrightId = copyrightId;
    }


    /**
     * Gets the sMSkeyword value for this ChargingRequest.
     * 
     * @return sMSkeyword
     */
    public java.lang.String getSMSkeyword() {
        return sMSkeyword;
    }


    /**
     * Sets the sMSkeyword value for this ChargingRequest.
     * 
     * @param sMSkeyword
     */
    public void setSMSkeyword(java.lang.String sMSkeyword) {
        this.sMSkeyword = sMSkeyword;
    }


    /**
     * Gets the srcCode value for this ChargingRequest.
     * 
     * @return srcCode
     */
    public java.lang.String getSrcCode() {
        return srcCode;
    }


    /**
     * Sets the srcCode value for this ChargingRequest.
     * 
     * @param srcCode
     */
    public void setSrcCode(java.lang.String srcCode) {
        this.srcCode = srcCode;
    }


    /**
     * Gets the contentUrl value for this ChargingRequest.
     * 
     * @return contentUrl
     */
    public java.lang.String getContentUrl() {
        return contentUrl;
    }


    /**
     * Sets the contentUrl value for this ChargingRequest.
     * 
     * @param contentUrl
     */
    public void setContentUrl(java.lang.String contentUrl) {
        this.contentUrl = contentUrl;
    }


    /**
     * Gets the subscriptiondays value for this ChargingRequest.
     * 
     * @return subscriptiondays
     */
    public java.lang.String getSubscriptiondays() {
        return subscriptiondays;
    }


    /**
     * Sets the subscriptiondays value for this ChargingRequest.
     * 
     * @param subscriptiondays
     */
    public void setSubscriptiondays(java.lang.String subscriptiondays) {
        this.subscriptiondays = subscriptiondays;
    }


    /**
     * Gets the cpTransactionId value for this ChargingRequest.
     * 
     * @return cpTransactionId
     */
    public java.lang.String getCpTransactionId() {
        return cpTransactionId;
    }


    /**
     * Sets the cpTransactionId value for this ChargingRequest.
     * 
     * @param cpTransactionId
     */
    public void setCpTransactionId(java.lang.String cpTransactionId) {
        this.cpTransactionId = cpTransactionId;
    }


    /**
     * Gets the copyrightDescription value for this ChargingRequest.
     * 
     * @return copyrightDescription
     */
    public java.lang.String getCopyrightDescription() {
        return copyrightDescription;
    }


    /**
     * Sets the copyrightDescription value for this ChargingRequest.
     * 
     * @param copyrightDescription
     */
    public void setCopyrightDescription(java.lang.String copyrightDescription) {
        this.copyrightDescription = copyrightDescription;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ChargingRequest)) return false;
        ChargingRequest other = (ChargingRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.operation==null && other.getOperation()==null) || 
             (this.operation!=null &&
              this.operation.equals(other.getOperation()))) &&
            ((this.userId==null && other.getUserId()==null) || 
             (this.userId!=null &&
              this.userId.equals(other.getUserId()))) &&
            ((this.contentId==null && other.getContentId()==null) || 
             (this.contentId!=null &&
              this.contentId.equals(other.getContentId()))) &&
            ((this.itemName==null && other.getItemName()==null) || 
             (this.itemName!=null &&
              this.itemName.equals(other.getItemName()))) &&
            ((this.contentDescription==null && other.getContentDescription()==null) || 
             (this.contentDescription!=null &&
              this.contentDescription.equals(other.getContentDescription()))) &&
            ((this.circleId==null && other.getCircleId()==null) || 
             (this.circleId!=null &&
              this.circleId.equals(other.getCircleId()))) &&
            ((this.lineOfBusiness==null && other.getLineOfBusiness()==null) || 
             (this.lineOfBusiness!=null &&
              this.lineOfBusiness.equals(other.getLineOfBusiness()))) &&
            ((this.customerSegment==null && other.getCustomerSegment()==null) || 
             (this.customerSegment!=null &&
              this.customerSegment.equals(other.getCustomerSegment()))) &&
            ((this.contentMediaType==null && other.getContentMediaType()==null) || 
             (this.contentMediaType!=null &&
              this.contentMediaType.equals(other.getContentMediaType()))) &&
            ((this.serviceId==null && other.getServiceId()==null) || 
             (this.serviceId!=null &&
              this.serviceId.equals(other.getServiceId()))) &&
            ((this.parentId==null && other.getParentId()==null) || 
             (this.parentId!=null &&
              this.parentId.equals(other.getParentId()))) &&
            ((this.actualPrice==null && other.getActualPrice()==null) || 
             (this.actualPrice!=null &&
              this.actualPrice.equals(other.getActualPrice()))) &&
            ((this.basePrice==null && other.getBasePrice()==null) || 
             (this.basePrice!=null &&
              this.basePrice.equals(other.getBasePrice()))) &&
            ((this.discountApplied==null && other.getDiscountApplied()==null) || 
             (this.discountApplied!=null &&
              this.discountApplied.equals(other.getDiscountApplied()))) &&
            ((this.paymentMethod==null && other.getPaymentMethod()==null) || 
             (this.paymentMethod!=null &&
              this.paymentMethod.equals(other.getPaymentMethod()))) &&
            ((this.revenuePercent==null && other.getRevenuePercent()==null) || 
             (this.revenuePercent!=null &&
              this.revenuePercent.equals(other.getRevenuePercent()))) &&
            ((this.netShare==null && other.getNetShare()==null) || 
             (this.netShare!=null &&
              this.netShare.equals(other.getNetShare()))) &&
            ((this.cpId==null && other.getCpId()==null) || 
             (this.cpId!=null &&
              this.cpId.equals(other.getCpId()))) &&
            ((this.customerClass==null && other.getCustomerClass()==null) || 
             (this.customerClass!=null &&
              this.customerClass.equals(other.getCustomerClass()))) &&
            ((this.eventType==null && other.getEventType()==null) || 
             (this.eventType!=null &&
              this.eventType.equals(other.getEventType()))) &&
            ((this.localTimeStamp==null && other.getLocalTimeStamp()==null) || 
             (this.localTimeStamp!=null &&
              this.localTimeStamp.equals(other.getLocalTimeStamp()))) &&
            ((this.transactionId==null && other.getTransactionId()==null) || 
             (this.transactionId!=null &&
              this.transactionId.equals(other.getTransactionId()))) &&
            ((this.subscriptionTypeCode==null && other.getSubscriptionTypeCode()==null) || 
             (this.subscriptionTypeCode!=null &&
              this.subscriptionTypeCode.equals(other.getSubscriptionTypeCode()))) &&
            ((this.subscriptionName==null && other.getSubscriptionName()==null) || 
             (this.subscriptionName!=null &&
              this.subscriptionName.equals(other.getSubscriptionName()))) &&
            ((this.parentType==null && other.getParentType()==null) || 
             (this.parentType!=null &&
              this.parentType.equals(other.getParentType()))) &&
            ((this.deliveryChannel==null && other.getDeliveryChannel()==null) || 
             (this.deliveryChannel!=null &&
              this.deliveryChannel.equals(other.getDeliveryChannel()))) &&
            ((this.subscriptionExternalId==null && other.getSubscriptionExternalId()==null) || 
             (this.subscriptionExternalId!=null &&
              this.subscriptionExternalId.equals(other.getSubscriptionExternalId()))) &&
            ((this.contentSize==null && other.getContentSize()==null) || 
             (this.contentSize!=null &&
              this.contentSize.equals(other.getContentSize()))) &&
            ((this.currency==null && other.getCurrency()==null) || 
             (this.currency!=null &&
              this.currency.equals(other.getCurrency()))) &&
            ((this.copyrightId==null && other.getCopyrightId()==null) || 
             (this.copyrightId!=null &&
              this.copyrightId.equals(other.getCopyrightId()))) &&
            ((this.sMSkeyword==null && other.getSMSkeyword()==null) || 
             (this.sMSkeyword!=null &&
              this.sMSkeyword.equals(other.getSMSkeyword()))) &&
            ((this.srcCode==null && other.getSrcCode()==null) || 
             (this.srcCode!=null &&
              this.srcCode.equals(other.getSrcCode()))) &&
            ((this.contentUrl==null && other.getContentUrl()==null) || 
             (this.contentUrl!=null &&
              this.contentUrl.equals(other.getContentUrl()))) &&
            ((this.subscriptiondays==null && other.getSubscriptiondays()==null) || 
             (this.subscriptiondays!=null &&
              this.subscriptiondays.equals(other.getSubscriptiondays()))) &&
            ((this.cpTransactionId==null && other.getCpTransactionId()==null) || 
             (this.cpTransactionId!=null &&
              this.cpTransactionId.equals(other.getCpTransactionId()))) &&
            ((this.copyrightDescription==null && other.getCopyrightDescription()==null) || 
             (this.copyrightDescription!=null &&
              this.copyrightDescription.equals(other.getCopyrightDescription())));
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
        if (getOperation() != null) {
            _hashCode += getOperation().hashCode();
        }
        if (getUserId() != null) {
            _hashCode += getUserId().hashCode();
        }
        if (getContentId() != null) {
            _hashCode += getContentId().hashCode();
        }
        if (getItemName() != null) {
            _hashCode += getItemName().hashCode();
        }
        if (getContentDescription() != null) {
            _hashCode += getContentDescription().hashCode();
        }
        if (getCircleId() != null) {
            _hashCode += getCircleId().hashCode();
        }
        if (getLineOfBusiness() != null) {
            _hashCode += getLineOfBusiness().hashCode();
        }
        if (getCustomerSegment() != null) {
            _hashCode += getCustomerSegment().hashCode();
        }
        if (getContentMediaType() != null) {
            _hashCode += getContentMediaType().hashCode();
        }
        if (getServiceId() != null) {
            _hashCode += getServiceId().hashCode();
        }
        if (getParentId() != null) {
            _hashCode += getParentId().hashCode();
        }
        if (getActualPrice() != null) {
            _hashCode += getActualPrice().hashCode();
        }
        if (getBasePrice() != null) {
            _hashCode += getBasePrice().hashCode();
        }
        if (getDiscountApplied() != null) {
            _hashCode += getDiscountApplied().hashCode();
        }
        if (getPaymentMethod() != null) {
            _hashCode += getPaymentMethod().hashCode();
        }
        if (getRevenuePercent() != null) {
            _hashCode += getRevenuePercent().hashCode();
        }
        if (getNetShare() != null) {
            _hashCode += getNetShare().hashCode();
        }
        if (getCpId() != null) {
            _hashCode += getCpId().hashCode();
        }
        if (getCustomerClass() != null) {
            _hashCode += getCustomerClass().hashCode();
        }
        if (getEventType() != null) {
            _hashCode += getEventType().hashCode();
        }
        if (getLocalTimeStamp() != null) {
            _hashCode += getLocalTimeStamp().hashCode();
        }
        if (getTransactionId() != null) {
            _hashCode += getTransactionId().hashCode();
        }
        if (getSubscriptionTypeCode() != null) {
            _hashCode += getSubscriptionTypeCode().hashCode();
        }
        if (getSubscriptionName() != null) {
            _hashCode += getSubscriptionName().hashCode();
        }
        if (getParentType() != null) {
            _hashCode += getParentType().hashCode();
        }
        if (getDeliveryChannel() != null) {
            _hashCode += getDeliveryChannel().hashCode();
        }
        if (getSubscriptionExternalId() != null) {
            _hashCode += getSubscriptionExternalId().hashCode();
        }
        if (getContentSize() != null) {
            _hashCode += getContentSize().hashCode();
        }
        if (getCurrency() != null) {
            _hashCode += getCurrency().hashCode();
        }
        if (getCopyrightId() != null) {
            _hashCode += getCopyrightId().hashCode();
        }
        if (getSMSkeyword() != null) {
            _hashCode += getSMSkeyword().hashCode();
        }
        if (getSrcCode() != null) {
            _hashCode += getSrcCode().hashCode();
        }
        if (getContentUrl() != null) {
            _hashCode += getContentUrl().hashCode();
        }
        if (getSubscriptiondays() != null) {
            _hashCode += getSubscriptiondays().hashCode();
        }
        if (getCpTransactionId() != null) {
            _hashCode += getCpTransactionId().hashCode();
        }
        if (getCopyrightDescription() != null) {
            _hashCode += getCopyrightDescription().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ChargingRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/vo", "ChargingRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "operation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/vo", ">ChargingRequest>operation"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contentId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "itemName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contentDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("circleId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "circleId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lineOfBusiness");
        elemField.setXmlName(new javax.xml.namespace.QName("", "lineOfBusiness"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerSegment");
        elemField.setXmlName(new javax.xml.namespace.QName("", "customerSegment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentMediaType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contentMediaType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "serviceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parentId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parentId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actualPrice");
        elemField.setXmlName(new javax.xml.namespace.QName("", "actualPrice"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("basePrice");
        elemField.setXmlName(new javax.xml.namespace.QName("", "basePrice"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("discountApplied");
        elemField.setXmlName(new javax.xml.namespace.QName("", "discountApplied"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paymentMethod");
        elemField.setXmlName(new javax.xml.namespace.QName("", "paymentMethod"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("revenuePercent");
        elemField.setXmlName(new javax.xml.namespace.QName("", "revenuePercent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("netShare");
        elemField.setXmlName(new javax.xml.namespace.QName("", "netShare"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cpId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "cpId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerClass");
        elemField.setXmlName(new javax.xml.namespace.QName("", "customerClass"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eventType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ChargingProcess/com/ibm/sdp/services/charging/vo", ">ChargingRequest>eventType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localTimeStamp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "localTimeStamp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
        elemField.setFieldName("subscriptionTypeCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subscriptionTypeCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subscriptionName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parentType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parentType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deliveryChannel");
        elemField.setXmlName(new javax.xml.namespace.QName("", "deliveryChannel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionExternalId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subscriptionExternalId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentSize");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contentSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currency");
        elemField.setXmlName(new javax.xml.namespace.QName("", "currency"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyrightId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "copyrightId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SMSkeyword");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sMSkeyword"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("srcCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "srcCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contentUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptiondays");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subscriptiondays"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyrightDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "copyrightDescription"));
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

	@Override
	public String toString() {
		return "ChargingRequest [operation=" + operation + ",\r\nuserId=" + userId
				+ ",\r\ncontentId=" + contentId + ",\r\nitemName=" + itemName
				+ ",\r\ncontentDescription=" + contentDescription + ",\r\ncircleId="
				+ circleId + ",\r\nlineOfBusiness=" + lineOfBusiness
				+ ",\r\ncustomerSegment=" + customerSegment
				+ ",\r\ncontentMediaType=" + contentMediaType + ",\r\nserviceId="
				+ serviceId + ",\r\nparentId=" + parentId + ",\r\nactualPrice="
				+ actualPrice + ",\r\nbasePrice=" + basePrice
				+ ",\r\ndiscountApplied=" + discountApplied + ",\r\npaymentMethod="
				+ paymentMethod + ",\r\nrevenuePercent=" + revenuePercent
				+ ",\r\nnetShare=" + netShare + ",\r\ncpId=" + cpId
				+ ",\r\ncustomerClass=" + customerClass + ",\r\neventType="
				+ eventType + ",\r\nlocalTimeStamp=" + localTimeStamp
				+ ",\r\ntransactionId=" + transactionId
				+ ",\r\nsubscriptionTypeCode=" + subscriptionTypeCode
				+ ",\r\nsubscriptionName=" + subscriptionName + ",\r\nparentType="
				+ parentType + ",\r\ndeliveryChannel=" + deliveryChannel
				+ ",\r\nsubscriptionExternalId=" + subscriptionExternalId
				+ ",\r\ncontentSize=" + contentSize + ",\r\ncurrency=" + currency
				+ ",\r\ncopyrightId=" + copyrightId + ",\r\nsMSkeyword=" + sMSkeyword
				+ ",\r\nsrcCode=" + srcCode + ",\r\ncontentUrl=" + contentUrl
				+ ",\r\nsubscriptiondays=" + subscriptiondays
				+ ",\r\ncpTransactionId=" + cpTransactionId
				+ ",\r\ncopyrightDescription=" + copyrightDescription
				+ ",\r\n__equalsCalc=" + __equalsCalc + ",\r\n__hashCodeCalc="
				+ __hashCodeCalc + "]";
	}
    
    
    
    

}
