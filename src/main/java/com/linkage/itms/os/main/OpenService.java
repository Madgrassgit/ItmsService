package com.linkage.itms.os.main;

import com.linkage.itms.os.bio.FinishServiceBIO;
import com.linkage.itms.os.bio.OpenServiceBIO;

/**
 * 
 * @author zhangshimin(工号) Tel:
 * @version 1.0
 * @since 2011-6-24 上午08:25:52
 * @category com.linkage.itms.os.main
 * @copyright 南京联创科技 网管科技部
 *
 */
public class OpenService
{
	public String recieveSheet(String strXML)
	{
		return new OpenServiceBIO().recieveSheet(strXML);
	}

	public String doFinishSheet(String strXML)
	{
		return new FinishServiceBIO().doFinishSheet(strXML);
	}
}
