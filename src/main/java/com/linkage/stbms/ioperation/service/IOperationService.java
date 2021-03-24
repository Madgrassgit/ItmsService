
package com.linkage.stbms.ioperation.service;

import com.linkage.stbms.ioperation.bio.IOperationBIO;

/**
 * 与爱运维MAC认证接口
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-6-4
 * @category com.linkage.stbms.ioperation
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class IOperationService
{

	/**
	 * 判断mac地址是否存在
	 * @param param mac请求xml字符串
	 * @return 返回结果xml字符串
	 */
	public String stbIsValid(String param)
	{
		return new IOperationBIO().stbIsValid(param);
	}
}
