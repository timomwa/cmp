package com.pixelandtag.api.bulksms;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Path("/rest/mt")
public interface BulkSMSMTReceiverI extends BaseRestI {
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("post")
	public Response post(@Context HttpHeaders headers,InputStream incomingData, @Context HttpServletRequest req) throws QueueException;
	
	
	

	
}
