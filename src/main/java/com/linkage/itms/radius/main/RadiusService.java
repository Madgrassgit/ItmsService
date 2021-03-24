
package com.linkage.itms.radius.main;

import com.linkage.itms.Global;
import com.linkage.itms.radius.service.RadiusSyncService;
import com.linkage.itms.radius.service.RadiusSyncServiceJxdx;

/**
 * 与Radius(AAA)系统对接接口
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.radius.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusService
{

	/**
	 * 修改密码。作为服务端.
	 * @param request
	 */
	public void changePassword(String request)
	{
		if("jx_dx".equals(Global.G_instArea))
		{
			new RadiusSyncServiceJxdx().changePassword(request);
		}
		else
		{
			new RadiusSyncService().changePassword(request);
		}

	}
}
