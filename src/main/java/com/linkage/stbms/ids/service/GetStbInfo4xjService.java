package com.linkage.stbms.ids.service;

import com.linkage.stbms.ids.bio.GetStbInfoXJBIO;

/**
 * 
 * @author chenxj6
 * @since 2016-9-28
 * @新疆电信 机顶盒信息查询接口
 * 
 */
public class GetStbInfo4xjService {
	public String work(String inParam) {
		return new GetStbInfoXJBIO().getStbInfoForXJ(inParam);
	}
}
