package com.linkage.itms.itv.bio;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.itv.dao.GetItvMacDAO;
import com.linkage.itms.itv.obj.GetItvMacOBJ;

/**
 * AHDX_ITMS-REQ-20170227YQW-001(通过MCA地址，查询stbind事件中对应的LOID信息)
 * @author wanghong
 *
 */
public class GetItvMacBIO
{
	private static Logger logger = LoggerFactory.getLogger(GetItvMacBIO.class);
	private GetItvMacOBJ obj = new GetItvMacOBJ();
	private GetItvMacDAO dao = new GetItvMacDAO();
	//设备id
	private Map<String,String> devMap;
	//用户loid
	private Map<String,String> loidMap;
	
	public String getItvMac(String inParam)
	{
		obj.setCallXml(inParam);
		
		if(false == obj.check())
		{
			return obj.getReturnXml();
		}
		
		devMap = dao.getDevByMac(obj.getStbMac());
		if(null == devMap || devMap.isEmpty() )
		{
			logger.warn("[{}]GetItvMacBIO 机顶盒Mac地址对应的设备不存在",obj.getStbMac());
			obj.setResult(1002);
			obj.setResultDesc("无此用户信息");
		}
		else
		{
			loidMap=dao.getLoid(devMap.get("device_id"));
			if(null == loidMap || loidMap.isEmpty() )
			{
				logger.warn("[{}]GetItvMacBIO 设备[{}]对应的用户不存在",obj.getStbMac(),devMap.get("device_id"));
				obj.setResult(1002);
				obj.setResultDesc("无此用户信息");
			}
			else
			{
				obj.setLoid(loidMap.get("username"));
			}
			
		}
		
		String retXML = obj.getReturnXml();
		
		//记录日志
		new RecordLogDAO().recordLog(obj.getCmdId(), obj.getClientType(), "getItvMac",
				null,null,null,obj.getResult(),inParam,retXML,System.currentTimeMillis()/1000);
		
		return retXML;
	}
}
