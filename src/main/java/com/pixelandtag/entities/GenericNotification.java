package com.pixelandtag.entities;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericMessage;

public abstract class GenericNotification extends GenericMessage {
	
	
	public GenericNotification(HttpServletRequest request){
		setCmp_tx_id(request.getParameter("CMP_Txid"));
		setErrorCode(ERROR.get(request.getParameter("ERRORCODE")));
	}
	
	private ERROR errorCode;
	
	
	
	public ERROR getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(ERROR errorCode) {
		this.errorCode = errorCode;
	}


	@Override
	public String toString() {
		return "Notification [ERRORCODE=" + errorCode.toString() + ", cMP_Txid="
				+ super.getCmp_tx_id() + "]";
	}
	
	

}
