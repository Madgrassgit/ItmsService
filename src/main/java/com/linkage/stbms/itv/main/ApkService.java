package com.linkage.stbms.itv.main;

import com.linkage.stbms.pic.service.ApkSetDownService;

/**
 * 提供服务接口类 ApkService
 * 
 */
public class ApkService {

	/**
	 * 江西 APK 系统调用ITV终端网管平台下发业务接口
	 * @param xmlData
	 * @return
	 */
	public String apkSetDown(String xmlData)
	{
		return new ApkSetDownService().work(xmlData);
	}
}
