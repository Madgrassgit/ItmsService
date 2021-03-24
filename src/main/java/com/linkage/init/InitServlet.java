package com.linkage.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import com.linkage.itms.Global;

import java.io.IOException;

public class InitServlet extends HttpServlet {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2542897545066831112L;

	/**
	 * Constructor of the object.
	 */
	public InitServlet() {
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

		// 初始化server路径
		Global.G_ServerHome = getServletContext().getRealPath("");
		try {
			new InitBIO().init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
