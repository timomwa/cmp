package com.inmobia.celcom.entities;

import javax.servlet.http.HttpServletRequest;

import com.inmobia.celcom.api.ERROR;
import com.inmobia.celcom.api.GenericMessage;

public abstract class GenericNotification extends GenericMessage {
	
	
	public GenericNotification(HttpServletRequest request){
		setCMP_Txid(request.getParameter("CMP_Txid"));
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
				+ super.getCMP_Txid() + "]";
	}
	
	

}
