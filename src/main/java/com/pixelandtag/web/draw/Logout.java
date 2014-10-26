package com.pixelandtag.web.draw;

import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Class
 *
 * @web.servlet              name="Logout"
 *                           display-name="Name for Logout"
 *                           description="Description for Logout"
 * @web.servlet-mapping      url-pattern="/logout"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class Logout extends HttpServlet {

	public Logout() {
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				cookies[i].setMaxAge(0);
				cookies[i].setPath("/");
				resp.addCookie(cookies[i]);
			}
		}
		resp.sendRedirect(req.getParameter("url"));
		
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		// TODO Auto-generated method stub
	}

}
