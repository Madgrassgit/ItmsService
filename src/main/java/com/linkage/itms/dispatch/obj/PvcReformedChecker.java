package com.linkage.itms.dispatch.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 多PVC考核接口格式检查类
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public class PvcReformedChecker extends BaseQueryChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory
			.getLogger(PvcReformedChecker.class);

	// 1：已部署 -1：未部署
	private int pvcReformed;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PvcReformedChecker(String inXml) {
		callXml = inXml;
	}

	/**
	 * 返回结果
	 * 
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果
		getBaseReturnXml();
		// 多PVC部署状态
		root.addElement("Param").addElement("PvcReformed").addText(
				StringUtil.getStringValue(pvcReformed));

		return document.asXML();
	}

	/** getter, setter methods */

	public int getPvcReformed() {
		return pvcReformed;
	}

	public void setPvcReformed(int pvcReformed) {
		this.pvcReformed = pvcReformed;
	}
}
