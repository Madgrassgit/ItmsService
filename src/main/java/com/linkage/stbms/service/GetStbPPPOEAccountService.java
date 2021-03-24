package com.linkage.stbms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.WSClient.WSClientProcess;
import com.linkage.stbms.itv.main.Global;



/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-11-9 上午10:32:13
 * @category com.linkage.stbms.service<br>
 * @copyright 亚信联创 网管产品部
 */
public class GetStbPPPOEAccountService
{
	private static Logger logger = LoggerFactory.getLogger(GetStbPPPOEAccountService.class);
	public String getStbPppoeAccount(String inParm)
	{
		logger.warn("调用ITMS接口参数：" + inParm);
		//调用接口方法
		String resltFromItms = WSClientProcess.callItmsService(Global.ITMS_URL,inParm,"getItvAccount");
		logger.warn("调用ITMS接口获取itv接入账号返回结果：" + resltFromItms);
		return resltFromItms;
	}
}
