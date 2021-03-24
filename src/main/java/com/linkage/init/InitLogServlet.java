package com.linkage.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author Jason(3412)
 * @date 2009-12-24
 */
public class InitLogServlet extends HttpServlet {


	/**
	 * zhaixf
	 * 2009-12-24
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public InitLogServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		String prefix = getServletContext().getRealPath("/");
		String file = getInitParameter("log4j-init-file");
//		if (file != null) {
//			PropertyConfigurator.configure(prefix + file);
//		}
		try {
			DOMConfigurator.configure(prefix + file);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
}
