
package com.linkage.stbms.pic.bio;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.cao.ParameValueOBJ;
import com.linkage.stbms.pic.dao.ApkSetDownDao;
import com.linkage.stbms.pic.service.ApkSetDownChecker;

/**
 * 江西 APK 系统调用ITV终端网管平台下发业务接口
 * @return
 */
public class ApkSetDownBio
{
	private static final Logger logger = LoggerFactory.getLogger(ApkSetDownBio.class);
	private ApkSetDownDao dao = new ApkSetDownDao();
	
	public Map<String,String> getUserInfoyByServAccount(String servAccount){
		logger.debug("getUserInfoyByServAccount({})",servAccount);
		return dao.getUserInfoyByServAccount(servAccount);
	}
	
	public Map<String,String> getdevInfoByMac(String mac){
		logger.debug("getdevInfoByMac({})",mac);
		return dao.getdevInfoByMac(mac);
	}
	
	public String getInParam(String cmdId, String servAccount, String servPwd){
		logger.debug("getInParam({},{},{})",cmdId, servAccount, servPwd);
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		sb.append("<root>");
		sb.append("<userID>" + servAccount + "<userID>");
		sb.append("<newPassword>" + servPwd + "<newPassword>");
		sb.append("<sequenceID>" + cmdId + "<sequenceID>");
		sb.append("</root>");
		
		return sb.toString();
	}
	
	public ArrayList<ParameValueOBJ> getObjList(String newPwd)
	{
		String path = "Device.X_CTC_IPTV.ServiceInfo.UserPassword";
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		
		ParameValueOBJ echoCancellationObj = new ParameValueOBJ();
		echoCancellationObj.setName(path);
		echoCancellationObj.setValue(newPwd);
		echoCancellationObj.setType("1");
		objList.add(echoCancellationObj);
	
		return objList;
	}
	
	public void saveApkRes(ApkSetDownChecker checker){
		dao.saveApkRes(checker);
	}
	
}
