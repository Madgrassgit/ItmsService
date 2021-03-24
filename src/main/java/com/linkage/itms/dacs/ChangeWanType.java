package com.linkage.itms.dacs;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.litms.acs.soap.object.Base64;


/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2014-3-10
 * @category com.linkage.itms.dacs
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangeWanType extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7843267230575544955L;
	
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ChangeWanType.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// 记录访问日志
		logger.warn("用户主机："+request.getRemoteHost());
		int port = request.getLocalPort();
		
		String usernameStr = request.getParameter("param");
		String userName = Base64.decode(usernameStr);
		logger.warn(usernameStr + "--解密：" + userName);
		
		String sendXml = getSendXml(userName);
		logger.warn("拼装的xml：" + sendXml);
		
		try
		{
			final String endPointReference = "http://localhost:"+port+"/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "BridgeToRout");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { sendXml });
			logger.warn(userName + "结果：" + returnParam);
		}
		catch (ServiceException e)
		{
			logger.error(userName + "发送异常：" + e);
		}
	}

	private String getSendXml(String userName)
	{
		long cmdId = new DateTimeUtil().getLongTime();
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
		inParam.append("<root>\n");
		inParam.append("    <CmdID>"+cmdId+"</CmdID>       \n");
		inParam.append("	<CmdType>CX_01</CmdType>           \n");
		inParam.append("	<ClientType>3</ClientType>         \n");
		inParam.append("	<Param>                            \n");
		inParam.append("		<UserInfoType>1</UserInfoType>      \n");
		inParam.append("		<UserInfo>"+userName+"</UserInfo>  \n");
		inParam.append("		<OperateType>2</OperateType>  \n");
		inParam.append("	</Param>                           \n");
		inParam.append("</root>                              \n");
		
		return inParam.toString();
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		doGet(req, resp);
	}

	@Override
	public void init() throws ServletException
	{

	}
	public static void main(String[] args)
	{
		String str = Base64.encode("x2636998");
		System.out.println(str);
		System.out.println(Base64.decode(str));
	}
}
