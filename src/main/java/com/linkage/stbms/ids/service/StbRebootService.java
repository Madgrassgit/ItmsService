
package com.linkage.stbms.ids.service;

import com.linkage.stbms.ids.bio.StbRebootBIO;
import com.linkage.stbms.itv.main.Global;

/**
 * @author Administrator(工号) Tel:??
 * @version 1.0
 * @since 2011-4-21 下午06:09:49
 * @category com.linkage.stbms.ids.service
 * @copyright 南京联创科技 网管科技部
 */
public class StbRebootService
{
	/**
	 * 江西、宁夏电信机顶盒重启接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String work(String inParam)
	{
		if ("jx_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea)||"hb_lt".equals(Global.G_instArea)
				||"xj_dx".equals(Global.G_instArea) ||"nmg_dx".equals(Global.G_instArea)) {
			return new StbRebootBIO().setStbRebootTwo(inParam);
		} else {
			return new StbRebootBIO().setStbReboot(inParam);
		}
	}
}
