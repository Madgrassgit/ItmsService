package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-29
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DeleteVXLANChecker extends VXLANBaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(DeleteVXLANChecker.class);

	private String inParam = null;

	public DeleteVXLANChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("DeleteVXLANChecker==>check()" + inParam);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));// 客户端类型:1：网厅 2：IPOSS	3：综调 4：RADIUS 5：翼翮
			dealDate = root.elementTextTrim("DealDate");
			servTypeId = StringUtil.getIntegerValue(root.elementTextTrim("ServTypeId"));
			operateId = StringUtil.getIntegerValue(root.elementTextTrim("OperateId"));
			vXLANConfigSequence = StringUtil.getIntegerValue(root.elementTextTrim("VXLANConfigSequence")); 
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			userType = StringUtil.getIntegerValue(param.elementTextTrim("UserType"));
			requestID = param.elementTextTrim("RequestID");
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		if(vXLANConfigSequence <= 0)
		{
			result = 3;
			resultDesc = "VXLANConfigSequence不能为小于1且不能为空";
			return false;
		}
		if(StringUtil.IsEmpty(requestID))
		{
			result = 3;
			resultDesc = "requestID不能为空";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}


	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 最新绑定的Loid
		root.addElement("Loid").addText(StringUtil.getStringValue(loid));
		// 其它Loid信息
		root.addElement("LoidPrev").addText(StringUtil.getStringValue(loidPrev));
		return document.asXML();
	}
}
