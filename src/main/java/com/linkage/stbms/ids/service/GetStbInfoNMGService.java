package com.linkage.stbms.ids.service;

import com.linkage.stbms.ids.bio.GetStbInfoNMGBIO;


/**
 * 
 * @author hourui
 * @since 2017-11-20
 * @新疆电信 机顶盒信息查询接口
 * 
 */
public class GetStbInfoNMGService {
	public String work(String inParam) {
		return new GetStbInfoNMGBIO().getStbInfoForNMG(inParam);
	}
}
