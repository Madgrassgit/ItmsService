package com.linkage.itms.itv.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.itv.dao.GetItvMacHBDAO;
import com.linkage.itms.itv.obj.GetItvMacHBOBJ;

/**
 * HBDX-REQ-20170330-XuPan-001(湖北ITMS+机顶盒即插即用零配置接口)
 * @author wanghong
 *
 */
public class GetItvMacHBBIO
{
	private static Logger logger = LoggerFactory.getLogger(GetItvMacHBBIO.class);
	private GetItvMacHBOBJ obj = new GetItvMacHBOBJ();
	private GetItvMacHBDAO dao = new GetItvMacHBDAO();
	//设备id
	private List<HashMap<String, String>> devMapList=new ArrayList<HashMap<String,String>>();
	
	public String getItvMac(String inParam)
	{
		obj.setCallXml(inParam);
		
		if(false == obj.check())
		{
			return obj.getReturnXml();
		}
		
		devMapList = dao.getDevByMac(obj.getStbMac().toUpperCase());
		if(null == devMapList || devMapList.size()==0 )
		{
			logger.warn("[{}]GetItvMacBIO Mac地址对应的家庭网关不存在",obj.getStbMac());
			obj.setResult(1002);
			obj.setResultDesc("Mac地址对应的家庭网关不存在");
		}else{
			int k=0;
			String username="";
			for(HashMap<String,String> map :devMapList){
				logger.warn("mac:{},device_id:{},username:{}",obj.getStbMac(),map.get("device_id"),map.get("username"));
				if(!StringUtil.IsEmpty(map.get("username"))){
					username=map.get("username");
					k++;
				}
			}
			if(k>1){
				logger.warn("{}成功，loid{}",obj.getStbMac(),username);
				obj.setLoid(devMapList.get(0).get("username"));
			}else if(k==1){
				logger.warn("{}成功，loid{}",obj.getStbMac(),username);
				obj.setLoid(username);
			}else if(k==0){
				logger.warn("{}Mac地址对应的家庭网关未绑定",obj.getStbMac());
				obj.setResult(1004);
				obj.setResultDesc("Mac地址对应的家庭网关未绑定");
			}
		}
		
		String retXML = obj.getReturnXml();
		
		//记录日志
		new RecordLogDAO().recordLog(obj.getCmdId(), obj.getClientType(), "getItvMac",
				null,null,null,obj.getResult(),inParam,retXML,System.currentTimeMillis()/1000);
		
		return retXML;
	}
}
