package com.linkage.itms.os.main;

import com.linkage.itms.os.bio.CompletedBIO;

/**
 * 向北向接口返回竣工信息
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-6-24 下午03:56:26
 * @category com.linkage.itms.os.main
 * @copyright 南京联创科技 网管科技部
 *
 */
public class CompletedService
{
	public String completedInfo(String strXML)
	{
		return new CompletedBIO().completedInfo(strXML);
	}
	
}
