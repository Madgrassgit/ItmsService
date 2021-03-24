
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import javax.annotation.MatchesPattern.Checker;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.commons.util.TimeUtil;
import com.linkage.itms.Global;

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月24日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpgradeChecker extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(UpgradeChecker.class);
	// xml片段
	private String callXml;
	// 调用ID
	private String cmdId;
	// 调用类型：CX_01,固定
	private String cmdType;
	// 调用客户端类型：1：BSS 2：IPOSS 3：综调 4：RADIUS 5：电子运维
	private int clientType;
	// 升级操作人
	private String operator;
	// 调用升级日期
	private Long callDate;
	// 任务号
	private Long taskNumber;
	// 查询结果
	int result;
	// 查询结果描述
	String resultDesc;

	public UpgradeChecker(String callXml)
	{
		this.callXml = callXml;
	}

	public boolean check()
	{
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
			operator = param.elementTextTrim("operator");
			callDate = StringUtil.getLongValue(param.elementTextTrim("CallDate"));
			taskNumber =  StringUtil.getLongValue(param.elementTextTrim("Tasknumber"));
		}
		catch (Exception e)
		{
			logger.warn("校验参数发生异常：{}", e);
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	/**
	 * 获得返回值
	 * @return 返回值
	 */
	public String getReturnXml(){
		logger.debug("getBaseReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		return document.asXML();
	}

	
	public String getCallXml()
	{
		return callXml;
	}

	
	public void setCallXml(String callXml)
	{
		this.callXml = callXml;
	}

	
	public String getCmdId()
	{
		return cmdId;
	}

	
	public void setCmdId(String cmdId)
	{
		this.cmdId = cmdId;
	}

	
	public String getCmdType()
	{
		return cmdType;
	}

	
	public void setCmdType(String cmdType)
	{
		this.cmdType = cmdType;
	}

	
	public int getClientType()
	{
		return clientType;
	}

	
	public void setClientType(int clientType)
	{
		this.clientType = clientType;
	}

	
	public String getOperator()
	{
		return operator;
	}

	
	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	
	public Long getCallDate()
	{
		return callDate;
	}

	
	public void setCallDate(Long callDate)
	{
		this.callDate = callDate;
	}

	
	public Long getTaskNumber()
	{
		return taskNumber;
	}

	
	public void setTaskNumber(Long taskNumber)
	{
		this.taskNumber = taskNumber;
	}

	
	public int getResult()
	{
		return result;
	}

	
	public void setResult(int result)
	{
		this.result = result;
	}

	
	public String getResultDesc()
	{
		return resultDesc;
	}

	
	public void setResultDesc(String resultDesc)
	{
		this.resultDesc = resultDesc;
	}
}
