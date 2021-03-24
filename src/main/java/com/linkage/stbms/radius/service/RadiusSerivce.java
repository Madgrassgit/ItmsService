
package com.linkage.stbms.radius.service;

import com.linkage.stbms.radius.bio.RadiusBIO;

/**
 * 与AAA系统（Radius）交互接口服务类
 * 
 * @author fangchao (Ailk No.69934)
 * @version 1.0
 * @since 2013-6-24
 * @category com.linkage.stbms.radius.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusSerivce
{

	/**
	 * 机顶盒零配置接口
	 * 
	 * @param request
	 *            requestId(请求参数,固定值：ID00010)~ipAddress(IP地址)
	 * @return 成功：ID00010~0~端口信息~上网帐号~mac地址~上/下线时间~目前帐号IP地址<br>
	 *         失败：ID00010~1
	 */
	public String getItvCfg(String request)
	{
		return new RadiusBIO().getItvCfg(request);
	}
}
