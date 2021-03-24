package com.linkage.itms.rms.servlet;

import com.linkage.itms.dispatch.main.CallService;
import com.linkage.itms.rms.obj.JsonEntity;
import com.linkage.itms.rms.util.RmsUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
		
public class ResetServlet extends HttpServlet 
{
	public static final Logger logger = LoggerFactory.getLogger(RebootServlet.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	this.doPost(request, response);
	}
	/**
	 *<?xml version="1.0" encoding="GBK"?>
		<root>
			<CmdID>123456789012345</CmdID>
			<CmdType>CX_01</CmdType>
			<ClientType>3</ClientType>
			<Param>
		<UserInfoType>1</UserInfoType>
				< UserInfo >njkd123456</ UserInfo >
			</Param>
		</root>
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cmdId = RmsUtil.genCmdId();
		String userInfoType = request.getParameter("userInfoType");
		String userInfo = request.getParameter("userInfo");

		//必须的设置，返回串类型指定json
		response.setContentType("application/json;charset=UTF-8");

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"GBK\"?>")
		.append("<root>")
		.append("<CmdID>"+cmdId+"</CmdID>")
		.append("<CmdType>CX_01</CmdType>")
		.append("<ClientType>3</ClientType>")
		.append("<Param>")
		.append("<UserInfoType>"+userInfoType+"</UserInfoType>")
		.append("<UserInfo>"+userInfo+"</UserInfo>")
		.append("</Param>")
		.append("</root>");
		logger.warn("准备恢复出厂设置, 参数{}", sb.toString());

		CallService service = new CallService();
		String ret = service.reset(sb.toString());
		logger.warn("恢复出厂设置结束,恢复出厂设置返回结果:{}", ret);
		SAXReader reader = new SAXReader();
		Document document  = null;
		try
		{
			document = reader.read(new StringReader(ret));
		}
		catch (DocumentException e)
		{
			logger.error("读取xml失败.", e);
			return;
		}
		Element root = document.getRootElement();
		String rstCode = root.elementTextTrim("RstCode");
		String rstMsg = root.elementTextTrim("RstMsg");

		// 书写json对象实体
		JsonEntity entity = new JsonEntity();
		entity.setCode(rstCode);
		entity.setDetail(rstMsg);
		
		// 写出json
		RmsUtil.writeJson(entity, response);
		
	}
}

	