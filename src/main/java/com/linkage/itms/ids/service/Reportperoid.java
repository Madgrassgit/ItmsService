package com.linkage.itms.ids.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.dispatch.service.IService;
import com.linkage.itms.ids.dao.IdsDAO;
import com.linkage.itms.ids.obj.ReportPeroidOBJ;
import com.linkage.itms.ids.util.IdsUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;


public class Reportperoid implements IService {
	
	private static Logger logger = LoggerFactory
			.getLogger(Reportperoid.class);
	
	private IdsDAO idsDAO = new IdsDAO();
	public String work(String param) {
		// 1.将请求报文转为具体对象
		ReportPeroidOBJ obj = transRequest(param);
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
	private ReportPeroidOBJ transRequest(String param)
	{
		logger.warn("xml request information is[{}]", param);
		ReportPeroidOBJ result = new ReportPeroidOBJ();
		
		try
		{
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new StringReader(param));
			Element root = doc.getRootElement();
			result.setCmdId(root.elementTextTrim("CmdID"));
			
			result.setCmdType(root.elementTextTrim("CmdType"));
			result.setClientType(root.elementTextTrim("ClientType"));
			Element params = root.element("Param");
			result.setTaskId(params.elementTextTrim("taskId"));
			result.setReportPeriod(params.elementTextTrim("reportPeriod"));
			result.setTargetPeroid(params.elementTextTrim("targetPeriod"));
			List<Element> devList = params.element("devs").elements("dev");
			for (Element devElement : devList)
			{
				result.addDevice(devElement.getTextTrim());
			}
		}
		catch (Exception e)
		{
			result.setResult("1", "XML数据格式错误");
			logger.warn("dom4j analize xml[" + param
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
	private String transResponse(ReportPeroidOBJ obj)
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
		logger.warn("xml response information is [{}]", response);
		return response;
	}
	/**
	 * 
	 * @param obj
	 */
	private void checkRequest(ReportPeroidOBJ obj)
	{
		if (!IdsUtil.validClientType(obj.getClientType()))
		{
			obj.setResult("2", "客户端类型非法");
			return;
		}
		if (StringUtil.IsEmpty(obj.getTaskId()))
		{
			obj.setResult("1001", "用户信息类型非法");
			return;
		}
	}
	/**
	 * 调用配置模块下发节点值
	 * @param obj
	 */
	private void handleRequest(ReportPeroidOBJ obj)
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
			logger.warn("开始调用预读模块");
			String[] deviceIdArr = devIdList.toArray(new String[0]);
			String[] paramArr = new String[3];
			paramArr[0] = obj.getReportPeriod();
			paramArr[1] = obj.getTargetPeroid();
			paramArr[2] = obj.getTaskId();
			logger.warn("deviceIdArr=={}", Arrays.toString(deviceIdArr));
			logger.warn("IdsUtil.REPORT_PEROID_SERVICE_ID=="+IdsUtil.REPORT_PEROID_SERVICE_ID);
			logger.warn("paramArr=={}",Arrays.toString(paramArr));
			CreateObjectFactory.createPreProcess().processDeviceStrategy(deviceIdArr,
					IdsUtil.REPORT_PEROID_SERVICE_ID, paramArr);
		}
	}
}
