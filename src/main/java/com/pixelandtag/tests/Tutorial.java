package com.pixelandtag.tests;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tutorial {
	 private static final transient Logger log = LoggerFactory.getLogger(Tutorial.class);
	

	public static void main(String[] args) {
		log.info("My First Apache Shiro Application");
		 //1.
	    Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");

	    //2.
	    SecurityManager securityManager = factory.getInstance();

	    //3.
	    SecurityUtils.setSecurityManager(securityManager);
	    
	    
	    Subject currentUser = SecurityUtils.getSubject();
	    System.out.println(currentUser.isAuthenticated());
	    if ( !currentUser.isAuthenticated() ) {
	        //collect user principals and credentials in a gui specific manner 
	        //such as username/password html form, X509 certificate, OpenID, etc.
	        //We'll use the username/password example here since it is the most common.
	        UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");

	        //this is all you have to do to support 'remember me' (no config - built in!):
	        token.setRememberMe(true);

	        currentUser.login(token);
	        log.info( "User [" + currentUser.getPrincipal() + "] logged in successfully." );
	    }
	    
	    if ( currentUser.hasRole( "schwartz" ) ) {
	        log.info("May the Schwartz be with you!" );
	    } else {
	        log.info( "Hello, mere mortal." );
	    }

	    if ( currentUser.isPermitted( "lightsaber:weild" ) ) {
	        log.info("You may use a lightsaber ring.  Use it wisely.");
	    } else {
	        log.info("Sorry, lightsaber rings are for schwartz masters only.");
	    }
	    
	    Session session = currentUser.getSession();
	    session.setAttribute( "someKey", "aValue" );
	    System.out.println(currentUser.isAuthenticated());
	    System.exit(0);
       
		
	}

}
