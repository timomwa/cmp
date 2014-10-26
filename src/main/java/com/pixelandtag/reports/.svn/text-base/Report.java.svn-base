package com.inmobia.axiata.reports;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class Report
 */
public class Report extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	 public Logger logger = Logger.getLogger(Report.class); 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Report() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//return ReportGenerator.generateReport(int statusId,int categoryId,String date,String msisdn,java.sql.Connection);
		Connection conn = getConnection();
		
		String dateSelection = request.getParameter("phone_voucher_num");
		
		
		
		
		File f = ReportGenerator.generateReport(dateSelection,conn);
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		response.setContentType("application/xls");
		
		response.setHeader("Content-Disposition","attachment; filename=\""+f.getName()+"\";");
		
		
		int                 length   = 0;
        ServletOutputStream op       = response.getOutputStream();
        //ServletContext      context  = getServletConfig().getServletContext();
       // String              mimetype = context.getMimeType( f.getName() );
        
        
        response.setContentType("application/xls");
        
        byte[] bbuf = new byte[1024];
        
        DataInputStream in = new DataInputStream(new FileInputStream(f));

        while ((in != null) && ((length = in.read(bbuf)) != -1))
        {
            op.write(bbuf,0,length);
        }

        in.close();
        op.flush();
        op.close();
	
	}
	
	
	
	public Connection getConnection(){
		 try{  
		 	Context initContext = new InitialContext();
		 	DataSource ds = (DataSource)initContext.lookup("java:/RESP_EDITOR");
		 	return ds.getConnection();
		 }catch(Exception e){
		 	try {
				//Ok.. make a normal connection ... this is a test
				 Class.forName("com.mysql.jdbc.Driver");
				 return DriverManager.getConnection("jdbc:mysql://localhost:3306/nacc?user=root&password=");
		 	} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SQLException eq) {
				// TODO Auto-generated catch block
				eq.printStackTrace();
			}
		 	
		 	
		 }
		return null;
	}
	

}
