
package com.linkage.itms.ids.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.ids.dao.IdsDAO;
import com.linkage.itms.ids.obj.DiagnosticEnableOBJ;
import com.linkage.itms.ids.util.IdsUtil;

/**
 * 设备状态信息上报功能开启和关闭接口
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-10-17
 * @category com.linkage.itms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnosticEnableService
{

	private static final Logger logger = LoggerFactory
			.getLogger(DiagnosticEnableService.class);
	private IdsDAO idsDAO = new IdsDAO();

	/**
	 * 设备状态信息上报功能开启和关闭接口
	 * 
	 * @param xmlRequest
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String diagnosticEnable(String xmlRequest)
	{
		// 1.将请求报文转为具体对象
		DiagnosticEnableOBJ obj = transRequest(xmlRequest);
		if (!obj.valid())
		{
			return transResponse(obj);
		}
		// 2.具体对象校验数据校验
		checkRequest(obj);
		if (!obj.valid())
		{
			return transResponse(obj);
		}
		// 3.循环处理，调用配置模块，下发参数
		handleRequest(obj);
		// 4.返回结果
		return transResponse(obj);
	}

	/**
	 * 将请求xml报文转化为具体对象
	 * 
	 * @param xmlRequest
	 *            请求xml报文
	 * @return 将报文转为具体对象，如果xml报文异常，将返回null
	 */
	@SuppressWarnings("unchecked")
	private DiagnosticEnableOBJ transRequest(String xmlRequest)
	{
		logger.warn("DiagnosticEnableService inParam:[{}]", xmlRequest);
		DiagnosticEnableOBJ result = new DiagnosticEnableOBJ();
		try
		{
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new StringReader(xmlRequest));
			Element root = doc.getRootElement();
			result.setCmdId(root.elementTextTrim("CmdID"));
			result.setCmdType(root.elementTextTrim("CmdType"));
			result.setClientType(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			result.setTaskId(param.elementTextTrim("taskId"));
			result.setPeriod(param.elementTextTrim("period"));
			result.setFileServerIp(param.elementText("fileServerIp"));
			result.setFileServerPort(param.elementText("fileServerPort"));
			result.setEnable(param.elementText("enable"));
			result.setParamList(param.elementTextTrim("paramList"));
			List<Element> devList = param.element("devs").elements("dev");
			for (Element devElement : devList)
			{
				result.addDevice(devElement.getTextTrim());
			}
		}
		catch (Exception e)
		{
			result.setResult("1", "XML数据格式错误");
			logger.warn("DiagnosticEnableService xml[" + xmlRequest
					+ "] error, request xml invalid");
			logger.warn(e.getMessage(), e);
		}
		logger.warn("trans xml request result is [{}]", result);
		return result;
	}

	/**
	 * 将具体对象转换为响应报文格式
	 * 
	 * @param obj
	 * @return
	 */
	private String transResponse(DiagnosticEnableOBJ obj)
	{
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GBK");
		Element root = doc.addElement("root");
		root.addElement("CmdID").addText(obj.getCmdId());
		root.addElement("RstCode").addText(obj.getResultNo());
		root.addElement("RstMsg").addText(obj.getResultMsg());
		root.addElement("taskId").addText(obj.getTaskId());
		Element devs = root.addElement("devs");
		Iterator<Map.Entry<String, String>> devIt = obj.getDevMap().entrySet().iterator();
		Map.Entry<String, String> devEntry = null;
		while (devIt.hasNext())
		{
			devEntry = devIt.next();
			devs.addElement("dev").addAttribute("result", devEntry.getValue())
					.addText(devEntry.getKey());
		}
		String response = doc.asXML();
		logger.warn("DiagnosticEnableService returnParam:[{}]", response);
		return response;
	}

	private void checkRequest(DiagnosticEnableOBJ obj)
	{
		if (!IdsUtil.validClientType(obj.getClientType()))
		{
			obj.setResult("2", "客户端类型非法");
			return;
		}
		if (!IdsUtil.validEnable(obj.getEnable()))
		{
			obj.setResult("1001", "用户信息类型非法");
			return;
		}
		// 当开启状态信息上报功能，需要校验参数
		if (IdsUtil.DIAGNOSTIC_ENABLE_YES.equals(obj.getEnable()))
		{
			if (!IdsUtil.validNumber(obj.getPeriod()))
			{
				obj.setResult("1001", "用户信息类型非法");
				return;
			}
			if (!IdsUtil.validParam(obj.getParamList()))
			{
				obj.setResult("1001", "用户信息类型非法");
				return;
			}
		}
		if (StringUtil.IsEmpty(obj.getTaskId()))
		{
			obj.setResult("1001", "用户信息类型非法");
			return;
		}
	}

	private void handleRequest(DiagnosticEnableOBJ obj)
	{
		List<String> devIdList = new ArrayList<String>(obj.getDevMap().size());
		Iterator<Map.Entry<String, String>> devIt = obj.getDevMap().entrySet().iterator();
		Map.Entry<String, String> devEntry = null;
		String devSn = null;
		String devId = null;
		while (devIt.hasNext())
		{
			devEntry = devIt.next();
			devSn = devEntry.getKey();
			if (StringUtil.IsEmpty(devSn))
			{
				devEntry.setValue(IdsUtil.DEVICE_STATUS_NOT_EXIST);
			}
			else
			{
				devId = idsDAO.getDeviceId(devSn);
				if (StringUtil.IsEmpty(devId))
				{
					devEntry.setValue(IdsUtil.DEVICE_STATUS_NOT_EXIST);
				}
				else
				{
					devEntry.setValue(IdsUtil.DEVICE_STATUS_SENDING);
					devIdList.add(devId);
				}
			}
		}
		if (devIdList.size() > 0)
		{
			String[] deviceIdArr = devIdList.toArray(new String[0]);
			String[] paramArr = new String[6];
			paramArr[0] = obj.getEnable();
			paramArr[1] = obj.getPeriod();
			paramArr[2] = obj.getFileServerIp();
			paramArr[3] = obj.getParamList();
			paramArr[4] = obj.getFileServerPort();
			paramArr[5] = obj.getTaskId();
			CreateObjectFactory.createPreProcess().processDeviceStrategy(deviceIdArr,
					IdsUtil.DEFAULT_SERVICE_ID, paramArr);
		}
	}
}
