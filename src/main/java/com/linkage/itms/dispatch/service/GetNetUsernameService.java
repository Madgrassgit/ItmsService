
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.WSClient.WSClientProcess;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;

/**
 * 根据MAC地址获取宽带帐号接口
 * 
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2012-3-15 下午02:54:14
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 */
public class GetNetUsernameService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(GetNetUsernameService.class);

	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		logger.warn("inXml({})",inXml);
		
		/** 命名空间 */
		String NAMESPACE = Global.RADIUS_NAMESPACE;
		/** 方法名 */
		String METHOD_GETUSERMODEMINFO = "getNetUsername";
		/** webservice前缀 */
		String PREFIX = "nsl";
		/** Action前缀 */
		String ACTION_PREFIX = "urn:";
		/** 配置文件中配置 */
		String URL = Global.RADIUS_URL;
		/** 入参 */
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("para", inXml);
		String returnXml = "";
//		StringBuffer inParam = new StringBuffer();
//		inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
//		inParam.append("<root>\n");
//		inParam.append("    <CmdID>123456789012345</CmdID>       \n");
//		inParam.append("	<RstCode>0</RstCode>         \n");
//		inParam.append("	<RstMsg>成功</RstMsg>         \n");
//		inParam.append("	<NetUsername>dmtwhzxcs</NetUsername>         \n");
//		inParam.append("</root>                              \n");
		OMElement element = WSClientProcess.serviceReceive(NAMESPACE, PREFIX,
				ACTION_PREFIX + METHOD_GETUSERMODEMINFO, METHOD_GETUSERMODEMINFO, param,
				URL);
		// xml返回值
		if (element != null)
		{
			try
			{
				returnXml = element.getFirstElement().getText();
			}
			catch (Exception e)
			{
				logger.error("解析XML失败");
			}
		}
		// 记录日志
		new RecordLogDAO().recordLog(
				StringUtil.getStringValue(RecordLogDAO.getRandomId()), 0,
				"getNetUsername", "", "", "", 0, inXml, returnXml,
				System.currentTimeMillis() / 1000);
		logger.warn("return({})", returnXml);
		// 回单
		return returnXml;
	}
}
