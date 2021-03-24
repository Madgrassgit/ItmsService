
package com.linkage.itms.oss.main;

import com.linkage.itms.oss.bio.SpecInfoBIO;

/**
 * <pre>
 * 江苏电信ITMS与OSS数据查询接口
 * 服务端：ITMS
 * 客户端：OSS
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-4-2
 * @category com.linkage.itms.oss.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OssService
{
	public String querySpecInfo(String xmlParam)
	{
		return new SpecInfoBIO().querySpecInfo(xmlParam);
	}
}
