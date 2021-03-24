package com.linkage.stbms.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.dao.InitDAO;
import com.linkage.stbms.itv.main.StbServGlobals;

public class InitServlet extends HttpServlet {
	private static Logger logger = LoggerFactory.getLogger(InitServlet.class);

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
		// init server home.
		StbServGlobals.G_ServerHome = getServletContext().getRealPath("");
		
		//初始化错误码
		InitDAO.initFaultCode();
		// 初始化 MQ 发布消息 及 其他全局变量
		new InitBIO().init(); 
		InitDAO.initMQ();
		//初始化SuperGather Corba
		InitDAO.initSuperGather();
		//初始化ACS Corba
		InitDAO.initACS();
		
		InitDAO.initResourceBind();
		// 定时统计策略生效执行结果，并FTP到IPTV业务平台指定的服务器
//		if ("1".equals(Global.SCHEDULE_TIME_ENAB)) {
//			new FtpUpLoadListener().start();
//		}
		
//		TestPolicySync.testPolicySync1();
	}

}
