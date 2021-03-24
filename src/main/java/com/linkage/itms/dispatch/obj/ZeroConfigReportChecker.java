
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.ZeroConfigReportDAO;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年12月22日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ZeroConfigReportChecker extends BaseChecker
{

	public static final Logger logger = LoggerFactory
			.getLogger(ZeroConfigReportChecker.class);
	
	//正则，14为数字
	static Pattern pattern = Pattern.compile("\\d{14}");
	/**
	 * 业务类型
	 */
	private int serviceType;
	/**
	 * 操作类型
	 */
	private int operateType;
	/**
	 * 入参发送时间
	 */
	private String sendTime;
	
	/**
	 * 入参发送时间的入库格式
	 */
	private long updateTime;
	
	private int inftResult;
	// 客户端调用XML字符串
	protected String callXml;
	/**
	 * 操作类型：2 解绑
	 */
	private int unbind_type = 2;
	/**
	 * 业务类型：机顶盒
	 */
	private int servtype_stb = 4;

	public ZeroConfigReportChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("ZeroConfigReportChecker>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			serviceType = StringUtil
					.getIntegerValue(param.elementTextTrim("ServiceType"));
			operateType = StringUtil
					.getIntegerValue(param.elementTextTrim("OperateType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityID");
			sendTime = param.elementTextTrim("SendTime");
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == cmdIdCheck()|| false == serviceTypeCheck()
				|| false == operateTypeCheck() || false == userInfoCheck()
				|| false == cityIdCheck()
				|| false == sendTimeCheck())
		{
			return false;
		}
		
		if(!("jl_dx".equals(Global.G_instArea) && servtype_stb==serviceType && operateType==unbind_type)){
			// 吉林机顶盒解绑时候不需要mac校验
			if (false == devSNCheck()) {
				return false;
			}
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	private boolean devSNCheck()
	{
		//入参的devsn为mac地址，机顶盒的mac地址是带冒号的格式。家庭网关的mac地址是不带冒号的。
		//业务类型为机顶盒类型，则直接按照带冒号的mac地址格式校验
		if(serviceType==4)
		{
			//add by fanjm begin 20161118 for XJDX-REQ-20161115-HUJG3-001【ITMS系统机顶盒零配置接口对MAC地址规范化处理需求】
			//如果mac地址没有分隔符或者不为冒号，转换成冒号再校验格式
			if(12 == devSn.length() ){
				StringBuffer sb = new StringBuffer();
				sb.append(devSn.substring(0,2)).append(":").append(devSn.substring(2,4)).append(":").append(devSn.substring(4,6))
				.append(":").append(devSn.substring(6,8)).append(":").append(devSn.substring(8,10)).append(":").append(devSn.substring(10,12));
				devSn = sb.toString();
			}
			else if(17 == devSn.length()){
				StringBuffer sb = new StringBuffer();
				sb.append(devSn.substring(0,2)).append(":").append(devSn.substring(3,5)).append(":").append(devSn.substring(6,8))
				.append(":").append(devSn.substring(9,11)).append(":").append(devSn.substring(12,14)).append(":").append(devSn.substring(15,17));
				devSn = sb.toString();
			}
			else{
				result = 1006;
				resultDesc = "MAC地址不合法";
				return false;
			}
			//add by fanjm end
			
			if("jl_dx".equals(Global.G_instArea)){
				if(false == patternMac_jl.matcher(devSn).matches()){
					result = 1006;
					resultDesc = "MAC地址不合法";
					return false;
				}
				return true;
			}
			
			return macCheck();
		}
		//业务类型为家庭网关，则devsn的值需要为不带冒号的mac地址
		else
		{
			if(devSn.length()<6 || devSn.indexOf(":")>0)
			{
				result = 1006;
				resultDesc = "MAC地址不合法";
				return false;
			}
			return true;
		}
	}
	
	private boolean cmdIdCheck()
	{
		if("jl_dx".equals(Global.G_instArea)){
			return true;
		}
		ZeroConfigReportDAO dao = new ZeroConfigReportDAO();
		Map<String,String> map = dao.getCmdIdCount(cmdId);
		if(null!=map && !map.isEmpty() && StringUtil.getIntValue(map, "cmdid_num")!=0)
		{
			result = 1;
			resultDesc = "cmdId已经存在";
			return false;
		}
		return true;
	}

	/**
	 * 业务类型合法性检查
	 * 
	 * @return
	 */
	private boolean serviceTypeCheck()
	{
		if ("jl_dx".equals(Global.G_instArea) && 4 != serviceType)
		{
			result = 1001;
			resultDesc = "业务类型非法";
			return false;
		}
		
		if (1 != serviceType && 2 != serviceType && 3 != serviceType && 4 != serviceType)
		{
			result = 1001;
			resultDesc = "业务类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 操作类型校验
	 * 
	 * @return
	 */
	private boolean operateTypeCheck()
	{
		if (1 != operateType && 2 != operateType)
		{
			result = 1010;
			resultDesc = "操作类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 校验发出时间
	 * @return
	 */
	private boolean sendTimeCheck()
	{
		if (StringUtil.IsEmpty(sendTime) || false == pattern.matcher(sendTime).matches())
		{
			result = 1000;
			resultDesc = "发出时间非法";
			return false;
		}
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		return document.asXML();
	}

	public int getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(int serviceType)
	{
		this.serviceType = serviceType;
	}

	public int getOperateType()
	{
		return operateType;
	}

	public void setOperateType(int operateType)
	{
		this.operateType = operateType;
	}

	public String getSendTime()
	{
		return sendTime;
	}

	public void setSendTime(String sendTime)
	{
		this.sendTime = sendTime;
	}

	
	public long getUpdateTime()
	{
		return updateTime;
	}

	
	public void setUpdateTime(long updateTime)
	{
		this.updateTime = updateTime;
	}

	public int getInftResult()
	{
		return inftResult;
	}

	
	public void setInftResult(int inftResult)
	{
		this.inftResult = inftResult;
	}
	
	
}
