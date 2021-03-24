package com.linkage.stbms.ids.service;

import com.linkage.stbms.ids.bio.GetStbInfoBIO;
import com.linkage.stbms.itv.main.Global;

/**
 * 
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-6-3 下午03:02:45
 * @category com.linkage.stbms.ids.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class GetStbInfoService
{
	public String work(String inParam)
	{
		if ("jx_dx".equals(Global.G_instArea)||"hb_lt".equals(Global.G_instArea)||"jl_dx".equals(Global.G_instArea)) {
			return new GetStbInfoBIO().getStbInfoForOther(inParam);
		}else {
			return new GetStbInfoBIO().getStbInfo(inParam);
		}
	}
}
