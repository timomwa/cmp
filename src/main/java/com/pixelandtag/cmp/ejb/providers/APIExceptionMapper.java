package com.pixelandtag.cmp.ejb.providers;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.Failure;
public class APIExceptionMapper{}
/*
@Provider
public class APIExceptionMapper implements
		ExceptionMapper<APIException> {
	private static final Logger log = Logger.getLogger(MyExceptionMapper.class);

	@Context
	private HttpHeaders headers;

	@XmlRootElement(name = "error")
	public static class Error {
		public Integer status;
		public String message;

		public Error() {
		}

		public Error(String msg, Integer status) {
			this.message = msg;
			this.status = status;
		}
	}

	@Override
	public Response toResponse(APIException exception) {
		log.info("com.pixelandtag.cmp.ejb.providers.APIExceptionMapper.toResponse(APIException)");
		int status = 500;
		String message = exception.getMessage();
		exception.printStackTrace();
		log.info("\n\n\n====[" + message + "]=======\n\n\n");
		
		if (exception instanceof Failure) {
            status = ((Failure) exception).getErrorCode();
        }
		MediaType mediaType;
		try {
			mediaType = headers.getMediaType();
		} catch (Exception e) {
			mediaType = MediaType.APPLICATION_XML_TYPE;
		}
		return Response.status(status).type(mediaType).entity(new Error(message, status)).build();
	}

}
*/
