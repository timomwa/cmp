package com.pixelandtag.cmp.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.ExceptionHandler;
import net.sourceforge.stripes.exception.SourcePageNotFoundException;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.validation.SimpleError;

public class CMPExceptionHandler implements ExceptionHandler {

	private Logger logger = Logger.getLogger(CMPExceptionHandler.class);

	@Override
	public void init(Configuration configuration) throws Exception {
		logger.debug("IN init..");

	}

	@Override
	public void handle(Throwable throwable, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ActionBean bean = (ActionBean) request
				.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
		
		 if (AppProperties.isDevMode()) {
	           throw new StripesServletException(throwable);
	     }
		 throwable.printStackTrace();
		logger.info("throwable.getMessage(): "+throwable.getCause());
		logger.info("bean.getContext().getSourcePage() : "+bean.getContext().getSourcePage());
			request.setAttribute("exception", throwable);
			request.setAttribute("msg", "Problem occurred");
			request.getRequestDispatcher("WEB-INF/jsp/error.jsp").forward(request, response);
		//}

	}

}
